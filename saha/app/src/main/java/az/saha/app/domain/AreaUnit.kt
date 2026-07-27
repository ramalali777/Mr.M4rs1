package az.saha.app.domain

enum class AreaUnit(
    val label: String,
    val squareMetersPerUnit: Double
) {
    SQUARE_METER("m²", 1.0),
    SOT("sot", 100.0),
    PUT("put", 1000.0),
    HECTARE("ha", 10_000.0);

    fun fromSquareMeters(m2: Double): Double = m2 / squareMetersPerUnit

    fun format(m2: Double, decimals: Int = 2): String {
        val value = fromSquareMeters(m2)
        val patterned = if (this == SQUARE_METER && value >= 100) {
            "%,.0f".format(value)
        } else {
            "%,.${decimals}f".format(value)
        }
        return "$patterned $label"
    }
}
