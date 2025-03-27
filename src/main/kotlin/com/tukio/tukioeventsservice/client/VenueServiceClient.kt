package com.tukio.tukioeventsservice.client

import com.tukio.tukioeventsservice.dto.AllocationResponseDTO
import com.tukio.tukioeventsservice.dto.VenueAllocationRequest
import com.tukio.tukioeventsservice.service.VenueServiceClientFallback
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@FeignClient(name = "tukio-venue-service", fallback = VenueServiceClientFallback::class)
interface VenueServiceClient {

    @PostMapping("/api/venues/allocate")
    fun allocateVenue(@RequestBody request: VenueAllocationRequest): AllocationResponseDTO

    @GetMapping("/api/venues/{id}")
    fun getVenueById(@PathVariable("id") venueId: Long): VenueDTO

    @GetMapping("/api/venues/{id}/availability")
    fun checkVenueAvailability(
        @PathVariable("id") venueId: Long,
        @RequestParam("startTime") startTime: LocalDateTime,
        @RequestParam("endTime") endTime: LocalDateTime
    ): Map<String, Boolean>
}