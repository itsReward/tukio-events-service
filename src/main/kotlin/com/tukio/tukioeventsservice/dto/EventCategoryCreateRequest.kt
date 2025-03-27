package com.tukio.tukioeventsservice.dto

data class EventCategoryCreateRequest(
    val name: String,
    val description: String?,
    val color: String?
)