package com.tukio.tukioeventsservice.client

// Data class to receive venue details
data class VenueDTO(
    val id: Long,
    val name: String,
    val location: String,
    val capacity: Int,
    val type: String,
    val description: String?,
    val availabilityStatus: Boolean,
    val amenities: List<String>
)