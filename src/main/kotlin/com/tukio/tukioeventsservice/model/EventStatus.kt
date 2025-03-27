package com.tukio.tukioeventsservice.model

enum class EventStatus {
    DRAFT,          // Event is being created, not visible to public
    SCHEDULED,      // Event is scheduled and visible
    RESCHEDULED,    // Event was rescheduled
    CANCELLED,      // Event was cancelled
    COMPLETED,      // Event has completed
    ONGOING         // Event is currently in progress
}