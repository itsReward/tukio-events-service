package com.tukio.tukioeventsservice.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "tukio-notification-service", fallback = NotificationServiceClientFallback::class)
interface NotificationServiceClient {

    @PostMapping("/api/notifications")
    fun sendNotification(@RequestBody request: NotificationRequestDTO): ResponseEntity<List<NotificationResponseDTO>>

    @PostMapping("/api/notifications/batch")
    fun sendBatchNotification(@RequestBody request: BatchNotificationRequestDTO): ResponseEntity<Int>
}

data class NotificationRequestDTO(
    val userId: Long,
    val templateKey: String,
    val templateData: Map<String, String>,
    val channels: List<String> = listOf("EMAIL", "IN_APP"),
    val notificationType: String,
    val referenceId: String? = null,
    val referenceType: String? = null
)

data class BatchNotificationRequestDTO(
    val userIds: List<Long>,
    val templateKey: String,
    val templateData: Map<String, String>,
    val channels: List<String> = listOf("EMAIL", "IN_APP"),
    val notificationType: String
)

data class NotificationResponseDTO(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val notificationType: String,
    val channel: String,
    val status: String,
    val createdAt: String
)