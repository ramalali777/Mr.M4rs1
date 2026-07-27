package az.saha.app.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class AreaCalculatorTest {
    @Test
    fun squareApproxOneHectareNearEquator() {
        // ~100m x 100m square around equator => ~1 ha
        val originLat = 0.0
        val originLng = 0.0
        val dLat = 100.0 / 111_320.0
        val dLng = 100.0 / 111_320.0
        val points = listOf(
            GeoPoint(originLat, originLng),
            GeoPoint(originLat, originLng + dLng),
            GeoPoint(originLat + dLat, originLng + dLng),
            GeoPoint(originLat + dLat, originLng)
        )
        val area = AreaCalculator.areaSquareMeters(points)
        assertTrue("area=$area", area in 9_500.0..10_500.0)
    }

    @Test
    fun needsAtLeastThreePoints() {
        assertTrue(AreaCalculator.areaSquareMeters(emptyList()) == 0.0)
        assertTrue(
            AreaCalculator.areaSquareMeters(
                listOf(GeoPoint(40.0, 49.0), GeoPoint(40.1, 49.1))
            ) == 0.0
        )
    }
}
