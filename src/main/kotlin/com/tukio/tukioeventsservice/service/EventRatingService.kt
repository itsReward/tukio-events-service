package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.*
import com.tukio.tukioeventsservice.model.EventRating
import com.tukio.tukioeventsservice.repository.EventRatingRepository
import com.tukio.tukioeventsservice.repository.EventRepository
import com.tukio.tukioeventsservice.repository.EventAttendanceRepository
import com.tukio.tukioeventsservice.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class EventRatingService(
    private val ratingRepository: EventRatingRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: EventAttendanceRepository
) {

    private val logger = LoggerFactory.getLogger(EventRatingService::class.java)

    fun rateEvent(eventId: Long, userId: Long, request: EventRatingRequest): EventRatingResponse {
        // Validate event exists
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }

        // Business Rule: Can only rate if event has ended
        val now = LocalDateTime.now()
        if (now.isBefore(event.endTime)) {
            throw IllegalStateException("Cannot rate event before it ends")
        }

        // Business Rule: Can only rate if user attended the event
        val attendance = attendanceRepository.findByEventIdAndUserId(eventId, userId)
        if (attendance == null || !attendance.attended) {
            throw IllegalStateException("Can only rate events you have attended")
        }

        // Validate rating range
        if (request.rating !in 1..5) {
            throw IllegalArgumentException("Rating must be between 1 and 5")
        }

        // Validate category ratings if provided
        request.categories?.values?.forEach { categoryRating ->
            if (categoryRating !in 1..5) {
                throw IllegalArgumentException("Category ratings must be between 1 and 5")
            }
        }

        // Check if user already rated this event
        val existingRating = ratingRepository.findByEventIdAndUserId(eventId, userId)

        val rating = if (existingRating != null) {
            // Update existing rating
            existingRating.copy(
                rating = request.rating,
                comment = request.comment,
                categories = request.categories,
                ratedAt = LocalDateTime.now()
            ).also { ratingRepository.save(it) }
        } else {
            // Create new rating
            val newRating = EventRating(
                eventId = eventId,
                userId = userId,
                rating = request.rating,
                comment = request.comment,
                categories = request.categories,
                ratedAt = LocalDateTime.now()
            )
            ratingRepository.save(newRating)
        }

        logger.info("User $userId rated event $eventId with ${request.rating} stars")

        return EventRatingResponse(
            id = rating.id,
            eventId = rating.eventId,
            userId = rating.userId,
            rating = rating.rating,
            comment = rating.comment,
            categories = rating.categories,
            ratedAt = rating.ratedAt
        )
    }

    fun getUserRatingForEvent(eventId: Long, userId: Long): EventRatingResponse? {
        val rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
            ?: return null

        return EventRatingResponse(
            id = rating.id,
            eventId = rating.eventId,
            userId = rating.userId,
            rating = rating.rating,
            comment = rating.comment,
            categories = rating.categories,
            ratedAt = rating.ratedAt
        )
    }

    fun getEventRatings(eventId: Long): List<EventRatingResponse> {
        val ratings = ratingRepository.findByEventId(eventId)
        return ratings.map { rating ->
            EventRatingResponse(
                id = rating.id,
                eventId = rating.eventId,
                userId = rating.userId,
                rating = rating.rating,
                comment = rating.comment,
                categories = rating.categories,
                ratedAt = rating.ratedAt
            )
        }
    }

    fun getEventRatingSummary(eventId: Long): EventRatingSummaryDTO {
        val ratings = ratingRepository.findByEventId(eventId)

        if (ratings.isEmpty()) {
            return EventRatingSummaryDTO(
                eventId = eventId,
                averageRating = 0.0,
                totalRatings = 0,
                ratingDistribution = emptyMap(),
                categoryAverages = null
            )
        }

        val averageRating = ratings.map { it.rating }.average()
        val ratingDistribution = ratings.groupingBy { it.rating }.eachCount()

        // Calculate category averages if any ratings have categories
        val categoryAverages = if (ratings.any { !it.categories.isNullOrEmpty() }) {
            val allCategories = ratings.flatMap { it.categories?.keys ?: emptySet() }.distinct()
            allCategories.associateWith { category ->
                ratings.mapNotNull { it.categories?.get(category) }.average()
            }
        } else null

        return EventRatingSummaryDTO(
            eventId = eventId,
            averageRating = averageRating,
            totalRatings = ratings.size,
            ratingDistribution = ratingDistribution,
            categoryAverages = categoryAverages
        )
    }
}