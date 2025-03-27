package com.tukio.tukioeventsservice.dto

import java.time.LocalDateTime

data class EventRegistrationDTO(
    val id: Long?,
    val eventId: Long,
    val eventTitle: String?,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val status: String,
    val checkInTime: LocalDateTime?,
    val feedback: String?,
    val rating: Int?,
    val registrationTime: LocalDateTime?
)
