package com.tukio.tukioeventsservice.dto

import com.tukio.tukioeventsservice.model.EventStatus
import java.time.LocalDateTime

data class EventUpdateRequest(
    val title: String?,
    val description: String?,
    val categoryId: Long?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val location: String?,
    val venueId: Long?,
    val maxParticipants: Int?,
    val imageUrl: String?,
    val tags: Set<String>?,
    val status: EventStatus?
)