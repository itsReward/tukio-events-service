package com.tukio.tukioeventsservice.repository

import com.tukio.tukioeventsservice.model.EventRegistration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRegistrationRepository : JpaRepository<EventRegistration, Long> {
    fun findByEventId(eventId: Long): List<EventRegistration>

    fun findByUserId(userId: Long): List<EventRegistration>

    fun findByEventIdAndUserId(eventId: Long, userId: Long): EventRegistration?

    fun countByEventId(eventId: Long): Int

    fun countByEventIdAndStatus(eventId: Long, status: String): Int

    @Query("""
        SELECT r FROM EventRegistration r
        JOIN FETCH r.event e
        WHERE r.userId = :userId
        AND e.startTime >= :now
        ORDER BY e.startTime ASC
    """)
    fun findUpcomingRegistrationsByUserId(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime
    ): List<EventRegistration>

    @Query("""
        SELECT r FROM EventRegistration r
        JOIN FETCH r.event e
        WHERE r.userId = :userId
        AND e.endTime < :now
        ORDER BY e.startTime DESC
    """)
    fun findPastRegistrationsByUserId(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime
    ): List<EventRegistration>
}