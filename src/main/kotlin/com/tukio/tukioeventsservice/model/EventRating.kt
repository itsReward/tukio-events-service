package com.tukio.tukioeventsservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event_ratings")
data class EventRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val rating: Int, // 1-5 stars

    @Column(columnDefinition = "TEXT")
    val comment: String? = null,

    @ElementCollection
    @CollectionTable(name = "rating_categories", joinColumns = [JoinColumn(name = "rating_id")])
    @MapKeyColumn(name = "category_name")
    @Column(name = "category_rating")
    val categories: Map<String, Int>? = null,

    @Column(nullable = false)
    val ratedAt: LocalDateTime = LocalDateTime.now()
)