package com.tukio.tukioeventsservice.service

import com.tukio.tukioeventsservice.client.VenueDTO
import com.tukio.tukioeventsservice.client.VenueServiceClient
import com.tukio.tukioeventsservice.dto.AllocationResponseDTO
import com.tukio.tukioeventsservice.dto.VenueAllocationRequest
import java.time.LocalDateTime

// Fallback implementation for circuit breaking
class VenueServiceClientFallback : VenueServiceClient {

    override fun allocateVenue(request: VenueAllocationRequest): AllocationResponseDTO {
        return AllocationResponseDTO(
            success = false,
            venueId = null,
            venueName = null,
            message = "Venue service is unavailable. Please try again later."
        )
    }

    override fun getVenueById(venueId: Long): VenueDTO {
        return VenueDTO(
            id = venueId,
            name = "Unknown Venue",
            location = "Unknown",
            capacity = 0,
            type = "UNKNOWN",
            description = "Venue details unavailable",
            availabilityStatus = false,
            amenities = emptyList()
        )
    }

    override fun checkVenueAvailability(
        venueId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Map<String, Boolean> {
        return mapOf("available" to false)
    }
}