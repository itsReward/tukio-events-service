package com.tukio.tukioeventsservice.dto

import java.time.LocalDateTime

data class VenueAllocationRequest(
    val eventId: Long,
    val eventName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val attendeeCount: Int,
    val requiredAmenities: List<String>?,
    val preferredVenueType: String?,
    val preferredLocation: String?,
    val notes: String?
)
