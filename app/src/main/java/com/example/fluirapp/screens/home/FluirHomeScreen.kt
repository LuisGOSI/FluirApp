package com.example.fluirapp.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

data class TankData(
    val id: String,
    val name: String,
    val fillLevel: Float, // 0-100%
    val pumpPower: String, // "Alta", "Baja", "Media"
    val status: String, // "Óptimo", "Requiere inspección", "Normal"
    val isOperating: Boolean = false,
    val capacity: String = "1000 L",
    val pumpPowerKw: String = "50kW"
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun FluirHomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    // Estados simulados para los sensores MQTT - aquí conectarías con tu ViewModel real
    var tank1Data by remember {
        mutableStateOf(
            TankData(
                id = "tank_1",
                name = "El Dorado",
                fillLevel = 80f,
                pumpPower = "Alta",
                status = "Normal",
                isOperating = true
            )
        )
    }

    var tank2Data by remember {
        mutableStateOf(
            TankData(
                id = "tank_2",
                name = "Manzanares",
                fillLevel = 90f,
                pumpPower = "Alta",
                status = "Óptimo",
                isOperating = true
            )
        )
    }

    // Aquí simulo la actualización de datos MQTT - reemplaza con tu lógica real
    LaunchedEffect(Unit) {
        // Ejemplo de cómo podrías actualizar los datos desde MQTT
        // viewModel.subscribeToMqttTopic("sensor/tank1") { data ->
        //     tank1Data = tank1Data.copy(fillLevel = data.level, status = data.status)
        // }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F7FA),
                        Color(0xFFE8F4F8)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tarjeta de consumo de energía
            item {
                EnergyConsumptionCard()
            }

            // Tanque 1
            item {
                TankCard(
                    tankData = tank1Data,
                    onTankClick = {
                        // Navegar a la pantalla de detalles del tanque
                        navController.navigate("tank_detail/${tank1Data.id}")
                    }
                )
            }

            // Tanque 2
            item {
                TankCard(
                    tankData = tank2Data,
                    onTankClick = {
                        // Navegar a la pantalla de detalles del tanque
                        navController.navigate("tank_detail/${tank2Data.id}")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun EnergyConsumptionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBBDEFB)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Energía consumida hoy",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "100kWh",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold
                ),
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun TankCard(
    tankData: TankData,
    onTankClick: () -> Unit
) {
    val statusColor = when (tankData.status) {
        "Óptimo" -> Color(0xFF4CAF50)
        "Requiere inspección" -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    val statusIndicatorColor = when (tankData.status) {
        "Óptimo" -> Color(0xFF4CAF50)
        "Requiere inspección" -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onTankClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con nombre del tanque y indicador de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tankData.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            statusIndicatorColor,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido principal del tanque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ilustración del tanque
                TankIllustration(
                    fillLevel = tankData.fillLevel,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Información del tanque
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Potencia de la bomba: ${tankData.pumpPower}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        )
                    )
                    Text(
                        text = "Nivel de llenado: ${tankData.fillLevel.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        )
                    )
                    Text(
                        text = "Estado: ${tankData.status}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón "Ver detalles"
            TextButton(
                onClick = onTankClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF1976D2)
                )
            ) {
                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun TankIllustration(
    fillLevel: Float,
    modifier: Modifier = Modifier
) {
    val animatedFillLevel by animateFloatAsState(
        targetValue = fillLevel,
        animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        label = "fill_level"
    )

    Canvas(modifier = modifier) {
        val tankWidth = size.width * 0.7f
        val tankHeight = size.height * 0.8f
        val tankLeft = (size.width - tankWidth) / 2f
        val tankTop = size.height * 0.1f

        // Dibujar el contenedor del tanque
        drawRoundRect(
            color = Color(0xFFE3F2FD),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
        )

        // Dibujar el agua
        val waterHeight = (animatedFillLevel / 100f) * tankHeight
        if (waterHeight > 0) {
            drawRoundRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(tankLeft, tankTop + tankHeight - waterHeight),
                size = Size(tankWidth, waterHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
        }

        // Dibujar el borde del tanque
        drawRoundRect(
            color = Color(0xFF1976D2),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Dibujar líneas horizontales (segmentos del tanque)
        for (i in 1..4) {
            val y = tankTop + (tankHeight / 5f) * i
            drawLine(
                color = Color(0xFF1976D2).copy(alpha = 0.3f),
                start = Offset(tankLeft, y),
                end = Offset(tankLeft + tankWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}