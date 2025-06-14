package com.emfad.app.Models

data class MeasurementSettings(
    val frequencyRange: String,
    val antennaConfiguration: String,
    val depthResolution: String,
    val maxDepth: Double,
    val powerLevel: String,
    val gainSetting: String,
    val filterSettings: String,
    val temperatureCompensation: Boolean,
    val environmentalCorrection: Boolean,
    val calibrationDate: String,
    val lastServiceDate: String
)