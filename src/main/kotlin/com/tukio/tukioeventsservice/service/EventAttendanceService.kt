package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.EventAttendanceRequest
import com.tukio.tukioeventsservice.dto.EventAttendanceResponse
import com.tukio.tukioeventsservice.model.EventAttendance
import com.tukio.tukioeventsservice.repository.EventAttendanceRepository
import com.tukio.tukioeventsservice.repository.EventRepository
import com.tukio.tukioeventsservice.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class EventAttendanceService(
    private val attendanceRepository: EventAttendanceRepository,
    private val eventRepository: EventRepository
) {

    private val logger = LoggerFactory.getLogger(EventAttendanceService::class.java)

    fun recordAttendance(eventId: Long, userId: Long, request: EventAttendanceRequest): EventAttendanceResponse {
        // Validate event exists
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }

        // Business Rule: Can only record attendance if event has started or ended
        val now = LocalDateTime.now()
        if (now.isBefore(event.startTime)) {
            throw IllegalStateException("Cannot record attendance before event starts")
        }

        // Check if user already recorded attendance
        val existingAttendance = attendanceRepository.findByEventIdAndUserId(eventId, userId)

        val attendance = if (existingAttendance != null) {
            // Update existing attendance record
            existingAttendance.copy(
                attended = request.attended,
                recordedAt = LocalDateTime.now()
            ).also { attendanceRepository.save(it) }
        } else {
            // Create new attendance record
            val newAttendance = EventAttendance(
                eventId = eventId,
                userId = userId,
                attended = request.attended,
                recordedAt = LocalDateTime.now()
            )
            attendanceRepository.save(newAttendance)
        }

        logger.info("User $userId ${if (request.attended) "attended" else "did not attend"} event $eventId")

        return EventAttendanceResponse(
            id = attendance.id,
            eventId = attendance.eventId,
            userId = attendance.userId,
            attended = attendance.attended,
            recordedAt = attendance.recordedAt,
            canRate = canUserRateEvent(event, attendance.attended)
        )
    }

    fun getUserAttendanceForEvent(eventId: Long, userId: Long): EventAttendanceResponse? {
        val attendance = attendanceRepository.findByEventIdAndUserId(eventId, userId)
            ?: return null

        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }

        return EventAttendanceResponse(
            id = attendance.id,
            eventId = attendance.eventId,
            userId = attendance.userId,
            attended = attendance.attended,
            recordedAt = attendance.recordedAt,
            canRate = canUserRateEvent(event, attendance.attended)
        )
    }

    fun getEventAttendees(eventId: Long): List<EventAttendanceResponse> {
        val attendances = attendanceRepository.findByEventId(eventId)
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }

        return attendances.map { attendance ->
            EventAttendanceResponse(
                id = attendance.id,
                eventId = attendance.eventId,
                userId = attendance.userId,
                attended = attendance.attended,
                recordedAt = attendance.recordedAt,
                canRate = canUserRateEvent(event, attendance.attended)
            )
        }
    }

    private fun canUserRateEvent(event: com.tukio.tukioeventsservice.model.Event, attended: Boolean): Boolean {
        val now = LocalDateTime.now()
        // Can rate if: event has ended AND user attended
        return now.isAfter(event.endTime) && attended
    }
}
