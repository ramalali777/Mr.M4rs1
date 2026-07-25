package az.saha.app.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
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
    onOpenMenu: () -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val syncing by viewModel.syncing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshUser()
        viewModel.sync()
    }

    AtmosphereBackground(
        imageRes = R.drawable.bg_soft_hills,
        scrim = Brush.verticalGradient(
            listOf(
                Color(0xCCF4F0E6),
                Color(0xE6F4F0E6),
                Color(0xF2F4F0E6)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaheMenuButton(onClick = onOpenMenu, tint = OliveDeep)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SAHƏ",
                        style = MaterialTheme.typography.titleMedium,
                        color = OliveDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ölçülərim",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Ink
                    )
                    Text(
                        "Saxlanmış ərazilər",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
                IconButton(onClick = viewModel::sync, enabled = !syncing) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Sinxron", tint = Olive)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White.copy(alpha = 0.82f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Hələ ölçmə yoxdur.\nXəritədən yeni sahə ölçün.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkMuted
                        )
                    }
                } else {
                    LazyColumn {
                        items(items, key = { it.id }) { item ->
                            MeasurementRow(item, onDelete = { viewModel.delete(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementRow(item: Measurement, onDelete: () -> Unit) {
    val date = rememberAzDate(item.createdAt)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Olive.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.preferredUnit.label,
                color = OliveDeep,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title.ifBlank { "Adsız sahə" },
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(date, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (item.synced) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = if (item.synced) Olive else Clay,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = item.preferredUnit.format(item.areaSquareMeters),
            style = MaterialTheme.typography.titleMedium,
            color = OliveDeep,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Sil", tint = InkMuted)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Olive.copy(alpha = 0.12f))
    )
}

@Composable
private fun rememberAzDate(millis: Long): String {
    val fmt = SimpleDateFormat("d MMM yyyy", Locale("az"))
    return fmt.format(Date(millis))
}
