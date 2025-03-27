package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.*

interface EventService {
    fun getAllEvents(): List<EventDTO>
    fun getEventById(id: Long): EventDTO
    fun createEvent(eventRequest: EventCreateRequest): EventDTO
    fun updateEvent(id: Long, eventRequest: EventUpdateRequest): EventDTO
    fun deleteEvent(id: Long)
    fun searchEvents(criteria: EventSearchCriteria): List<EventDTO>
    fun getUpcomingEvents(): List<EventDTO>
    fun getEventsByOrganizer(organizerId: Long): List<EventDTO>
    fun getEventsByCategory(categoryId: Long): List<EventDTO>
    fun allocateVenueForEvent(id: Long): AllocationResponseDTO
    fun getEventSummary(id: Long): EventSummaryDTO
}