package com.tukio.tukioeventsservice.repository

import com.tukio.tukioeventsservice.model.EventCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EventCategoryRepository : JpaRepository<EventCategory, Long> {
    fun findByNameIgnoreCase(name: String): EventCategory?

    @Query("""
        SELECT c, COUNT(e) 
        FROM EventCategory c 
        LEFT JOIN Event e ON c.id = e.category.id 
        GROUP BY c.id
    """)
    fun findAllWithEventCount(): List<Array<Any>>
}
