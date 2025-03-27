package com.tukio.tukioeventsservice.controller

import com.tukio.tukioeventsservice.dto.EventCategoryCreateRequest
import com.tukio.tukioeventsservice.dto.EventCategoryDTO
import com.tukio.tukioeventsservice.service.EventCategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/event-categories")
class EventCategoryController(private val eventCategoryService: EventCategoryService) {

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<EventCategoryDTO>> {
        return ResponseEntity.ok(eventCategoryService.getAllCategories())
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<EventCategoryDTO> {
        return ResponseEntity.ok(eventCategoryService.getCategoryById(id))
    }

    @PostMapping
    fun createCategory(@RequestBody categoryRequest: EventCategoryCreateRequest): ResponseEntity<EventCategoryDTO> {
        val createdCategory = eventCategoryService.createCategory(categoryRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory)
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @RequestBody categoryRequest: EventCategoryCreateRequest
    ): ResponseEntity<EventCategoryDTO> {
        return ResponseEntity.ok(eventCategoryService.updateCategory(id, categoryRequest))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        eventCategoryService.deleteCategory(id)
        return ResponseEntity.noContent().build()
    }
}