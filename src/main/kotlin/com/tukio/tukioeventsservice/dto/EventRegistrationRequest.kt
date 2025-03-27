package com.tukio.tukioeventsservice.dto

data class EventRegistrationRequest(
    val eventId: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String
)