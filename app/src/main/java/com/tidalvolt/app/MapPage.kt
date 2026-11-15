package com.tidalvolt.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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

@Composable
internal fun MapPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var locationMessage by remember { mutableStateOf("Map centered on Nassau. Tap locate to use your position.") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val map = mapView
        if (granted && map != null) {
            val overlay = locationOverlay ?: MyLocationNewOverlay(GpsMyLocationProvider(context), map).also {
                locationOverlay = it
                map.overlays.add(it)
            }
            overlay.enableMyLocation()
            overlay.enableFollowLocation()
            locationMessage = "Location enabled. GPS accuracy depends on signal."
        } else {
            locationMessage = "Location permission was not granted. The map still works without it."
        }
    }

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("EXPLORE", color = Aqua, fontSize = 10.sp, letterSpacing = 2.sp)
        Text("Map", color = MainText, fontSize = 29.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Light)
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(12.0)
                        controller.setCenter(GeoPoint(25.0479, -77.3554))
                        mapView = this
                    }
                },
                update = { map ->
                    mapView = map
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fine || coarse) {
                        val map = mapView
                        if (map != null) {
                            val overlay = locationOverlay ?: MyLocationNewOverlay(GpsMyLocationProvider(context), map).also {
                                locationOverlay = it
                                map.overlays.add(it)
                            }
                            overlay.enableMyLocation()
                            overlay.enableFollowLocation()
                            locationMessage = "Location enabled. GPS accuracy depends on signal."
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
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
                    mapView?.controller?.animateTo(GeoPoint(25.0479, -77.3554))
                    mapView?.controller?.setZoom(12.0)
                    locationMessage = "Map centered on Nassau."
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("Nassau")
            }
        }
        Text(locationMessage, color = SoftText, fontSize = 11.sp)
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("EV range map", color = MainText, fontSize = 15.sp)
                Text("Road-based reachable range, charging stations and route energy estimates will appear here once vehicle telemetry and routing calculations are connected. No range circle or live battery value is fabricated.", color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}
