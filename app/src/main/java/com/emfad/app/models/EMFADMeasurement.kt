import java.util.UUID
import kotlin.math.sqrt

data class EMFADMeasurement(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val magneticField: Double, // µT
    val electricField: Double, // V/m
    val frequency: Double,    // Hz
    val mode: MeasurementMode,
    val orientation: MeasurementOrientation,
    val value: Double,        // Rohmesswert
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // Impedanzberechnung basierend auf E- und M-Feld
    val impedance: Complex by lazy {
        // Z = sqrt(µ/ε) * (1 + σ/(iωε)) für komplexe Impedanz
        val omega = 2 * Math.PI * frequency
        val mu = 1.0 // Luft als Referenz
        val epsilon = Complex(electricField, 0.0) // Vereinfachung für Beispiel
        val sigma = calculateConductivity() // Leitfähigkeit aus E-Feld
        
        val realPart = sqrt(mu / epsilon.real) * (1 - sigma / (omega * epsilon.imaginary))
        val imaginaryPart = sqrt(mu / epsilon.real) * (sigma / (omega * epsilon.imaginary))
        
        Complex(realPart, imaginaryPart)
    }

    // Leitfähigkeit aus E-Feld ableiten (vereinfachte Formel)
    private fun calculateConductivity(): Double {
        // σ ≈ E / (ρ * ω * ε₀) mit ρ = 1 für Luft
        val epsilon0 = 8.854e-12
        val omega = 2 * Math.PI * frequency
        return electricField / (omega * epsilon0)
    }
}

