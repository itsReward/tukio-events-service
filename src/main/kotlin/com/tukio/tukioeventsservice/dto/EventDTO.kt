package com.tukio.tukioeventsservice.dto

import com.tukio.tukioeventsservice.model.EventStatus
import java.time.LocalDateTime

data class EventDTO(
    val id: Long?,
    val title: String,
    val description: String,
    val categoryId: Long,
    val categoryName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String,
    val venueId: Long?,
    val venueName: String?,
    val maxParticipants: Int,
    val organizer: String,
    val organizerId: Long,
    val imageUrl: String?,
    val tags: Set<String>,
    val status: EventStatus,
    val currentRegistrations: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)