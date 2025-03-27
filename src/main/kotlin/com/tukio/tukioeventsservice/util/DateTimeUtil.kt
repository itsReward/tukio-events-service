package com.tukio.tukioeventsservice.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utility class for handling date and time operations
 */
object DateTimeUtil {

    private val DEFAULT_FORMAT = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Format LocalDateTime to ISO string
     */
    fun formatToISOString(dateTime: LocalDateTime): String {
        return dateTime.format(DEFAULT_FORMAT)
    }

    /**
     * Parse ISO string to LocalDateTime
     */
    fun parseFromISOString(dateTimeStr: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMAT)
    }

    /**
     * Checks if two time periods overlap
     */
    fun isOverlapping(
        startTime1: LocalDateTime,
        endTime1: LocalDateTime,
        startTime2: LocalDateTime,
        endTime2: LocalDateTime
    ): Boolean {
        return startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2)
    }

    /**
     * Calculates the duration between two timestamps in minutes
     */
    fun durationInMinutes(startTime: LocalDateTime, endTime: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(startTime, endTime)
    }

    /**
     * Calculates the duration between two timestamps in hours
     */
    fun durationInHours(startTime: LocalDateTime, endTime: LocalDateTime): Double {
        val minutes = durationInMinutes(startTime, endTime)
        return minutes / 60.0
    }

    /**
     * Gets the epoch time in seconds for a LocalDateTime
     */
    fun getEpochSeconds(dateTime: LocalDateTime): Long {
        return dateTime.toEpochSecond(ZoneOffset.UTC)
    }

    /**
     * Checks if a date is today
     */
    fun isToday(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        return dateTime.toLocalDate() == now.toLocalDate()
    }

    /**
     * Checks if a date is in the future
     */
    fun isFuture(dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(LocalDateTime.now())
    }

    /**
     * Checks if a date is in the past
     */
    fun isPast(dateTime: LocalDateTime): Boolean {
        return dateTime.isBefore(LocalDateTime.now())
    }

    /**
     * Get start of day for a given date
     */
    fun startOfDay(dateTime: LocalDateTime): LocalDateTime {
        return dateTime.toLocalDate().atStartOfDay()
    }

    /**
     * Get end of day for a given date
     */
    fun endOfDay(dateTime: LocalDateTime): LocalDateTime {
        return dateTime.toLocalDate().atTime(23, 59, 59)
    }
}