package com.tukio.tukioeventsservice.dto

import com.tukio.tukioeventsservice.model.EventStatus
import java.time.LocalDateTime

data class EventSearchCriteria(
    val categoryId: Long? = null,
    val keyword: String? = null,
    val startFrom: LocalDateTime? = null,
    val startTo: LocalDateTime? = null,
    val tags: List<String>? = null,
    val status: EventStatus? = null
)