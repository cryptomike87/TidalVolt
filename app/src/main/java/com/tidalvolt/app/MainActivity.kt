package com.tidalvolt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Locale

private val Ink = Color(0xFF080B0E)
private val SurfaceDark = Color(0xFF151B21)
private val SurfaceRaised = Color(0xFF202A32)
private val Aqua = Color(0xFF48D8D0)
private val SoftText = Color(0xFF9EABB4)
private val MainText = Color(0xFFF3F6F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TidalVoltRoot() }
    }
}

@Composable
private fun TidalVoltRoot() {
    var page by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Aqua, background = Ink, surface = SurfaceDark, onBackground = MainText, onSurface = MainText)) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = SurfaceDark) {
                    val labels = listOf("Vehicle", "Charge", "Savings", "Drive", "More")
                    val icons = listOf(Icons.Default.DirectionsCar, Icons.Default.Bolt, Icons.Default.Savings, Icons.Default.Route, Icons.Default.Tune)
                    labels.forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = page == i, onClick = { page = i },
                            icon = { Icon(icons[i], contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Aqua, selectedTextColor = Aqua, indicatorColor = SurfaceRaised, unselectedIconColor = SoftText, unselectedTextColor = SoftText)
                        )
                    }
                }
            }
        ) { padding ->
            when (page) {
                0 -> VehiclePage(Modifier.padding(padding))
                1 -> ChargingPage(Modifier.padding(padding))
                2 -> SavingsPage(Modifier.padding(padding))
                3 -> DrivingPage(Modifier.padding(padding))
                else -> MorePage(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 21.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(17.dp), content = content)
}

@Composable
private fun Heading(eyebrow: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(eyebrow.uppercase(), color = Aqua, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
        Text(title, color = MainText, fontSize = 29.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun VehiclePage(modifier: Modifier = Modifier) {
    Page(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Heading("TIDALVOLT  /  GARAGE", "Vehicle")
            Icon(Icons.Default.AccountCircle, null, tint = SoftText, modifier = Modifier.size(28.dp))
        }
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(25.dp)) {
            Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Text("2026 BYD Yuan Pro", color = MainText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Box(Modifier.fillMaxWidth().height(145.dp).background(SurfaceRaised, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsCar, null, tint = Aqua.copy(alpha = .85f), modifier = Modifier.size(100.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("BATTERY", color = SoftText, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Text("--%", color = MainText, fontSize = 43.sp, fontWeight = FontWeight.Light)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ESTIMATED RANGE", color = SoftText, fontSize = 10.sp, letterSpacing = 1.sp)
                        Text("-- mi", color = MainText, fontSize = 25.sp)
                    }
                }
                LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth(), color = Aqua, trackColor = SurfaceRaised)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.CloudOff, null, tint = SoftText, modifier = Modifier.size(15.dp))
                    Text("Waiting for verified vehicle connection", color = SoftText, fontSize = 11.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            SmallMetric(Modifier.weight(1f), "LIVE STATUS", "Not connected")
            SmallMetric(Modifier.weight(1f), "CONSUMPTION", "-- kWh/100 mi")
        }
        InfoPanel("Vehicle intelligence", "Connect a supported telemetry source to unlock battery, range, trip and charging insights.")
        Text("NO SIMULATED VALUES ARE PRESENTED AS LIVE DATA", color = SoftText, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun SmallMetric(modifier: Modifier, label: String, value: String) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, color = SoftText, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(value, color = MainText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ChargingPage(modifier: Modifier = Modifier) {
    var current by remember { mutableFloatStateOf(40f) }
    var target by remember { mutableFloatStateOf(80f) }
    var tariff by remember { mutableStateOf("0.55") }
    val estimate = ChargingMath.toTarget(45.1, current.toDouble(), target.toDouble(), tariff.toDoubleOrNull() ?: .55)
    Page(modifier) {
        Heading("ENERGY", "Charging")
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("CHARGING COST ESTIMATE", color = Aqua, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Text("Current battery  ·  ${current.toInt()}%", color = MainText)
                Slider(value = current, onValueChange = { current = it.coerceAtMost(target) }, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua))
                Text("Target charge  ·  ${target.toInt()}%", color = MainText)
                Slider(value = target, onValueChange = { target = it.coerceAtLeast(current) }, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua))
                OutlinedTextField(value = tariff, onValueChange = { tariff = it }, label = { Text("Electricity tariff · BSD/kWh") }, singleLine = true)
                HorizontalDivider(color = SurfaceRaised)
                ValueLine("Energy stored in battery", "${fmt(estimate.batteryKwh)} kWh")
                ValueLine("Energy from charger (+5% loss)", "${fmt(estimate.gridKwh)} kWh")
                Text("ESTIMATED CHARGING COST", color = SoftText, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Text("$${fmt(estimate.cost)} BSD", color = Aqua, fontSize = 34.sp, fontWeight = FontWeight.Light)
                Text("Uses 95% charging efficiency. Actual charger-meter energy takes precedence when available.", color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SavingsPage(modifier: Modifier = Modifier) {
    var miles by remember { mutableFloatStateOf(1000f) }
    var petrolPrice by remember { mutableStateOf("6.85") }
    var kiaMpg by remember { mutableStateOf("15") }
    var powerPrice by remember { mutableStateOf("0.55") }
    var evUse by remember { mutableStateOf("20") }
    val petrol = ChargingMath.petrolCost(miles.toDouble(), kiaMpg.toDoubleOrNull()?.coerceAtLeast(.1) ?: 15.0, petrolPrice.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 6.85)
    val electric = ChargingMath.evCost(miles.toDouble(), evUse.toDoubleOrNull()?.coerceAtLeast(.1) ?: 20.0, powerPrice.toDoubleOrNull()?.coerceAtLeast(0.0) ?: .55)
    val saved = petrol - electric
    Page(modifier) {
        Heading("OWNERSHIP", "EV vs petrol")
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("COST OF THE SAME DISTANCE", color = Aqua, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Text("BYD Yuan Pro  vs  Kia Sportage", color = MainText, fontSize = 15.sp)
                Text("Distance · ${miles.toInt()} miles", color = SoftText)
                Slider(value = miles, onValueChange = { miles = it }, valueRange = 100f..3000f, colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua))
                OutlinedTextField(petrolPrice, { petrolPrice = it }, label = { Text("Petrol price · BSD/gallon") }, singleLine = true)
                OutlinedTextField(kiaMpg, { kiaMpg = it }, label = { Text("Kia fuel economy · MPG") }, singleLine = true)
                OutlinedTextField(powerPrice, { powerPrice = it }, label = { Text("Electricity price · BSD/kWh") }, singleLine = true)
                OutlinedTextField(evUse, { evUse = it }, label = { Text("BYD consumption · kWh/100 mi") }, singleLine = true)
                HorizontalDivider(color = SurfaceRaised)
                ValueLine("Kia petrol cost", "$${fmt(petrol)} BSD")
                ValueLine("BYD electricity (+5% loss)", "$${fmt(electric)} BSD")
                Text("ESTIMATED SAVINGS", color = SoftText, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Text("$${fmt(saved)} BSD", color = if (saved >= 0) Aqua else Color(0xFFFF8A80), fontSize = 35.sp, fontWeight = FontWeight.Light)
                Text("Fuel price and vehicle efficiency are editable assumptions. Historical records will preserve the rates used at the time.", color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun DrivingPage(modifier: Modifier = Modifier) {
    Page(modifier) {
        Heading("ON THE ROAD", "Driving")
        InfoPanel("Trip history", "Recorded journeys, distance, duration and trip energy will appear here.")
        InfoPanel("Driving efficiency", "Track consumption, average speed, trip cost and efficiency trends.")
        InfoPanel("Time tracking", "Driving, parked and vehicle-ready stationary time will be classified separately.")
        InfoPanel("Traffic stop estimates", "Stationary traffic events will be estimates with confidence labels, not assumed red lights.")
    }
}

@Composable
private fun MorePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val credentials = remember {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context,
            "tidalvolt_secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    var apiKey by remember { mutableStateOf(credentials.getString("abrp_api_key", "") ?: "") }
    var userToken by remember { mutableStateOf(credentials.getString("abrp_user_token", "") ?: "") }
    var saved by remember { mutableStateOf(apiKey.isNotBlank() && userToken.isNotBlank()) }
    Page(modifier) {
        Heading("PREFERENCES", "More")
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ABRP CONNECTION", color = Aqua, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
                Text("Add your ABRP credentials", color = MainText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text("ABRP uses two separate credentials for generic telemetry: an application Telemetry API key and your vehicle's ABRP user token.", color = SoftText, fontSize = 12.sp, lineHeight = 18.sp)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ABRP Telemetry API key") },
                    placeholder = { Text("Paste your API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = userToken,
                    onValueChange = { userToken = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ABRP vehicle user token") },
                    placeholder = { Text("Paste your vehicle token") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Button(
                    onClick = {
                        credentials.edit().putString("abrp_api_key", apiKey.trim())
                            .putString("abrp_user_token", userToken.trim()).apply()
                        saved = true
                    },
                    enabled = apiKey.isNotBlank() && userToken.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Aqua, contentColor = Ink)
                ) { Text("Save ABRP credentials") }
                Text(
                    if (saved) "Credentials saved securely on this device." else "Credentials have not been saved.",
                    color = if (saved) Aqua else SoftText,
                    fontSize = 11.sp
                )
                Text("Create a free Telemetry API key in ABRP's API settings. Find the vehicle token in ABRP under your vehicle's Live Data setup → Generic → Copy Token.", color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
                Text("Credentials are stored in encrypted app preferences and are not written into GitHub source code.", color = SoftText, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
        InfoPanel("Vehicle profile", "2026 BYD Yuan Pro · 45.1 kWh nominal battery")
        InfoPanel("Units", "Miles · MPH · °F")
        InfoPanel("Planned integrations", "ABRP telemetry · Open-Meteo weather · OpenStreetMap routing")
        InfoPanel("Data accuracy", "Measured, estimated and simulated values will be clearly distinguished.")
        InfoPanel("TidalVolt", "Fresh Android foundation · Version 1.0.0")
    }
}

@Composable
private fun InfoPanel(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(body, color = SoftText, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ValueLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SoftText, fontSize = 11.sp)
        Text(value, color = MainText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun fmt(value: Double) = String.format(Locale.US, "%.2f", value)
