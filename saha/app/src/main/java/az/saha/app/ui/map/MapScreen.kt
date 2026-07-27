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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Layers
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import az.saha.app.ui.components.rememberPermissionGranted
import az.saha.app.ui.navigation.SaheMenuButton
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

private enum class MapLayer { SATELLITE, STREET }

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onOpenMenu: () -> Unit = {}
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var layer by remember { mutableStateOf(MapLayer.SATELLITE) }
    var didCenterOnUser by remember { mutableStateOf(false) }

    val locationGranted = rememberPermissionGranted(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationGranted) {
        if (locationGranted) viewModel.startLocationFollow()
    }

    LaunchedEffect(state.currentLocation, locationGranted, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        val loc = state.currentLocation ?: return@LaunchedEffect
        if (locationGranted && !didCenterOnUser) {
            map.controller.animateTo(OsmGeoPoint(loc.latitude, loc.longitude))
            map.controller.setZoom(18.0)
            didCenterOnUser = true
        }
    }

    LaunchedEffect(layer, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        map.setTileSource(
            when (layer) {
                MapLayer.SATELLITE -> SatelliteTileSource
                MapLayer.STREET -> TileSourceFactory.MAPNIK
            }
        )
        map.invalidate()
    }

    DisposableEffect(Unit) {
        onDispose { mapViewRef?.onDetach() }
    }

    LaunchedEffect(state.points, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        val keep = map.overlays.filterIsInstance<MyLocationNewOverlay>() +
            map.overlays.filterIsInstance<MapEventsOverlay>()
        map.overlays.clear()
        map.overlays.addAll(keep)

        val osmPoints = state.points.map { OsmGeoPoint(it.latitude, it.longitude) }
        if (osmPoints.size >= 2) {
            map.overlays.add(
                Polyline().apply {
                    setPoints(
                        ArrayList(
                            osmPoints + if (osmPoints.size >= 3) listOf(osmPoints.first()) else emptyList()
                        )
                    )
                    outlinePaint.color = AndroidColor.parseColor("#B8FF6A")
                    outlinePaint.strokeWidth = 8f
                }
            )
        }
        if (osmPoints.size >= 3) {
            map.overlays.add(
                Polygon().apply {
                    points = ArrayList(osmPoints)
                    fillPaint.color = AndroidColor.parseColor("#553D5C45")
                    outlinePaint.color = AndroidColor.parseColor("#B8FF6A")
                    outlinePaint.strokeWidth = 6f
                }
            )
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
                    setTileSource(SatelliteTileSource)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(OsmGeoPoint(40.4093, 49.8671))

                    val myLoc = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        enableMyLocation()
                    }
                    overlays.add(myLoc)
                    locationOverlay = myLoc
                    mapViewRef = this
                }
            },
            update = { map ->
                map.onResume()
                if (locationGranted) {
                    locationOverlay?.enableMyLocation()
                }
            }
        )

        MapTapBinder(mapViewRef, state.mode) { lat, lng ->
            viewModel.addTapPoint(GeoPoint(lat, lng))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent))
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

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    layer = if (layer == MapLayer.SATELLITE) MapLayer.STREET else MapLayer.SATELLITE
                },
                containerColor = Clay,
                contentColor = Ink,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = "Lay")
            }
            FloatingActionButton(
                onClick = {
                    val loc = state.currentLocation
                    if (loc != null) {
                        mapViewRef?.controller?.animateTo(OsmGeoPoint(loc.latitude, loc.longitude))
                        mapViewRef?.controller?.setZoom(18.0)
                    } else {
                        viewModel.startLocationFollow()
                        locationOverlay?.myLocation?.let {
                            mapViewRef?.controller?.animateTo(it)
                            mapViewRef?.controller?.setZoom(18.0)
                        }
                    }
                },
                containerColor = Clay,
                contentColor = Ink,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "Mövqe")
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(PanelDark)
                .padding(20.dp)
        ) {
            Text(
                text = if (layer == MapLayer.SATELLITE) "Satellite · pulsuz" else "Küçə xəritəsi",
                color = Clay,
                style = MaterialTheme.typography.labelLarge
            )
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
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
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
