package com.tukio.tukioeventsservice.dto

import java.time.LocalDateTime

data class EventCreateRequest(
    val title: String,
    val description: String,
    val categoryId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String,
    val venueId: Long?,
    val maxParticipants: Int,
    val organizer: String,
    val organizerId: Long,
    val imageUrl: String?,
    val tags: Set<String>?
)