package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.EventCategoryCreateRequest
import com.tukio.tukioeventsservice.dto.EventCategoryDTO

interface EventCategoryService {
    fun getAllCategories(): List<EventCategoryDTO>
    fun getCategoryById(id: Long): EventCategoryDTO
    fun createCategory(categoryRequest: EventCategoryCreateRequest): EventCategoryDTO
    fun updateCategory(id: Long, categoryRequest: EventCategoryCreateRequest): EventCategoryDTO
    fun deleteCategory(id: Long)
}