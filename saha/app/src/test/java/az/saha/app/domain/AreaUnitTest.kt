package az.saha.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AreaUnitTest {
    @Test
    fun sotAndPutAndHectareConversions() {
        val m2 = 10_000.0
        assertEquals(100.0, AreaUnit.SOT.fromSquareMeters(m2), 1e-9)
        assertEquals(10.0, AreaUnit.PUT.fromSquareMeters(m2), 1e-9)
        assertEquals(1.0, AreaUnit.HECTARE.fromSquareMeters(m2), 1e-9)
        assertEquals(10_000.0, AreaUnit.SQUARE_METER.fromSquareMeters(m2), 1e-9)
    }

    @Test
    fun putIsTenSot() {
        assertEquals(
            AreaUnit.PUT.squareMetersPerUnit,
            AreaUnit.SOT.squareMetersPerUnit * 10,
            1e-9
        )
    }
}
