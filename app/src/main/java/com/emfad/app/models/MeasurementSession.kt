package com.emfad.app.Models

import java.time.LocalDateTime

data class MeasurementSession(
    val id: String,
    val startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    val mode: MeasurementMode,
    val settings: MeasurementSettings,
    val materialProperties: MaterialProperties? = null,
    val location: LocationData? = null,
    val notes: String? = null,
    val measurements: MutableList<MeasurementData> = mutableListOf()
)