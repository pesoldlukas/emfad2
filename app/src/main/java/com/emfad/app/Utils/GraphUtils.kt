package com.emfad.app.Utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emfad.app.Models.MeasurementData
import kotlin.math.max
import kotlin.math.min

object GraphUtils {

    /**
     * Calculates the path for a line graph given a list of measurement data.
     * @param measurements The list of MeasurementData points.
     * @param width The width of the drawing area.
     * @param height The height of the drawing area.
     * @param valueRange The range of values (min to max) for scaling the Y-axis.
     * @param frequencyRange The range of frequencies (min to max) for scaling the X-axis.
     * @return A Path object representing the graph line.
     */
    fun calculateLinePath(
        measurements: List<MeasurementData>,
        width: Float,
        height: Float,
        valueRange: Pair<Double, Double>,
        frequencyRange: Pair<Double, Double>
    ): Path {
        val path = Path()
        if (measurements.isEmpty()) return path

        val (minValue, maxValue) = valueRange
        val (minFreq, maxFreq) = frequencyRange

        // Sort measurements by frequency to ensure correct line drawing
        val sortedMeasurements = measurements.sortedBy { it.frequency }

        sortedMeasurements.forEachIndexed { index, data ->
            val x = ((data.frequency - minFreq) / (maxFreq - minFreq)).toFloat() * width
            val y = (1 - ((data.value - minValue) / (maxValue - minValue))).toFloat() * height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        return path
    }

    /**
     * Determines the appropriate Y-axis labels based on the value range.
     * @param valueRange The range of values (min to max).
     * @param numLabels The desired number of labels.
     * @return A list of Pair<Double, String> where Double is the value and String is its label.
     */
    fun getYAxisLabels(valueRange: Pair<Double, Double>, numLabels: Int = 5): List<Pair<Double, String>> {
        val (minValue, maxValue) = valueRange
        val labels = mutableListOf<Pair<Double, String>>()
        val step = (maxValue - minValue) / (numLabels - 1)

        for (i in 0 until numLabels) {
            val value = minValue + i * step
            labels.add(Pair(value, UIUtils.formatValue(value)))
        }
        return labels
    }

    /**
     * Determines the appropriate X-axis labels based on the frequency range.
     * @param frequencyRange The range of frequencies (min to max).
     * @param numLabels The desired number of labels.
     * @return A list of Pair<Double, String> where Double is the frequency and String is its label.
     */
    fun getXAxisLabels(frequencyRange: Pair<Double, Double>, numLabels: Int = 5): List<Pair<Double, String>> {
        val (minFreq, maxFreq) = frequencyRange
        val labels = mutableListOf<Pair<Double, String>>()
        val step = (maxFreq - minFreq) / (numLabels - 1)

        for (i in 0 until numLabels) {
            val frequency = minFreq + i * step
            labels.add(Pair(frequency, UIUtils.formatFrequency(frequency)))
        }
        return labels
    }

    /**
     * Calculates the min and max values for the Y-axis from a list of measurements.
     */
    fun calculateValueRange(measurements: List<MeasurementData>): Pair<Double, Double> {
        if (measurements.isEmpty()) return Pair(0.0, 1.0) // Default range
        val minValue = measurements.minOf { it.value }
        val maxValue = measurements.maxOf { it.value }
        return Pair(minValue, maxValue)
    }

    /**
     * Calculates the min and max frequencies for the X-axis from a list of measurements.
     */
    fun calculateFrequencyRange(measurements: List<MeasurementData>): Pair<Double, Double> {
        if (measurements.isEmpty()) return Pair(0.0, 1.0) // Default range
        val minFreq = measurements.minOf { it.frequency }
        val maxFreq = measurements.maxOf { it.frequency }
        return Pair(minFreq, maxFreq)
    }
}