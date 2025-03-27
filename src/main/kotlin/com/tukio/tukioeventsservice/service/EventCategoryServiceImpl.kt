package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.dto.EventCategoryCreateRequest
import com.tukio.tukioeventsservice.dto.EventCategoryDTO
import com.tukio.tukioeventsservice.exception.ResourceNotFoundException
import com.tukio.tukioeventsservice.model.EventCategory
import com.tukio.tukioeventsservice.repository.EventCategoryRepository
import com.tukio.tukioeventsservice.repository.EventRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventCategoryServiceImpl(
    private val eventCategoryRepository: EventCategoryRepository,
    private val eventRepository: EventRepository
) : EventCategoryService {

    private val logger = LoggerFactory.getLogger(EventCategoryServiceImpl::class.java)

    override fun getAllCategories(): List<EventCategoryDTO> {
        return eventCategoryRepository.findAllWithEventCount().map { result ->
            val category = result[0] as EventCategory
            val eventCount = (result[1] as Long).toInt()
            category.toDTO(eventCount)
        }
    }

    override fun getCategoryById(id: Long): EventCategoryDTO {
        val category = eventCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Category not found with id: $id") }

        val eventCount = eventRepository.countByCategoryId(id).toInt()
        return category.toDTO(eventCount)
    }

    @Transactional
    override fun createCategory(categoryRequest: EventCategoryCreateRequest): EventCategoryDTO {
        val existingCategory = eventCategoryRepository.findByNameIgnoreCase(categoryRequest.name)
        if (existingCategory != null) {
            logger.warn("Attempted to create a category with name that already exists: ${categoryRequest.name}")
            throw IllegalArgumentException("Category with name '${categoryRequest.name}' already exists")
        }

        val category = EventCategory(
            name = categoryRequest.name,
            description = categoryRequest.description,
            color = categoryRequest.color
        )

        val savedCategory = eventCategoryRepository.save(category)
        logger.info("Created new category: ${savedCategory.id} - ${savedCategory.name}")

        return savedCategory.toDTO(0)
    }

    @Transactional
    override fun updateCategory(id: Long, categoryRequest: EventCategoryCreateRequest): EventCategoryDTO {
        val category = eventCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Category not found with id: $id") }

        // Check if name is being changed and if it conflicts with an existing category
        if (categoryRequest.name != category.name) {
            val existingCategory = eventCategoryRepository.findByNameIgnoreCase(categoryRequest.name)
            if (existingCategory != null && existingCategory.id != id) {
                throw IllegalArgumentException("Category with name '${categoryRequest.name}' already exists")
            }
        }

        category.name = categoryRequest.name
        category.description = categoryRequest.description
        category.color = categoryRequest.color
        category.updatedAt = LocalDateTime.now()

        val updatedCategory = eventCategoryRepository.save(category)
        logger.info("Updated category: ${updatedCategory.id} - ${updatedCategory.name}")

        val eventCount = eventRepository.countByCategoryId(id).toInt()
        return updatedCategory.toDTO(eventCount)
    }

    @Transactional
    override fun deleteCategory(id: Long) {
        if (!eventCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("Category not found with id: $id")
        }

        val eventCount = eventRepository.countByCategoryId(id)
        if (eventCount > 0) {
            throw IllegalStateException("Cannot delete category with id: $id as it has $eventCount associated events")
        }

        eventCategoryRepository.deleteById(id)
        logger.info("Deleted category with id: $id")
    }

    // Extension function to convert Entity to DTO
    private fun EventCategory.toDTO(eventCount: Int? = null): EventCategoryDTO {
        return EventCategoryDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            color = this.color,
            eventCount = eventCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}