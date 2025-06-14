package com.emfad.app.Utils

import com.emfad.app.Models.MeasurementData
import com.emfad.app.Models.MeasurementSession
import com.emfad.app.Models.MeasurementSettings
import com.emfad.app.Models.MaterialProperties
import com.emfad.app.Models.MeasurementMode
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.complex.Complex
import java.time.LocalDateTime
import java.util.UUID

object DataProcessor {

    /**
     * Processes raw measurement data into a structured MeasurementData object.
     * This function can be extended to include more complex parsing logic.
     */
    fun processRawData(
        sessionId: String,
        frequency: Double,
        value: Double,
        unit: String,
        sensorType: String,
        latitude: Double? = null,
        longitude: Double? = null,
        altitude: Double? = null,
        accuracy: Double? = null,
        notes: String? = null
    ): MeasurementData {
        val id = UUID.randomUUID().toString()
        val timestamp = LocalDateTime.now()
        val location = if (latitude != null && longitude != null) {
            com.emfad.app.Models.LocationData(latitude, longitude, altitude, accuracy)
        } else null

        return MeasurementData(
            id = id,
            sessionId = sessionId,
            timestamp = timestamp,
            frequency = frequency,
            value = value,
            unit = unit,
            sensorType = sensorType,
            location = location,
            notes = notes
        )
    }

    /**
     * Analyzes a list of MeasurementData to extract key statistics.
     */
    fun analyzeMeasurements(measurements: List<MeasurementData>): Map<String, Double> {
        val values = measurements.map { it.value }.toDoubleArray()
        val frequencies = measurements.map { it.frequency }.toDoubleArray()

        val stats = DescriptiveStatistics(values)
        val freqStats = DescriptiveStatistics(frequencies)

        return mapOf(
            "mean_value" to stats.mean,
            "median_value" to stats.getPercentile(50.0),
            "std_dev_value" to stats.standardDeviation,
            "min_value" to stats.min,
            "max_value" to stats.max,
            "mean_frequency" to freqStats.mean,
            "min_frequency" to freqStats.min,
            "max_frequency" to freqStats.max
        )
    }

    /**
     * Performs Fast Fourier Transform on a given signal.
     * Assumes the signal is real-valued.
     */
    fun performFFT(signal: DoubleArray): Array<Complex> {
        val fft = FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD)
        return fft.transform(signal, org.apache.commons.math3.transform.TransformType.FORWARD)
    }

    /**
     * Calculates the magnitude spectrum from FFT results.
     */
    fun calculateMagnitudeSpectrum(fftResult: Array<Complex>): DoubleArray {
        return fftResult.map { it.abs() }.toDoubleArray()
    }

    /**
     * Creates a new measurement session.
     */
    fun createMeasurementSession(
        mode: MeasurementMode,
        settings: MeasurementSettings,
        materialProperties: MaterialProperties? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        altitude: Double? = null,
        accuracy: Double? = null,
        notes: String? = null
    ): MeasurementSession {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now()
        val location = if (latitude != null && longitude != null) {
            com.emfad.app.Models.LocationData(latitude, longitude, altitude, accuracy)
        } else null

        return MeasurementSession(
            id = sessionId,
            startTime = startTime,
            mode = mode,
            settings = settings,
            materialProperties = materialProperties,
            location = location,
            notes = notes
        )
    }

    /**
     * Adds a measurement to an existing session.
     */
    fun addMeasurementToSession(session: MeasurementSession, measurement: MeasurementData) {
        session.measurements.add(measurement)
    }

    /**
     * Finalizes a measurement session by setting the end time.
     */
    fun finalizeMeasurementSession(session: MeasurementSession) {
        session.endTime = LocalDateTime.now()
    }

    /**
     * Generates a summary report for a completed measurement session.
     */
    fun generateSessionReport(session: MeasurementSession): String {
        val report = StringBuilder()
        report.append("--- Measurement Session Report ---\n")
        report.append("Session ID: ${session.id}\n")
        report.append("Start Time: ${session.startTime}\n")
        report.append("End Time: ${session.endTime ?: "N/A"}\n")
        report.append("Measurement Mode: ${session.mode.displayName}\n")
        report.append("Total Measurements: ${session.measurements.size}\n")

        if (session.measurements.isNotEmpty()) {
            val analysis = analyzeMeasurements(session.measurements)
            report.append("\n--- Measurement Statistics ---\n")
            analysis.forEach { (key, value) ->
                report.append("${key.replace("_", " ").capitalize()}: %.4f\n".format(value))
            }
        }

        session.location?.let {
            report.append("\n--- Location Data ---\n")
            report.append("Latitude: ${it.latitude}\n")
            report.append("Longitude: ${it.longitude}\n")
            it.altitude?.let { alt -> report.append("Altitude: $alt\n") }
            it.accuracy?.let { acc -> report.append("Accuracy: $acc\n") }
        }

        session.notes?.let { report.append("\nNotes: $it\n") }

        report.append("-----------------------------------\n")
        return report.toString()
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}