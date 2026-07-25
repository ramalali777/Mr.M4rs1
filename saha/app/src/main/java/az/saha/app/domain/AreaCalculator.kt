package az.saha.app.domain

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

object AreaCalculator {
    private const val EARTH_RADIUS_M = 6_371_008.8

    fun closedPerimeterMeters(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in points.indices) {
            total += haversineMeters(points[i], points[(i + 1) % points.size])
        }
        return total
    }

    fun openPathLengthMeters(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.lastIndex) {
            total += haversineMeters(points[i], points[i + 1])
        }
        return total
    }

    /**
     * Polygon area for field-scale plots using local equirectangular projection + shoelace.
     */
    fun areaSquareMeters(points: List<GeoPoint>): Double {
        if (points.size < 3) return 0.0
        val ring = if (
            points.first().latitude == points.last().latitude &&
            points.first().longitude == points.last().longitude
        ) {
            points.dropLast(1)
        } else {
            points
        }
        if (ring.size < 3) return 0.0

        val refLat = ring.map { it.latitude }.average()
        val refLng = ring.map { it.longitude }.average()
        val cosLat = cos(Math.toRadians(refLat))

        val xy = ring.map { p ->
            val x = Math.toRadians(p.longitude - refLng) * EARTH_RADIUS_M * cosLat
            val y = Math.toRadians(p.latitude - refLat) * EARTH_RADIUS_M
            x to y
        }

        var sum = 0.0
        for (i in xy.indices) {
            val (x1, y1) = xy[i]
            val (x2, y2) = xy[(i + 1) % xy.size]
            sum += x1 * y2 - x2 * y1
        }
        return abs(sum) / 2.0
    }

    private fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow2() + cos(lat1) * cos(lat2) * sin(dLon / 2).pow2()
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    private fun Double.pow2() = this * this
}
