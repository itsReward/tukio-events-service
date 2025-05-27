package com.tukio.tukioeventsservice.controller

import com.tukio.tukioeventsservice.dto.*
import com.tukio.tukioeventsservice.service.EventAttendanceService
import com.tukio.tukioeventsservice.service.EventRatingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/events")
class EventInteractionController(
    private val attendanceService: EventAttendanceService,
    private val ratingService: EventRatingService
) {

    // Attendance endpoints
    @PostMapping("/{eventId}/attendance")
    fun recordAttendance(
        @PathVariable eventId: Long,
        @Valid @RequestBody request: EventAttendanceRequest
    ): ResponseEntity<EventAttendanceResponse> {
        val userId = request.userId
        val response = attendanceService.recordAttendance(eventId, userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{eventId}/attendance/{userId}")
    fun getMyAttendance(
        @PathVariable eventId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<EventAttendanceResponse?> {
        val response = attendanceService.getUserAttendanceForEvent(eventId, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user/{userId}/attended")
    fun getUserAttendedEvents(
        @PathVariable userId: Long
    ): ResponseEntity<List<EventAttendedDTO>> {
        val attendedEvents = attendanceService.getUserAttendedEvents(userId)
        return ResponseEntity.ok(attendedEvents)
    }



    @GetMapping("/{eventId}/attendees")
    fun getEventAttendees(
        @PathVariable eventId: Long
    ): ResponseEntity<List<EventAttendanceResponse>> {
        val attendees = attendanceService.getEventAttendees(eventId)
        return ResponseEntity.ok(attendees)
    }

    // Rating endpoints
    @PostMapping("/{eventId}/rating")
    fun rateEvent(
        @PathVariable eventId: Long,
        @Valid @RequestBody request: EventRatingRequest
    ): ResponseEntity<EventRatingResponse> {
        val userId = request.userId
        val response = ratingService.rateEvent(eventId, userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{eventId}/rating/{userId}")
    fun getMyRating(
        @PathVariable eventId: Long,
        @PathVariable userId: Long,
    ): ResponseEntity<EventRatingResponse?> {
        val response = ratingService.getUserRatingForEvent(eventId, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{eventId}/ratings")
    fun getEventRatings(
        @PathVariable eventId: Long
    ): ResponseEntity<List<EventRatingResponse>> {
        val ratings = ratingService.getEventRatings(eventId)
        return ResponseEntity.ok(ratings)
    }

    @GetMapping("/{eventId}/ratings/summary")
    fun getEventRatingSummary(
        @PathVariable eventId: Long
    ): ResponseEntity<EventRatingSummaryDTO> {
        val summary = ratingService.getEventRatingSummary(eventId)
        return ResponseEntity.ok(summary)
    }
}