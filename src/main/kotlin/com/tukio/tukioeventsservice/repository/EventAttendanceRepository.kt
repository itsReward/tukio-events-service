package com.tukio.tukioeventsservice.repository

import com.tukio.tukioeventsservice.model.EventAttendance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventAttendanceRepository : JpaRepository<EventAttendance, Long> {

    fun findByEventIdAndUserId(eventId: Long, userId: Long): EventAttendance?

    fun findByEventId(eventId: Long): List<EventAttendance>

    fun findByUserId(userId: Long): List<EventAttendance>

    @Query("SELECT COUNT(a) FROM EventAttendance a WHERE a.eventId = :eventId AND a.attended = true")
    fun countAttendeesByEventId(@Param("eventId") eventId: Long): Long

    @Query("SELECT a FROM EventAttendance a WHERE a.userId = :userId AND a.attended = true")
    fun findAttendedEventsByUserId(@Param("userId") userId: Long): List<EventAttendance>
}