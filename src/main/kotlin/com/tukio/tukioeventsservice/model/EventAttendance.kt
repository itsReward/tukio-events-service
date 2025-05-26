package com.tukio.tukioeventsservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event_attendance")
data class EventAttendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val attended: Boolean, // true = attended, false = did not attend

    @Column(nullable = false)
    val recordedAt: LocalDateTime = LocalDateTime.now()
)