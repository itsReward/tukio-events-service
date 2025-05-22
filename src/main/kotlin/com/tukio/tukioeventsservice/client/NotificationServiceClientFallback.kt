package com.tukio.tukioeventsservice.client

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class NotificationServiceClientFallback : NotificationServiceClient {

    private val logger = LoggerFactory.getLogger(NotificationServiceClientFallback::class.java)

    override fun sendNotification(request: NotificationRequestDTO): ResponseEntity<List<NotificationResponseDTO>> {
        logger.warn("Fallback: sendNotification for user ${request.userId}")
        return ResponseEntity.ok(emptyList())
    }

    override fun sendBatchNotification(request: BatchNotificationRequestDTO): ResponseEntity<Int> {
        logger.warn("Fallback: sendBatchNotification for ${request.userIds.size} users")
        return ResponseEntity.ok(0)
    }
}