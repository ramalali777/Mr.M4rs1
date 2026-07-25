package az.saha.app.ui.map

import android.Manifest
import android.graphics.Color as AndroidColor
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import az.saha.app.ui.components.PermissionRequester
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep
import az.saha.app.ui.theme.PanelDark
import az.saha.app.ui.theme.SkyAccent
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    PermissionRequester(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let { loc ->
            mapViewRef?.controller?.animateTo(OsmGeoPoint(loc.latitude, loc.longitude))
        }
    }

    LaunchedEffect(state.points, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        val keep = map.overlays.filterIsInstance<MyLocationNewOverlay>() +
            map.overlays.filterIsInstance<MapEventsOverlay>()
        map.overlays.clear()
        map.overlays.addAll(keep)

        val osmPoints = state.points.map { OsmGeoPoint(it.latitude, it.longitude) }
        if (osmPoints.size >= 2) {
            val line = Polyline().apply {
                setPoints(ArrayList(osmPoints + if (osmPoints.size >= 3) listOf(osmPoints.first()) else emptyList()))
                outlinePaint.color = AndroidColor.parseColor("#7CFF6B")
                outlinePaint.strokeWidth = 8f
            }
            map.overlays.add(line)
        }
        if (osmPoints.size >= 3) {
            val poly = Polygon().apply {
                points = ArrayList(osmPoints)
                fillPaint.color = AndroidColor.parseColor("#553D5C45")
                outlinePaint.color = AndroidColor.parseColor("#7CFF6B")
                outlinePaint.strokeWidth = 6f
            }
            map.overlays.add(poly)
        }
        osmPoints.forEachIndexed { index, point ->
            map.overlays.add(
                Marker(map).apply {
                    position = point
                    title = "${index + 1}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
            )
        }
        map.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(OsmGeoPoint(40.4093, 49.8671))

                    val events = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: OsmGeoPoint?): Boolean {
                            if (p != null && state.mode == MeasureMode.TAP) {
                                viewModel.addTapPoint(GeoPoint(p.latitude, p.longitude))
                            }
                            return true
                        }

                        override fun longPressHelper(p: OsmGeoPoint?): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(events))

                    val myLoc = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        enableMyLocation()
                    }
                    overlays.add(myLoc)
                    locationOverlay = myLoc
                    mapViewRef = this
                }
            },
            update = { map ->
                // Capture latest mode via recreated receiver is hard; taps use state from closure
                // Refresh event overlay binding by keeping viewModel reference stable
                map.onResume()
            }
        )

        // Transparent tap catcher alternative when mode is TAP — use map events with remembered callback
        MapTapBinder(mapViewRef, state.mode) { lat, lng ->
            viewModel.addTapPoint(GeoPoint(lat, lng))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAHA",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
                        .background(PanelDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        FloatingActionButton(
            onClick = {
                locationOverlay?.myLocation?.let {
                    mapViewRef?.controller?.animateTo(it)
                } ?: state.currentLocation?.let {
                    mapViewRef?.controller?.animateTo(OsmGeoPoint(it.latitude, it.longitude))
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
                .background(PanelDark, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(20.dp)
        ) {
            val primary = if (state.areaM2 > 0) state.unit.format(state.areaM2) else "— ${state.unit.label}"
            Text(text = primary, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Text(
                text = buildString {
                    append("${"%.0f".format(state.areaM2)} m²")
                    if (state.perimeterM > 0) append(" · ${"%.1f".format(state.perimeterM)} m perimetr")
                    append(" · ${state.points.size} nöqtə")
                },
                color = Color.White.copy(alpha = 0.75f),
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
                        Icon(Icons.Outlined.Undo, null, tint = Color.White)
                    }, onClick = viewModel::undo)
                    ActionChip("Təmizlə", Color(0xFF3A3A3A), Color.White, Modifier.weight(1f), icon = {
                        Icon(Icons.Outlined.Clear, null, tint = Color.White)
                    }, onClick = viewModel::clear)
                    ActionChip("Saxla", Clay, Ink, Modifier.weight(1f), icon = {
                        Icon(Icons.Outlined.Save, null, tint = Ink)
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
private fun MapTapBinder(
    mapView: MapView?,
    mode: MeasureMode,
    onTap: (Double, Double) -> Unit
) {
    LaunchedEffect(mapView, mode) {
        val map = mapView ?: return@LaunchedEffect
        map.overlays.removeAll { it is MapEventsOverlay }
        map.overlays.add(
            0,
            MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: OsmGeoPoint?): Boolean {
                    if (p != null && mode == MeasureMode.TAP) {
                        onTap(p.latitude, p.longitude)
                    }
                    return true
                }

                override fun longPressHelper(p: OsmGeoPoint?): Boolean = false
            })
        )
    }
}

@Composable
private fun UnitSelector(selected: AreaUnit, onSelect: (AreaUnit) -> Unit) {
    Row(
        modifier = Modifier
            .background(PanelDark, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AreaUnit.entries.forEach { unit ->
            val active = unit == selected
            Text(
                text = unit.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) Olive else Color.Transparent)
                    .clickable { onSelect(unit) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
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
            .background(PanelDark, RoundedCornerShape(18.dp))
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
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
        if (selected) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.Check, null, tint = Color.White)
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
