package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.EventRegistrationDTO
import com.tukio.tukioeventsservice.dto.EventRegistrationRequest
import com.tukio.tukioeventsservice.dto.EventRegistrationUpdateRequest
import com.tukio.tukioeventsservice.exception.EventRegistrationException
import com.tukio.tukioeventsservice.exception.ResourceNotFoundException
import com.tukio.tukioeventsservice.model.EventRegistration
import com.tukio.tukioeventsservice.model.EventStatus
import com.tukio.tukioeventsservice.repository.EventRegistrationRepository
import com.tukio.tukioeventsservice.repository.EventRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventRegistrationServiceImpl(
    private val eventRegistrationRepository: EventRegistrationRepository,
    private val eventRepository: EventRepository
) : EventRegistrationService {

    private val logger = LoggerFactory.getLogger(EventRegistrationServiceImpl::class.java)

    override fun getAllRegistrations(): List<EventRegistrationDTO> {
        return eventRegistrationRepository.findAll().map { it.toDTO() }
    }

    override fun getRegistrationById(id: Long): EventRegistrationDTO {
        val registration = eventRegistrationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Registration not found with id: $id") }
        return registration.toDTO()
    }

    override fun getRegistrationsByEventId(eventId: Long): List<EventRegistrationDTO> {
        if (!eventRepository.existsById(eventId)) {
            throw ResourceNotFoundException("Event not found with id: $eventId")
        }
        return eventRegistrationRepository.findByEventId(eventId).map { it.toDTO() }
    }

    override fun getRegistrationsByUserId(userId: Long): List<EventRegistrationDTO> {
        return eventRegistrationRepository.findByUserId(userId).map { it.toDTO() }
    }

    override fun getUserUpcomingEvents(userId: Long): List<EventRegistrationDTO> {
        return eventRegistrationRepository.findUpcomingRegistrationsByUserId(
            userId,
            LocalDateTime.now()
        ).map { it.toDTO() }
    }

    override fun getUserPastEvents(userId: Long): List<EventRegistrationDTO> {
        return eventRegistrationRepository.findPastRegistrationsByUserId(
            userId,
            LocalDateTime.now()
        ).map { it.toDTO() }
    }

    @Transactional
    override fun registerForEvent(registrationRequest: EventRegistrationRequest): EventRegistrationDTO {
        val event = eventRepository.findById(registrationRequest.eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: ${registrationRequest.eventId}") }

        // Check if user is already registered
        val existingRegistration = eventRegistrationRepository.findByEventIdAndUserId(
            registrationRequest.eventId,
            registrationRequest.userId
        )

        if (existingRegistration != null) {
            if (existingRegistration.status == "CANCELLED") {
                // If previously cancelled, reactivate it
                existingRegistration.status = "REGISTERED"
                existingRegistration.updatedAt = LocalDateTime.now()
                return eventRegistrationRepository.save(existingRegistration).toDTO()
            } else {
                throw EventRegistrationException("User is already registered for this event")
            }
        }

        // Validate event status
        if (event.status != EventStatus.SCHEDULED && event.status != EventStatus.RESCHEDULED) {
            throw EventRegistrationException("Cannot register for event with status: ${event.status}")
        }

        // Check if event is in the past
        if (event.startTime.isBefore(LocalDateTime.now())) {
            throw EventRegistrationException("Cannot register for past events")
        }

        // Check if event has reached max capacity
        val currentRegistrations = eventRegistrationRepository.countByEventIdAndStatus(event.id, "REGISTERED")
        if (currentRegistrations >= event.maxParticipants) {
            throw EventRegistrationException("Event has reached maximum capacity")
        }

        // Create new registration
        val registration = EventRegistration(
            event = event,
            userId = registrationRequest.userId,
            userName = registrationRequest.userName,
            userEmail = registrationRequest.userEmail
        )

        val savedRegistration = eventRegistrationRepository.save(registration)
        logger.info("User ${registration.userId} registered for event ${event.id}")

        return savedRegistration.toDTO()
    }

    @Transactional
    override fun updateRegistration(id: Long, updateRequest: EventRegistrationUpdateRequest): EventRegistrationDTO {
        val registration = eventRegistrationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Registration not found with id: $id") }

        // Update fields if provided
        updateRequest.status?.let { registration.status = it }
        updateRequest.checkInTime?.let { registration.checkInTime = it }
        updateRequest.feedback?.let { registration.feedback = it }
        updateRequest.rating?.let { registration.rating = it }

        registration.updatedAt = LocalDateTime.now()
        return eventRegistrationRepository.save(registration).toDTO()
    }

    @Transactional
    override fun cancelRegistration(id: Long): EventRegistrationDTO {
        val registration = eventRegistrationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Registration not found with id: $id") }

        if (registration.status == "CANCELLED") {
            throw EventRegistrationException("Registration is already cancelled")
        }

        // Check if event is already in progress or completed
        if (registration.event.startTime.isBefore(LocalDateTime.now())) {
            throw EventRegistrationException("Cannot cancel registration for event that has already started")
        }

        registration.status = "CANCELLED"
        registration.updatedAt = LocalDateTime.now()

        return eventRegistrationRepository.save(registration).toDTO()
    }

    @Transactional
    override fun checkInAttendee(eventId: Long, userId: Long): EventRegistrationDTO {
        val registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
            ?: throw ResourceNotFoundException("Registration not found for event: $eventId and user: $userId")

        if (registration.status != "REGISTERED") {
            throw EventRegistrationException("Cannot check in attendee with registration status: ${registration.status}")
        }

        // Check if event is active
        val event = registration.event
        val now = LocalDateTime.now()

        if (now.isBefore(event.startTime.minusHours(1)) || now.isAfter(event.endTime)) {
            throw EventRegistrationException("Check-in is only available from 1 hour before event start until event end")
        }

        registration.status = "ATTENDED"
        registration.checkInTime = LocalDateTime.now()
        registration.updatedAt = LocalDateTime.now()

        return eventRegistrationRepository.save(registration).toDTO()
    }

    @Transactional
    override fun submitFeedback(eventId: Long, userId: Long, feedback: String, rating: Int?): EventRegistrationDTO {
        val registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
            ?: throw ResourceNotFoundException("Registration not found for event: $eventId and user: $userId")

        // Validate rating if provided
        if (rating != null && (rating < 1 || rating > 5)) {
            throw IllegalArgumentException("Rating must be between 1 and 5")
        }

        // Check if event has ended
        if (registration.event.endTime.isAfter(LocalDateTime.now())) {
            throw EventRegistrationException("Feedback can only be submitted after the event has ended")
        }

        registration.feedback = feedback
        registration.rating = rating
        registration.updatedAt = LocalDateTime.now()

        return eventRegistrationRepository.save(registration).toDTO()
    }

    // Extension function to convert Entity to DTO
    private fun EventRegistration.toDTO(): EventRegistrationDTO {
        return EventRegistrationDTO(
            id = this.id,
            eventId = this.event.id,
            eventTitle = this.event.title,
            userId = this.userId,
            userName = this.userName,
            userEmail = this.userEmail,
            status = this.status,
            checkInTime = this.checkInTime,
            feedback = this.feedback,
            rating = this.rating,
            registrationTime = this.registrationTime
        )
    }
}