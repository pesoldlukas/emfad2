
enum class MeasurementMode(val displayName: String) {
    BA_VERTICAL("B-A Vertikal"),
    AB_HORIZONTAL("A-B Horizontal"),
    ANTENNA_A("Antenne A"),
    TIEFE_PRO("Tiefe Pro");

    companion object {
        fun fromDisplayName(displayName: String): MeasurementMode? {
            return values().find { it.displayName == displayName }
        }
    }
}
}

