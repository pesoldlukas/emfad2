package com.emfad.app.Models

import java.time.LocalDateTime

data class MeasurementData(
    val id: String,
    val sessionId: String,
    val timestamp: LocalDateTime,
    val frequency: Double,
    val value: Double,
    val unit: String,
    val sensorType: String,
    val location: LocationData? = null,
    val notes: String? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Double? = null
)