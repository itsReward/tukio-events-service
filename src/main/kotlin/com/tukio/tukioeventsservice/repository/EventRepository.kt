package com.tukio.tukioeventsservice.repository

import com.tukio.tukioeventsservice.model.Event
import com.tukio.tukioeventsservice.model.EventStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    fun findByOrganizerId(organizerId: Long): List<Event>

    fun findByCategoryId(categoryId: Long): List<Event>

    fun findByStatus(status: EventStatus): List<Event>

    fun findByStartTimeBetween(start: LocalDateTime, end: LocalDateTime): List<Event>

    fun findByVenueId(venueId: Long): List<Event>

    @Query("""
        SELECT e FROM Event e
        WHERE e.status = :status
        AND e.startTime >= :now
        ORDER BY e.startTime ASC
    """)
    fun findUpcomingEvents(
        @Param("status") status: EventStatus,
        @Param("now") now: LocalDateTime
    ): List<Event>

    @Query("""
    SELECT e FROM Event e
    WHERE (:categoryId IS NULL OR e.category.id = :categoryId)
    AND (:keyword IS NULL 
        OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (CAST(:startFrom as timestamp) IS NULL OR e.startTime >= :startFrom)
    AND (CAST(:startTo as timestamp) IS NULL OR e.startTime <= :startTo)
    AND (:status IS NULL OR e.status = :status)
""")
    fun findBySearchCriteria(
        @Param("categoryId") categoryId: Long?,
        @Param("keyword") keyword: String?,
        @Param("startFrom") startFrom: LocalDateTime?,
        @Param("startTo") startTo: LocalDateTime?,
        @Param("status") status: EventStatus?
    ): List<Event>

    @Query("""
        SELECT e FROM Event e
        JOIN e.tags t
        WHERE t IN :tags
        GROUP BY e.id
        HAVING COUNT(DISTINCT t) = :tagCount
    """)
    fun findByAllTagsMatching(
        @Param("tags") tags: List<String>,
        @Param("tagCount") tagCount: Long
    ): List<Event>

    @Query("""
        SELECT COUNT(e) FROM Event e
        WHERE e.category.id = :categoryId
    """)
    fun countByCategoryId(@Param("categoryId") categoryId: Long): Long
}