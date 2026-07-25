package az.saha.app.data.model

import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint

data class Measurement(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val points: List<GeoPoint> = emptyList(),
    val areaSquareMeters: Double = 0.0,
    val perimeterMeters: Double = 0.0,
    val preferredUnit: AreaUnit = AreaUnit.SOT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
