
package com.emfad.app.Models

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

object MaterialPhysicsAnalyzer {

    /**
     * Analyzes a measurement to determine if it indicates a crystalline material.
     * This is based on low conductivity, specific permittivity range, and signal characteristics.
     */
    fun analyzeCrystallineMaterial(measurement: MeasurementData, environmentConductivity: Double): Boolean {
        // Criteria for crystalline non-conductors:
        // - Very low conductivity (e.g., < 10^-8 S/m)
        // - Permittivity within a typical range for crystals (e.g., 4-15)
        // - Signal characteristics (e.g., sharp, localized impedance change) - simplified here

        val isLowConductivity = measurement.value < 1e-8 // Assuming 'value' represents conductivity or related
        val isTypicalPermittivity = measurement.value >= 4.0 && measurement.value <= 15.0 // Placeholder

        // More sophisticated analysis would involve frequency response, Q-factor, etc.
        return isLowConductivity && isTypicalPermittivity
    }

    /**
     * Distinguishes between natural veins and artificial structures based on geometric and physical properties.
     */
    fun distinguishVeinVsStructure(
        aspectRatio: Double,
        symmetry: Double,
        conductivity: Double,
        depth: Double
    ): AnalysisResult {
        var score = 0.0
        var type = "Unknown"

        // Criteria for Natural Veins
        if (aspectRatio > 10 && symmetry < 0.3 && conductivity > 1e4 && depth > 1.5) {
            score += 0.5 // Initial strong indicator
            type = "Natural Vein"
        }

        // Criteria for Artificial Structures
        if (aspectRatio < 3 && symmetry > 0.7 && (conductivity < 1e-9 || conductivity > 1e6) && depth < 10) {
            score += 0.5 // Initial strong indicator
            type = "Artificial Structure"
        }

        // Refine score based on individual criteria
        // Conductivity
        if (conductivity > 1e4) score += 0.2 // High conductivity for veins
        if (conductivity < 1e-9 || conductivity > 1e6) score += 0.2 // Extreme conductivity for structures

        // Aspect Ratio
        if (aspectRatio > 10) score += 0.1
        if (aspectRatio < 3) score += 0.1

        // Symmetry
        if (symmetry < 0.3) score += 0.1
        if (symmetry > 0.7) score += 0.1

        // Depth
        if (depth > 1.5) score += 0.1
        if (depth < 10) score += 0.1

        // Normalize score to 0-1 range (simple normalization, can be improved)
        score = score.coerceIn(0.0, 1.0)

        return AnalysisResult(type, score)
    }

    /**
     * Calculates the reliability score for a given analysis result.
     * The weights are simplified for this example.
     */
    fun calculateReliability(analysisResult: AnalysisResult): Double {
        // This is a placeholder. In a real scenario, this would be a more complex calculation
        // based on multiple input parameters and their uncertainties.
        return analysisResult.score
    }

    /**
     * Simulates a multi-layer model for inclusion detection.
     * This is a highly simplified model.
     */
    fun analyzeMultiLayerInclusion(layerProperties: List<MaterialProperties>): Boolean {
        // Example: Detect a high-conductivity object (inclusion) within a low-conductivity layer (cavity)
        if (layerProperties.size < 2) return false

        val cavityLayer = layerProperties.first()
        val inclusionLayer = layerProperties.last()

        val isCavity = cavityLayer.conductivity < 1e-9 && cavityLayer.permittivity < 2.0 // Air-like
        val isInclusion = inclusionLayer.conductivity > 1e3 || inclusionLayer.permittivity > 50.0 // Metal/dense material

        return isCavity && isInclusion
    }

    /**
     * Distinguishes between precious metals and alloys based on frequency response (simplified).
     * This would typically involve analyzing impedance vs. frequency curves.
     */
    fun distinguishPreciousMetalVsAlloy(frequencyResponse: Map<Double, Complex>): Boolean {
        // Simplified: Precious metals often have a flatter impedance response across frequencies
        // and very high conductivity. Alloys might show more dispersion or lower overall conductivity.

        if (frequencyResponse.isEmpty()) return false

        val impedances = frequencyResponse.values.map { it.abs() }
        val stats = DescriptiveStatistics()
        impedances.forEach { stats.addValue(it) }

        val meanImpedance = stats.mean
        val stdDevImpedance = stats.standardDeviation

        // A very low standard deviation relative to the mean might indicate a pure metal
        val cv = if (meanImpedance != 0.0) stdDevImpedance / meanImpedance else 0.0

        // Thresholds are illustrative and would need empirical tuning
        val isHighConductivity = frequencyResponse.any { it.value.real > 1e6 } // Placeholder for high conductivity
        val isLowDispersion = cv < 0.05 // Coefficient of variation

        return isHighConductivity && isLowDispersion
    }

    /**
     * Calculates the skin depth for a given material and frequency.
     * @param conductivity Conductivity of the material in S/m.
     * @param permeability Relative permeability of the material (unitless).
     * @param frequency Frequency of the electromagnetic wave in Hz.
     * @return Skin depth in meters.
     */
    fun calculateSkinDepth(conductivity: Double, permeability: Double, frequency: Double): Double {
        if (conductivity <= 0 || frequency <= 0) return Double.POSITIVE_INFINITY
        val mu0 = 4 * Math.PI * 1e-7 // Permeability of free space
        val mu = permeability * mu0
        return sqrt(2 / (mu * conductivity * 2 * Math.PI * frequency))
    }

    /**
     * Placeholder for a more complex impedance calculation.
     */
    fun calculateComplexImpedance(frequency: Double, material: MaterialProperties): Complex {
        // This is a highly simplified model. Real impedance calculation depends on geometry,
        // measurement setup, and full electromagnetic properties.
        val resistance = 1.0 / (material.conductivity * 100) // Simplified resistance
        val reactance = 2 * Math.PI * frequency * material.permittivity * 1e-12 // Simplified reactance (capacitive)
        return Complex(resistance, reactance)
    }

    /**
     * Placeholder for analyzing magnetic field gradients.
     */
    fun analyzeMagneticFieldGradient(gradientData: List<Double>): Boolean {
        // Simplified: Check for significant changes or patterns in gradient data
        val stats = DescriptiveStatistics()
        gradientData.forEach { stats.addValue(it) }

        // If the standard deviation is high, it indicates a non-uniform field, potentially an anomaly
        return stats.standardDeviation > 0.1
    }

    /**
     * Placeholder for layer analysis based on depth profiles.
     */
    fun analyzeLayerProfile(depthProfile: Map<Double, MaterialProperties>): List<String> {
        val layers = mutableListOf<String>()
        var lastDepth = 0.0
        depthProfile.toSortedMap().forEach { (depth, material) ->
            layers.add("Layer from ${lastDepth}m to ${depth}m: ${material.name}")
            lastDepth = depth
        }
        return layers
    }
}

data class AnalysisResult(
    val type: String,
    val score: Double // 0-1, higher is more confident
)

