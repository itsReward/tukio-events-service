package com.tukio.tukioeventsservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event_registrations")
data class EventRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val userName: String,

    @Column(nullable = false)
    val userEmail: String,

    @Column(nullable = false)
    var status: String = "REGISTERED", // REGISTERED, ATTENDED, CANCELLED

    @Column(nullable = true)
    var checkInTime: LocalDateTime? = null,

    @Column(nullable = true)
    var feedback: String? = null,

    @Column(nullable = true)
    var rating: Int? = null,

    @Column(nullable = false)
    val registrationTime: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)