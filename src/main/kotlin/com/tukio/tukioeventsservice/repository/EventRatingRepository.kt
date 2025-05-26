package com.tukio.tukioeventsservice.repository

import com.tukio.tukioeventsservice.model.EventRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventRatingRepository : JpaRepository<EventRating, Long> {

    fun findByEventIdAndUserId(eventId: Long, userId: Long): EventRating?

    fun findByEventId(eventId: Long): List<EventRating>

    fun findByUserId(userId: Long): List<EventRating>

    @Query("SELECT AVG(r.rating) FROM EventRating r WHERE r.eventId = :eventId")
    fun getAverageRatingByEventId(@Param("eventId") eventId: Long): Double?

    @Query("SELECT COUNT(r) FROM EventRating r WHERE r.eventId = :eventId")
    fun countRatingsByEventId(@Param("eventId") eventId: Long): Long
}
