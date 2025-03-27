package com.tukio.tukioeventsservice.controller

import com.tukio.tukioeventsservice.dto.EventRegistrationDTO
import com.tukio.tukioeventsservice.dto.EventRegistrationRequest
import com.tukio.tukioeventsservice.dto.EventRegistrationUpdateRequest
import com.tukio.tukioeventsservice.service.EventRegistrationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/event-registrations")
class EventRegistrationController(private val eventRegistrationService: EventRegistrationService) {

    @GetMapping
    fun getAllRegistrations(): ResponseEntity<List<EventRegistrationDTO>> {
        return ResponseEntity.ok(eventRegistrationService.getAllRegistrations())
    }

    @GetMapping("/{id}")
    fun getRegistrationById(@PathVariable id: Long): ResponseEntity<EventRegistrationDTO> {
        return ResponseEntity.ok(eventRegistrationService.getRegistrationById(id))
    }

    @GetMapping("/event/{eventId}")
    fun getRegistrationsByEventId(@PathVariable eventId: Long): ResponseEntity<List<EventRegistrationDTO>> {
        return ResponseEntity.ok(eventRegistrationService.getRegistrationsByEventId(eventId))
    }

    @GetMapping("/user/{userId}")
    fun getRegistrationsByUserId(@PathVariable userId: Long): ResponseEntity<List<EventRegistrationDTO>> {
        return ResponseEntity.ok(eventRegistrationService.getRegistrationsByUserId(userId))
    }

    @GetMapping("/user/{userId}/upcoming")
    fun getUserUpcomingEvents(@PathVariable userId: Long): ResponseEntity<List<EventRegistrationDTO>> {
        return ResponseEntity.ok(eventRegistrationService.getUserUpcomingEvents(userId))
    }

    @GetMapping("/user/{userId}/past")
    fun getUserPastEvents(@PathVariable userId: Long): ResponseEntity<List<EventRegistrationDTO>> {
        return ResponseEntity.ok(eventRegistrationService.getUserPastEvents(userId))
    }

    @PostMapping("/register")
    fun registerForEvent(@RequestBody registrationRequest: EventRegistrationRequest): ResponseEntity<EventRegistrationDTO> {
        val registration = eventRegistrationService.registerForEvent(registrationRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(registration)
    }

    @PutMapping("/{id}")
    fun updateRegistration(
        @PathVariable id: Long,
        @RequestBody updateRequest: EventRegistrationUpdateRequest
    ): ResponseEntity<EventRegistrationDTO> {
        return ResponseEntity.ok(eventRegistrationService.updateRegistration(id, updateRequest))
    }

    @PutMapping("/{id}/cancel")
    fun cancelRegistration(@PathVariable id: Long): ResponseEntity<EventRegistrationDTO> {
        return ResponseEntity.ok(eventRegistrationService.cancelRegistration(id))
    }

    @PostMapping("/event/{eventId}/user/{userId}/check-in")
    fun checkInAttendee(
        @PathVariable eventId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<EventRegistrationDTO> {
        return ResponseEntity.ok(eventRegistrationService.checkInAttendee(eventId, userId))
    }

    @PostMapping("/event/{eventId}/user/{userId}/feedback")
    fun submitFeedback(
        @PathVariable eventId: Long,
        @PathVariable userId: Long,
        @RequestParam feedback: String,
        @RequestParam(required = false) rating: Int?
    ): ResponseEntity<EventRegistrationDTO> {
        return ResponseEntity.ok(eventRegistrationService.submitFeedback(eventId, userId, feedback, rating))
    }
}