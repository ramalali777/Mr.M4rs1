package az.saha.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.R
import az.saha.app.data.model.Measurement
import az.saha.app.ui.components.AreaThumbnail
import az.saha.app.ui.components.AtmosphereBackground
import az.saha.app.ui.navigation.SaheMenuButton
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.InkMuted
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenMenu: () -> Unit,
    onNewMeasurement: () -> Unit = {}
) {
    val measurements by viewModel.items.collectAsStateWithLifecycle()
    val syncing by viewModel.syncing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshUser()
        viewModel.sync()
    }

    AtmosphereBackground(
        imageRes = R.drawable.bg_soft_hills,
        scrim = Brush.verticalGradient(
            listOf(
                Color(0xB3F4F0E6),
                Color(0xE6F4F0E6),
                Color(0xF5F4F0E6)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SaheMenuButton(onClick = onOpenMenu, tint = OliveDeep)
                    Text(
                        text = "SAHƏ",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        color = OliveDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (!syncing) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = Olive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (syncing) "Sinxron..." else "Sinxronlaşıb",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Ölçülərim",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Saxlanmış ərazilər",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (measurements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Hələ ölçmə yoxdur.\nAşağıdan yeni ölçmə başladın.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(measurements, key = { it.id }) { item ->
                                MeasurementRow(
                                    item = item,
                                    onDelete = { viewModel.delete(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Mockup-style primary CTA
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(OliveDeep)
                    .clickable(onClick = onNewMeasurement)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Yeni ölçmə",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MeasurementRow(
    item: Measurement,
    onDelete: () -> Unit
) {
    val date = rememberAzDate(item.createdAt)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AreaThumbnail(points = item.points, modifier = Modifier.size(68.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title.ifBlank { "Adsız sahə" },
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                item.preferredUnit.format(item.areaSquareMeters),
                style = MaterialTheme.typography.bodyLarge,
                color = OliveDeep
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(date, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (item.synced) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = if (item.synced) Olive else Clay,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Sil", tint = InkMuted)
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = InkMuted.copy(alpha = 0.5f)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(1.dp)
            .background(Olive.copy(alpha = 0.10f))
    )
}

@Composable
private fun rememberAzDate(millis: Long): String {
    val fmt = SimpleDateFormat("d MMM yyyy", Locale("az"))
    return fmt.format(Date(millis))
}
