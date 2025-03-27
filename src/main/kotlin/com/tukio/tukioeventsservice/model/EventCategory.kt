package com.tukio.tukioeventsservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event_categories")
data class EventCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = true)
    var description: String? = null,

    @Column(nullable = true)
    var color: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)