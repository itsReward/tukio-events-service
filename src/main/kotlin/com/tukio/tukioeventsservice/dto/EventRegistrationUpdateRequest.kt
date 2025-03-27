package com.tukio.tukioeventsservice.dto

import java.time.LocalDateTime

data class EventRegistrationUpdateRequest(
    val status: String?,
    val checkInTime: LocalDateTime?,
    val feedback: String?,
    val rating: Int?
)
