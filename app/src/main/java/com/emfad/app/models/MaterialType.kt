sealed class MaterialType(val name: String, val conductivity: Double, val permittivity: Double)

object MaterialType {
    // Metallische Materialien
    object FERROUS_METAL : MaterialType("Eisen", 1e6, 1.0)
    object NON_FERROUS_METAL : MaterialType("Nicht-Eisenmetall", 1e7, 1.0)
    
    // Kristalline Materialien
    object QUARTZ : MaterialType("Quarz", 1e-9, 4.5)
    object DIAMOND : MaterialType("Diamant", 1e-12, 5.5)
    object EMERALD : MaterialType("Smaragd", 1e-8, 7.0)
    
    // Hohlraum
    object CAVITY : MaterialType("Hohlraum", 1e-15, 1.0)
    
    // Unbekanntes Material
    object UNKNOWN : MaterialType("Unbekannt", 0.0, 0.0)
    
    // Liste aller Materialtypen
    val values = listOf(
        FERROUS_METAL,
        NON_FERROUS_METAL,
        QUARTZ,
        DIAMOND,
        EMERALD,
        CAVITY,
        UNKNOWN
    )
}

