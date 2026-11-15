package com.tidalvolt.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.*

private val NassauFallback = GeoPoint(25.078, -77.338)

@Composable
fun MapPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var locationEnabled by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) }
    var currentPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var rangePercent by remember { mutableFloatStateOf(60f) }
    var consumption by remember { mutableStateOf("20") }
    var reserve by remember { mutableFloatStateOf(15f) }
    var showRange by remember { mutableStateOf(true) }
    var showChargers by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        locationEnabled = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationEnabled) currentPoint = readLastLocation(context)
    }

    val usablePercent = (rangePercent - reserve).coerceAtLeast(0f)
    val kwhAvailable = 45.1 * usablePercent / 100.0
    val kwhPer100 = consumption.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 20.0
    val estimatedMiles = kwhAvailable / kwhPer100 * 100.0

    Column(modifier.fillMaxSize().background(Ink)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TIDALVOLT  /  EXPLORE", color = Aqua, fontSize = 10.sp, letterSpacing = 2.sp)
                Text("Map", color = MainText, fontSize = 27.sp)
            }
            IconButton(onClick = {
                if (locationEnabled) {
                    currentPoint = readLastLocation(context)
                    currentPoint?.let { mapView?.controller?.animateTo(it) }
                } else permissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))
            }) { Icon(Icons.Default.MyLocation, "Use my location", tint = Aqua) }
        }

        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.5)
                    controller.setCenter(NassauFallback)
                    mapView = this
                    currentPoint = if (locationEnabled) readLastLocation(ctx) else null
                    invalidate()
                }
            },
            update = { view ->
                view.overlays.removeAll { it is Polygon || it is Marker }
                val center = currentPoint ?: NassauFallback
                if (showRange) {
                    val radiusMeters = (estimatedMiles * 1609.34).coerceAtLeast(0.0)
                    val ring = Polygon().apply {
                        points = circlePoints(center, radiusMeters)
                        fillPaint.color = android.graphics.Color.argb(28, 57, 125, 120)
                        outlinePaint.color = android.graphics.Color.argb(170, 57, 125, 120)
                        outlinePaint.strokeWidth = 3f
                    }
                    view.overlays.add(ring)
                }
                if (currentPoint != null) {
                    view.overlays.add(Marker(view).apply {
                        position = currentPoint!!
                        title = "Your last known location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                }
                view.invalidate()
            }
        )

        Column(
            Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 17.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("MAP LAYERS", color = SoftText, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Range", color = MainText, fontSize = 11.sp)
                    Switch(checked = showRange, onCheckedChange = { showRange = it })
                    Text("Chargers", color = MainText, fontSize = 11.sp)
                    Switch(checked = showChargers, onCheckedChange = { showChargers = it })
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ESTIMATED RANGE", color = SoftText, fontSize = 9.sp, letterSpacing = 1.sp)
                    Text("~${estimatedMiles.roundToInt()} mi", color = Aqua, fontSize = 25.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("BATTERY INPUT", color = SoftText, fontSize = 9.sp, letterSpacing = 1.sp)
                    Text("${rangePercent.roundToInt()}%  ·  ${reserve.roundToInt()}% reserve", color = MainText, fontSize = 13.sp)
                }
            }
            Text("Battery level used for estimate", color = SoftText, fontSize = 11.sp)
            Slider(value = rangePercent, onValueChange = { rangePercent = it.coerceAtLeast(reserve) }, valueRange = 0f..100f)
            Text("Reserve: ${reserve.roundToInt()}%", color = SoftText, fontSize = 11.sp)
            Slider(value = reserve, onValueChange = { reserve = it.coerceAtMost(rangePercent) }, valueRange = 0f..40f)
            OutlinedTextField(
                value = consumption, onValueChange = { consumption = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Consumption · kWh/100 mi") }
            )
            OutlinedTextField(
                value = destination, onValueChange = { destination = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Plan a destination") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { Icon(Icons.Default.Directions, null, tint = Aqua) }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (locationEnabled && currentPoint != null) "Using last known device location" else "Map centered on Nassau · location not enabled",
                    color = SoftText, fontSize = 10.sp
                )
                TextButton(onClick = {
                    if (!locationEnabled) permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )) else {
                        currentPoint = readLastLocation(context)
                        currentPoint?.let { mapView?.controller?.animateTo(it) }
                    }
                }) { Text("Locate me") }
            }
            Text(
                "Range ring is a straight-line preview, not a road-reachable boundary. Route guidance and live charger data are not connected yet.",
                color = SoftText, fontSize = 10.sp, lineHeight = 14.sp
            )
        }
    }

    DisposableEffect(mapView) {
        mapView?.onResume()
        onDispose { mapView?.onPause(); mapView?.onDetach(); mapView = null }
    }
}

private fun readLastLocation(context: Context): GeoPoint? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null
    return try {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = manager.getProviders(true)
        val last = providers.mapNotNull { provider ->
            try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
        }.maxByOrNull { it.time }
        last?.let { GeoPoint(it.latitude, it.longitude) }
    } catch (_: Exception) { null }
}

private fun circlePoints(center: GeoPoint, radiusMeters: Double): List<GeoPoint> {
    val earth = 6378137.0
    val lat = Math.toRadians(center.latitude)
    val lon = Math.toRadians(center.longitude)
    return (0..72).map { index ->
        val bearing = 2.0 * Math.PI * index / 72.0
        val angular = radiusMeters / earth
        val newLat = asin(sin(lat) * cos(angular) + cos(lat) * sin(angular) * cos(bearing))
        val newLon = lon + atan2(
            sin(bearing) * sin(angular) * cos(lat),
            cos(angular) - sin(lat) * sin(newLat)
        )
        GeoPoint(Math.toDegrees(newLat), Math.toDegrees(newLon))
    }
}
