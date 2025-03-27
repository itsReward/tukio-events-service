package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.client.VenueServiceClient
import com.tukio.tukioeventsservice.dto.*
import com.tukio.tukioeventsservice.exception.ResourceNotFoundException
import com.tukio.tukioeventsservice.model.Event
import com.tukio.tukioeventsservice.model.EventStatus
import com.tukio.tukioeventsservice.repository.EventCategoryRepository
import com.tukio.tukioeventsservice.repository.EventRegistrationRepository
import com.tukio.tukioeventsservice.repository.EventRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class EventServiceImpl(
    private val eventRepository: EventRepository,
    private val eventCategoryRepository: EventCategoryRepository,
    private val eventRegistrationRepository: EventRegistrationRepository,
    private val venueServiceClient: VenueServiceClient
) : EventService {

    private val logger = LoggerFactory.getLogger(EventServiceImpl::class.java)

    override fun getAllEvents(): List<EventDTO> {
        return eventRepository.findAll().map { it.toDTO() }
    }

    override fun getEventById(id: Long): EventDTO {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $id") }
        return event.toDTO()
    }

    @Transactional
    override fun createEvent(eventRequest: EventCreateRequest): EventDTO {
        val category = eventCategoryRepository.findById(eventRequest.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found with id: ${eventRequest.categoryId}") }

        // Validate time range
        if (eventRequest.endTime.isBefore(eventRequest.startTime)) {
            throw IllegalArgumentException("End time cannot be before start time")
        }

        val event = Event(
            title = eventRequest.title,
            description = eventRequest.description,
            category = category,
            startTime = eventRequest.startTime,
            endTime = eventRequest.endTime,
            location = eventRequest.location,
            venueId = eventRequest.venueId,
            maxParticipants = eventRequest.maxParticipants,
            organizer = eventRequest.organizer,
            organizerId = eventRequest.organizerId,
            imageUrl = eventRequest.imageUrl,
            tags = eventRequest.tags?.toMutableSet() ?: mutableSetOf(),
            status = EventStatus.SCHEDULED
        )

        val savedEvent = eventRepository.save(event)
        logger.info("Created new event: ${savedEvent.id} - ${savedEvent.title}")

        // If venue ID is not provided, try to allocate one automatically
        if (savedEvent.venueId == null) {
            try {
                allocateVenueForEvent(savedEvent.id)
            } catch (e: Exception) {
                logger.warn("Failed to automatically allocate venue for event ${savedEvent.id}: ${e.message}")
            }
        }

        return savedEvent.toDTO()
    }

    @Transactional
    override fun updateEvent(id: Long, eventRequest: EventUpdateRequest): EventDTO {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $id") }

        // Update fields if provided
        eventRequest.title?.let { event.title = it }
        eventRequest.description?.let { event.description = it }

        eventRequest.categoryId?.let { categoryId ->
            val category = eventCategoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }
            event.category = category
        }

        // Validate time range if both start and end times are provided
        if (eventRequest.startTime != null && eventRequest.endTime != null) {
            if (eventRequest.endTime.isBefore(eventRequest.startTime)) {
                throw IllegalArgumentException("End time cannot be before start time")
            }
            event.startTime = eventRequest.startTime
            event.endTime = eventRequest.endTime
        } else {
            // Update individual time fields if provided
            eventRequest.startTime?.let { event.startTime = it }
            eventRequest.endTime?.let { event.endTime = it }

            // Revalidate after individual updates
            if (event.endTime.isBefore(event.startTime)) {
                throw IllegalArgumentException("End time cannot be before start time")
            }
        }

        eventRequest.location?.let { event.location = it }
        eventRequest.venueId?.let { event.venueId = it }
        eventRequest.maxParticipants?.let { event.maxParticipants = it }
        eventRequest.imageUrl?.let { event.imageUrl = it }

        eventRequest.tags?.let {
            event.tags.clear()
            event.tags.addAll(it)
        }

        eventRequest.status?.let { event.status = it }

        event.updatedAt = LocalDateTime.now()

        val updatedEvent = eventRepository.save(event)
        logger.info("Updated event: ${updatedEvent.id} - ${updatedEvent.title}")

        return updatedEvent.toDTO()
    }

    @Transactional
    override fun deleteEvent(id: Long) {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $id") }

        // Check if the event has registrations
        val registrationsCount = eventRegistrationRepository.countByEventId(id)
        if (registrationsCount > 0) {
            // Instead of deleting, mark as cancelled
            event.status = EventStatus.CANCELLED
            event.updatedAt = LocalDateTime.now()
            eventRepository.save(event)
            logger.info("Event with id: $id marked as cancelled because it has $registrationsCount registrations")
        } else {
            // If no registrations, delete the event
            eventRepository.deleteById(id)
            logger.info("Deleted event with id: $id")
        }
    }

    override fun searchEvents(criteria: EventSearchCriteria): List<EventDTO> {
        // Handle tag search separately if tags are provided
        if (!criteria.tags.isNullOrEmpty()) {
            val eventsMatchingTags = eventRepository.findByAllTagsMatching(
                criteria.tags,
                criteria.tags.size.toLong()
            )

            // Further filter the results based on other criteria
            return eventsMatchingTags
                .filter { event ->
                    (criteria.categoryId == null || event.category.id == criteria.categoryId) &&
                            (criteria.keyword == null ||
                                    event.title.contains(criteria.keyword, ignoreCase = true) ||
                                    event.description.contains(criteria.keyword, ignoreCase = true)) &&
                            (criteria.startFrom == null || event.startTime >= criteria.startFrom) &&
                            (criteria.startTo == null || event.startTime <= criteria.startTo) &&
                            (criteria.status == null || event.status == criteria.status)
                }
                .map { it.toDTO() }
        }

        // If no tags, use the repository method that handles other criteria
        return eventRepository.findBySearchCriteria(
            criteria.categoryId,
            criteria.keyword,
            criteria.startFrom,
            criteria.startTo,
            criteria.status
        ).map { it.toDTO() }
    }

    override fun getUpcomingEvents(): List<EventDTO> {
        return eventRepository.findUpcomingEvents(
            EventStatus.SCHEDULED,
            LocalDateTime.now()
        ).map { it.toDTO() }
    }

    override fun getEventsByOrganizer(organizerId: Long): List<EventDTO> {
        return eventRepository.findByOrganizerId(organizerId).map { it.toDTO() }
    }

    override fun getEventsByCategory(categoryId: Long): List<EventDTO> {
        return eventRepository.findByCategoryId(categoryId).map { it.toDTO() }
    }

    @Transactional
    override fun allocateVenueForEvent(id: Long): AllocationResponseDTO {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $id") }

        if (event.status == EventStatus.CANCELLED) {
            throw IllegalStateException("Cannot allocate venue for cancelled event")
        }

        // Prepare request for venue service
        val venueRequest = VenueAllocationRequest(
            eventId = event.id,
            eventName = event.title,
            startTime = event.startTime,
            endTime = event.endTime,
            attendeeCount = event.maxParticipants,
            requiredAmenities = null, // Could be based on event category or tags
            preferredVenueType = null, // Could be derived from event category
            preferredLocation = event.location,
            notes = "Event allocation for ${event.title}"
        )

        try {
            // Call venue service to allocate a venue
            val response = venueServiceClient.allocateVenue(venueRequest)

            if (response.success && response.venueId != null) {
                // Update event with allocated venue
                event.venueId = response.venueId
                event.updatedAt = LocalDateTime.now()
                eventRepository.save(event)

                logger.info("Successfully allocated venue ${response.venueId} for event ${event.id}")
            } else {
                logger.warn("Venue allocation failed for event ${event.id}: ${response.message}")
            }

            return response
        } catch (e: Exception) {
            logger.error("Error allocating venue for event ${event.id}: ${e.message}", e)
            return AllocationResponseDTO(
                success = false,
                venueId = null,
                venueName = null,
                message = "Error communicating with venue service: ${e.message}"
            )
        }
    }

    override fun getEventSummary(id: Long): EventSummaryDTO {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $id") }

        val registrationCount = eventRegistrationRepository.countByEventId(id)

        return EventSummaryDTO(
            id = event.id,
            title = event.title,
            categoryName = event.category.name,
            startTime = event.startTime,
            endTime = event.endTime,
            location = event.location,
            organizer = event.organizer,
            status = event.status,
            registrationCount = registrationCount,
            maxParticipants = event.maxParticipants
        )
    }

    // Extension function to convert Entity to DTO
    private fun Event.toDTO(): EventDTO {
        val registrationCount = eventRegistrationRepository.countByEventId(this.id)

        // For simplicity, venue name is not included here
        // In a real app, you'd use a venue service client to get venue details
        // or cache venue information

        return EventDTO(
            id = this.id,
            title = this.title,
            description = this.description,
            categoryId = this.category.id,
            categoryName = this.category.name,
            startTime = this.startTime,
            endTime = this.endTime,
            location = this.location,
            venueId = this.venueId,
            venueName = null, // Would be populated from venue service in a real implementation
            maxParticipants = this.maxParticipants,
            organizer = this.organizer,
            organizerId = this.organizerId,
            imageUrl = this.imageUrl,
            tags = this.tags,
            status = this.status,
            currentRegistrations = registrationCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

}