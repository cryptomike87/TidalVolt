package com.tidalvolt.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF171614)
private val Panel = Color(0xFF211F1B)
private val Accent = Color(0xFFA8B99A)
private val PrimaryText = Color(0xFFE7E0D4)
private val SecondaryText = Color(0xFFB7AEA0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CompanionHome() }
    }
}

@Composable
private fun CompanionHome() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Panel,
            primary = Accent,
            onBackground = PrimaryText,
            onSurface = PrimaryText
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Background).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("TIDALVOLT  /  COMPANION", color = Accent, fontSize = 10.sp, letterSpacing = 2.sp)
            Text("Vehicle link", color = PrimaryText, fontSize = 30.sp, fontWeight = FontWeight.Light)
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(19.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("2026 BYD Yuan Pro", color = PrimaryText, fontSize = 18.sp)
                    Text("COMPANION STATUS", color = Accent, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    Text("Not connected", color = PrimaryText, fontSize = 23.sp)
                    Text(
                        "This is the separate Companion app foundation. Vehicle/BMS acquisition and secure TidalVolt pairing will be added in later stages.",
                        color = SecondaryText,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
            Text("TELEMETRY CHANNEL", color = Accent, fontSize = 10.sp, letterSpacing = 1.5.sp)
            listOf("Battery and range", "Speed and odometer", "GPS location", "Charging power and session", "Last update and connection health").forEach {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                    Text(it, modifier = Modifier.fillMaxWidth().padding(14.dp), color = PrimaryText, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("No simulated vehicle readings are displayed.", color = SecondaryText, fontSize = 11.sp)
        }
    }
}