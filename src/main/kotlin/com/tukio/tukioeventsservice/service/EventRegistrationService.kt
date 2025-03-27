package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.EventRegistrationDTO
import com.tukio.tukioeventsservice.dto.EventRegistrationRequest
import com.tukio.tukioeventsservice.dto.EventRegistrationUpdateRequest

interface EventRegistrationService {
    fun getAllRegistrations(): List<EventRegistrationDTO>
    fun getRegistrationById(id: Long): EventRegistrationDTO
    fun getRegistrationsByEventId(eventId: Long): List<EventRegistrationDTO>
    fun getRegistrationsByUserId(userId: Long): List<EventRegistrationDTO>
    fun getUserUpcomingEvents(userId: Long): List<EventRegistrationDTO>
    fun getUserPastEvents(userId: Long): List<EventRegistrationDTO>
    fun registerForEvent(registrationRequest: EventRegistrationRequest): EventRegistrationDTO
    fun updateRegistration(id: Long, updateRequest: EventRegistrationUpdateRequest): EventRegistrationDTO
    fun cancelRegistration(id: Long): EventRegistrationDTO
    fun checkInAttendee(eventId: Long, userId: Long): EventRegistrationDTO
    fun submitFeedback(eventId: Long, userId: Long, feedback: String, rating: Int?): EventRegistrationDTO
}