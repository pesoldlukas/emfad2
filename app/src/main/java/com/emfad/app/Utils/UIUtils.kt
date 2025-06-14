package com.emfad.app.Utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emfad.app.Models.MeasurementMode

object UIUtils {

    /**
     * Provides a color based on the measurement mode for UI representation.
     */
    @Composable
    fun getModeColor(mode: MeasurementMode): Color {
        return when (mode) {
            MeasurementMode.BA_VERTICAL -> Color(0xFF4CAF50) // Green
            MeasurementMode.AB_HORIZONTAL -> Color(0xFF2196F3) // Blue
            MeasurementMode.ANTENNA_A -> Color(0xFFFFC107) // Amber
            MeasurementMode.DEPTH_PRO -> Color(0xFF9C27B0) // Purple
            // Add more modes as needed
        }
    }

    /**
     * Formats a double value to a specified number of decimal places.
     */
    fun formatValue(value: Double, decimalPlaces: Int = 2): String {
        return "%.${decimalPlaces}f".format(value)
    }

    /**
     * Provides a human-readable string for a given frequency.
     */
    fun formatFrequency(frequency: Double): String {
        return when {
            frequency >= 1_000_000_000 -> "%.2f GHz".format(frequency / 1_000_000_000)
            frequency >= 1_000_000 -> "%.2f MHz".format(frequency / 1_000_000)
            frequency >= 1_000 -> "%.2f kHz".format(frequency / 1_000)
            else -> "%.2f Hz".format(frequency)
        }
    }

    /**
     * Provides a human-readable string for a given depth.
     */
    fun formatDepth(depth: Double): String {
        return "%.2f m".format(depth)
    }
}