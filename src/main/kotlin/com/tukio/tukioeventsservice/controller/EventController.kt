package com.tukio.tukioeventsservice.controller

import com.tukio.tukioeventsservice.dto.*
import com.tukio.tukioeventsservice.model.EventStatus
import com.tukio.tukioeventsservice.service.EventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {

    @GetMapping
    fun getAllEvents(): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(eventService.getAllEvents())
    }

    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: Long): ResponseEntity<EventDTO> {
        return ResponseEntity.ok(eventService.getEventById(id))
    }

    @PostMapping
    fun createEvent(@RequestBody eventRequest: EventCreateRequest): ResponseEntity<EventDTO> {
        val createdEvent = eventService.createEvent(eventRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent)
    }

    @PutMapping("/{id}")
    fun updateEvent(
        @PathVariable id: Long,
        @RequestBody eventRequest: EventUpdateRequest
    ): ResponseEntity<EventDTO> {
        return ResponseEntity.ok(eventService.updateEvent(id, eventRequest))
    }

    @DeleteMapping("/{id}")
    fun deleteEvent(@PathVariable id: Long): ResponseEntity<Unit> {
        eventService.deleteEvent(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    fun searchEvents(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startFrom: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTo: LocalDateTime?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<EventDTO>> {
        // Convert status string to enum if provided
        val statusEnum = status?.let {
            try {
                EventStatus.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        val criteria = EventSearchCriteria(
            categoryId = categoryId,
            keyword = keyword,
            startFrom = startFrom,
            startTo = startTo,
            tags = tags,
            status = statusEnum
        )

        return ResponseEntity.ok(eventService.searchEvents(criteria))
    }

    @GetMapping("/upcoming")
    fun getUpcomingEvents(): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(eventService.getUpcomingEvents())
    }

    @GetMapping("/organizer/{organizerId}")
    fun getEventsByOrganizer(@PathVariable organizerId: Long): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizerId))
    }

    @GetMapping("/category/{categoryId}")
    fun getEventsByCategory(@PathVariable categoryId: Long): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(eventService.getEventsByCategory(categoryId))
    }

    @PostMapping("/{id}/allocate-venue")
    fun allocateVenueForEvent(@PathVariable id: Long): ResponseEntity<AllocationResponseDTO> {
        return ResponseEntity.ok(eventService.allocateVenueForEvent(id))
    }

    @GetMapping("/{id}/summary")
    fun getEventSummary(@PathVariable id: Long): ResponseEntity<EventSummaryDTO> {
        return ResponseEntity.ok(eventService.getEventSummary(id))
    }
}