package com.tukio.tukioeventsservice.dto

import com.tukio.tukioeventsservice.model.EventStatus
import java.time.LocalDateTime

data class EventSummaryDTO(
    val id: Long,
    val title: String,
    val categoryName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String,
    val organizer: String,
    val status: EventStatus,
    val registrationCount: Int,
    val maxParticipants: Int
)