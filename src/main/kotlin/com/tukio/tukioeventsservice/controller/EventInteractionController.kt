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
        @RequestHeader("X-Auth-User") userIdHeader: String,
        @Valid @RequestBody request: EventAttendanceRequest
    ): ResponseEntity<EventAttendanceResponse> {
        val userId = userIdHeader.toLong()
        val response = attendanceService.recordAttendance(eventId, userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{eventId}/attendance/me")
    fun getMyAttendance(
        @PathVariable eventId: Long,
        @RequestHeader("X-Auth-User") userIdHeader: String
    ): ResponseEntity<EventAttendanceResponse?> {
        val userId = userIdHeader.toLong()
        val response = attendanceService.getUserAttendanceForEvent(eventId, userId)
        return ResponseEntity.ok(response)
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
        @RequestHeader("X-Auth-User") userIdHeader: String,
        @Valid @RequestBody request: EventRatingRequest
    ): ResponseEntity<EventRatingResponse> {
        val userId = userIdHeader.toLong()
        val response = ratingService.rateEvent(eventId, userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{eventId}/rating/me")
    fun getMyRating(
        @PathVariable eventId: Long,
        @RequestHeader("X-Auth-User") userIdHeader: String
    ): ResponseEntity<EventRatingResponse?> {
        val userId = userIdHeader.toLong()
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