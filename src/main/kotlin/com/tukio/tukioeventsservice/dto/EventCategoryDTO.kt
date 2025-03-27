package com.tukio.tukioeventsservice.dto

import java.time.LocalDateTime

data class EventCategoryDTO(
    val id: Long?,
    val name: String,
    val description: String?,
    val color: String?,
    val eventCount: Int?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)