package az.saha.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import az.saha.app.domain.GeoPoint
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveSoft

/**
 * Mini “satellite plot” thumbnail: terrain-like fill + measured polygon outline.
 */
@Composable
fun AreaThumbnail(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier.size(64.dp),
    corner: Dp = 14.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF3A4A32),
                        Color(0xFF6B7F4E),
                        Color(0xFF8A9A5C),
                        Color(0xFF4E5C3A)
                    )
                )
            )
    ) {
        // Soft terrain noise bands
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                    center = Offset(w * 0.3f, h * 0.35f),
                    radius = w * 0.7f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x22000000), Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.7f),
                    radius = w * 0.55f
                )
            )

            if (points.size >= 3) {
                val lats = points.map { it.latitude }
                val lngs = points.map { it.longitude }
                val minLat = lats.minOrNull() ?: return@Canvas
                val maxLat = lats.maxOrNull() ?: return@Canvas
                val minLng = lngs.minOrNull() ?: return@Canvas
                val maxLng = lngs.maxOrNull() ?: return@Canvas
                val dLat = (maxLat - minLat).coerceAtLeast(1e-7)
                val dLng = (maxLng - minLng).coerceAtLeast(1e-7)
                val pad = w * 0.16f

                fun mapX(lng: Double) =
                    pad + ((lng - minLng) / dLng).toFloat() * (w - pad * 2)
                fun mapY(lat: Double) =
                    pad + ((maxLat - lat) / dLat).toFloat() * (h - pad * 2)

                val path = Path().apply {
                    val first = points.first()
                    moveTo(mapX(first.longitude), mapY(first.latitude))
                    points.drop(1).forEach { p ->
                        lineTo(mapX(p.longitude), mapY(p.latitude))
                    }
                    close()
                }

                drawPath(path, color = Olive.copy(alpha = 0.45f))
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 3.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                points.forEach { p ->
                    val c = Offset(mapX(p.longitude), mapY(p.latitude))
                    drawCircle(Color.White, radius = 5.5f, center = c)
                    drawCircle(OliveSoft, radius = 3f, center = c)
                }
            }
        }
    }
}
