package com.tukio.tukioeventsservice.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class EventAttendanceRequest(
    @field:NotNull(message = "Attended status is required")
    val attended: Boolean
)

data class EventAttendanceResponse(
    val id: Long,
    val eventId: Long,
    val userId: Long,
    val attended: Boolean,
    val recordedAt: LocalDateTime,
    val canRate: Boolean // Whether user can now rate this event
)

data class EventRatingRequest(
    @field:Min(value = 1, message = "Rating must be at least 1")
    @field:Max(value = 5, message = "Rating must be at most 5")
    val rating: Int,

    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String? = null,

    val categories: Map<String, @Min(1) @Max(5) Int>? = null
)

data class EventRatingResponse(
    val id: Long,
    val eventId: Long,
    val userId: Long,
    val rating: Int,
    val comment: String?,
    val categories: Map<String, Int>?,
    val ratedAt: LocalDateTime
)

data class EventRatingSummaryDTO(
    val eventId: Long,
    val averageRating: Double,
    val totalRatings: Int,
    val ratingDistribution: Map<Int, Int>, // star count -> number of ratings
    val categoryAverages: Map<String, Double>?
)
