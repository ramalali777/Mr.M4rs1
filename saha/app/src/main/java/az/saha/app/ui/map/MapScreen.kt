package az.saha.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import az.saha.app.ui.components.PermissionRequester
import az.saha.app.ui.navigation.SaheMenuButton
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep
import az.saha.app.ui.theme.PanelDark
import az.saha.app.ui.theme.SkyAccent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onOpenMenu: () -> Unit = {}
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val baku = LatLng(40.4093, 49.8671)
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(baku, 17f)
    }
    var locationGranted by remember { mutableStateOf(false) }

    PermissionRequester(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        locationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let {
            camera.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 18f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(
                isMyLocationEnabled = locationGranted,
                mapType = MapType.HYBRID // Satellite + labels
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false
            ),
            onMapClick = { latLng ->
                if (state.mode == MeasureMode.TAP) {
                    viewModel.addTapPoint(GeoPoint(latLng.latitude, latLng.longitude))
                }
            }
        ) {
            val latLngs = state.points.map { LatLng(it.latitude, it.longitude) }
            if (latLngs.size >= 2) {
                Polyline(
                    points = latLngs + if (latLngs.size >= 3) listOf(latLngs.first()) else emptyList(),
                    color = Color(0xFFB8FF6A),
                    width = 7f
                )
            }
            if (latLngs.size >= 3) {
                Polygon(
                    points = latLngs,
                    fillColor = Color(0x553D5C45),
                    strokeColor = Color(0xFFB8FF6A),
                    strokeWidth = 5f
                )
            }
            latLngs.forEachIndexed { index, point ->
                Marker(state = MarkerState(point), title = "${index + 1}")
            }
        }

        // Top vignette for brand readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x99000000), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SaheMenuButton(onClick = onOpenMenu, tint = Color.White)
                    Text(
                        text = "SAHƏ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                UnitSelector(selected = state.unit, onSelect = viewModel::setUnit)
            }

            Spacer(Modifier.height(10.dp))
            ModeRow(
                mode = state.mode,
                walkTracking = state.walkTracking,
                onTap = { viewModel.setMode(MeasureMode.TAP) },
                onWalk = {
                    viewModel.setMode(MeasureMode.WALK)
                    viewModel.startWalk()
                }
            )

            AnimatedVisibility(visible = state.walkTracking, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = "Gəzərək ölçün · ${state.points.size} nöqtə · izləmə aktiv",
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PanelDark)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        FloatingActionButton(
            onClick = {
                state.currentLocation?.let {
                    camera.position = CameraPosition.fromLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        18f
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            containerColor = Clay,
            contentColor = Ink,
            shape = CircleShape
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "Mövqe")
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(PanelDark)
                .padding(20.dp)
        ) {
            val primary =
                if (state.areaM2 > 0) state.unit.format(state.areaM2) else "— ${state.unit.label}"
            Text(
                text = primary,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = buildString {
                    append("${"%.0f".format(state.areaM2)} m²")
                    if (state.perimeterM > 0) append(" · ${"%.1f".format(state.perimeterM)} m perimetr")
                    append(" · ${state.points.size} nöqtə")
                },
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(14.dp))

            if (state.mode == MeasureMode.WALK) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Nöqtə qeyd et", Olive, Color.White, Modifier.weight(1f), onClick = viewModel::recordWalkPoint)
                    ActionChip("Bitir", Clay, Ink, Modifier.weight(1f), onClick = viewModel::finishWalk)
                    ActionChip("Dayandır", Color(0xFF3A3A3A), Color.White, Modifier.weight(1f), onClick = viewModel::stopWalk)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Geri al", OliveDeep, Color.White, Modifier.weight(1f), icon = {
                        Icon(Icons.Outlined.Undo, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }, onClick = viewModel::undo)
                    ActionChip("Təmizlə", Color(0xFF3A3A3A), Color.White, Modifier.weight(1f), icon = {
                        Icon(Icons.Outlined.Clear, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }, onClick = viewModel::clear)
                    ActionChip("Saxla", Clay, Ink, Modifier.weight(1f), icon = {
                        Icon(Icons.Outlined.Save, null, tint = Ink, modifier = Modifier.size(18.dp))
                    }, onClick = viewModel::openSaveDialog)
                }
            }

            AnimatedVisibility(visible = state.message != null) {
                Text(
                    text = state.message ?: "",
                    color = SkyAccent,
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (state.showSaveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSave,
            title = { Text("Ölçməni saxla") },
            text = {
                OutlinedTextField(
                    value = state.saveTitle,
                    onValueChange = viewModel::onSaveTitle,
                    label = { Text("Ad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::save,
                    enabled = !state.saving,
                    colors = ButtonDefaults.buttonColors(containerColor = Olive)
                ) { Text("Saxla") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSave) { Text("Ləğv et") }
            }
        )
    }
}

@Composable
private fun UnitSelector(selected: AreaUnit, onSelect: (AreaUnit) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(PanelDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AreaUnit.entries.forEach { unit ->
            val active = unit == selected
            Text(
                text = unit.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (active) Olive else Color.Transparent)
                    .clickable { onSelect(unit) }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ModeRow(
    mode: MeasureMode,
    walkTracking: Boolean,
    onTap: () -> Unit,
    onWalk: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(PanelDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModeChip(
            selected = mode == MeasureMode.TAP,
            label = "Toxunaraq",
            icon = Icons.Outlined.TouchApp,
            onClick = onTap
        )
        ModeChip(
            selected = mode == MeasureMode.WALK || walkTracking,
            label = "Gəzərək",
            icon = Icons.Outlined.DirectionsWalk,
            onClick = onWalk
        )
    }
}

@Composable
private fun ModeChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Olive else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
        if (selected) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}
