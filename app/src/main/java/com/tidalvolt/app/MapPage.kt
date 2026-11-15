package com.tidalvolt.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private val NassauPoint = GeoPoint(25.0479, -77.3554)

@Composable
internal fun MapPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var locationMessage by remember { mutableStateOf("Map centered on Nassau · Location not enabled") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    fun enableLocation(map: MapView) {
        val overlay = locationOverlay ?: MyLocationNewOverlay(GpsMyLocationProvider(context), map).also {
            locationOverlay = it
            map.overlays.add(it)
        }
        overlay.enableMyLocation()
        overlay.enableFollowLocation()
        locationMessage = "Finding your location…"
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val map = mapView
        if (granted && map != null) {
            enableLocation(map)
        } else {
            locationMessage = "Location permission denied · Map remains available"
        }
    }

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(NassauPoint)
                    mapView = this
                }
            },
            update = { map -> mapView = map }
        )

        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
            color = SurfaceDark.copy(alpha = .94f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 5.dp
        ) {
            Column(Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {
                Text("TIDALVOLT MAP", color = Aqua, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Text("Explore", color = MainText, fontSize = 18.sp)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact route summary floats over the map; values stay blank until a real route is calculated.
            Surface(
                modifier = Modifier.align(Alignment.End).widthIn(min = 174.dp, max = 210.dp),
                color = SurfaceDark.copy(alpha = .96f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text("ARRIVAL ESTIMATE", color = Aqua, fontSize = 9.sp, letterSpacing = 1.3.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Arrival time", color = SoftText, fontSize = 10.sp)
                            Text("--:--", color = MainText, fontSize = 19.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Battery on arrival", color = SoftText, fontSize = 10.sp)
                            Text("--%", color = MainText, fontSize = 19.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        }
                    }
                    Text("Choose a destination to calculate", color = SoftText, fontSize = 9.sp)
                }
            }
            Surface(
                color = SurfaceDark.copy(alpha = .94f),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    locationMessage,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 9.dp),
                    color = SoftText,
                    fontSize = 11.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val map = mapView
                        if (fine || coarse) {
                            if (map != null) enableLocation(map)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Aqua, contentColor = Ink)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Locate me")
                }
                OutlinedButton(
                    onClick = {
                        mapView?.controller?.animateTo(NassauPoint)
                        mapView?.controller?.setZoom(12.0)
                        locationMessage = "Map centered on Nassau"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceDark.copy(alpha = .94f),
                        contentColor = MainText
                    )
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(5.dp))
                    Text("Nassau")
                }
            }
        }
    }
}
