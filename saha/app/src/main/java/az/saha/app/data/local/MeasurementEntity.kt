package az.saha.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import az.saha.app.data.model.Measurement
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val pointsJson: String,
    val areaSquareMeters: Double,
    val perimeterMeters: Double,
    val preferredUnit: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean
) {
    fun toDomain(): Measurement = Measurement(
        id = id,
        userId = userId,
        title = title,
        points = decodePoints(pointsJson),
        areaSquareMeters = areaSquareMeters,
        perimeterMeters = perimeterMeters,
        preferredUnit = runCatching { AreaUnit.valueOf(preferredUnit) }.getOrDefault(AreaUnit.SOT),
        createdAt = createdAt,
        updatedAt = updatedAt,
        synced = synced
    )

    companion object {
        fun fromDomain(m: Measurement) = MeasurementEntity(
            id = m.id,
            userId = m.userId,
            title = m.title,
            pointsJson = encodePoints(m.points),
            areaSquareMeters = m.areaSquareMeters,
            perimeterMeters = m.perimeterMeters,
            preferredUnit = m.preferredUnit.name,
            createdAt = m.createdAt,
            updatedAt = m.updatedAt,
            synced = m.synced
        )

        fun encodePoints(points: List<GeoPoint>): String {
            val arr = JSONArray()
            points.forEach { p ->
                arr.put(JSONObject().put("lat", p.latitude).put("lng", p.longitude))
            }
            return arr.toString()
        }

        fun decodePoints(json: String): List<GeoPoint> {
            val arr = JSONArray(json)
            return buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(GeoPoint(o.getDouble("lat"), o.getDouble("lng")))
                }
            }
        }
    }
}
