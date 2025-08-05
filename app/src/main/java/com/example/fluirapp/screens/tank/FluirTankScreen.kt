package com.example.fluirapp.screens.tank

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fluirapp.mqtt.MqttClientManager

@Composable
fun TankDetailScreen(
    navController: NavController,
    tankId: String
) {

    val tank1FillLevel by MqttClientManager.tank1DataFlow.collectAsState()
    val tank2FillLevel by MqttClientManager.tank2DataFlow.collectAsState()
    val flow1Level by MqttClientManager.colony1Flow.collectAsState()
    val flow2Level by MqttClientManager.colony2Flow.collectAsState()

    // Datos del tanque según el ID
    val tankData = remember(tankId) {
        mutableStateOf(
            when (tankId) {
                "tank_1" -> TankDetailData(
                    name = "El Dorado",
                    fillLevel = 0f,
                    capacity = "600,000 L",
                    pumpPower = "40kW",
                    status = "Apagado",
                    flow = 0f,
                    operationMode = "Automático",
                    isOperating = true
                )
                else -> TankDetailData(
                    name = "Manzanares",
                    fillLevel = 0f,
                    capacity = "350A,000 L",
                    pumpPower = "30kW",
                    status = "Apagado",
                    flow = 0f,
                    operationMode = "Automático",
                    isOperating = true
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        MqttClientManager.connect()
        MqttClientManager.subscribeToTank1("fluir/tanque/1")
        MqttClientManager.subscribeToTank2("fluir/tanque/2")
        MqttClientManager.subscribeToFlow1("fluir/caudal/1")
        MqttClientManager.subscribeToFlow2("fluir/caudal/2")
    }

    LaunchedEffect(tank1FillLevel, tank2FillLevel, flow1Level, flow2Level) {
        val currentFillLevel = when (tankId) {
            "tank_1" -> tank1FillLevel ?: 0f
            else -> tank2FillLevel ?: 0f
        }

        val currentFlow = when (tankId) {
            "tank_1" -> flow1Level ?: 0f
            else -> flow2Level ?: 0f
        }

        tankData.value = tankData.value.copy(
            fillLevel = currentFillLevel,
            isOperating = currentFillLevel > 0f,
            flow = currentFlow,
            status = if (currentFillLevel > 0f) "En operación" else "Apagado"
        )
    }

    val currentTankData by tankData
    var isOperating by remember{ mutableStateOf(currentTankData.isOperating) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con botón de regreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color(0xFF1976D2)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = tankData.value.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ilustración grande del tanque
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LargeTankIllustration(
                        fillLevel = tankData.value.fillLevel,
                        modifier = Modifier.size(150.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Switch de operación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFBBDEFB)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "En operación",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Switch(
                        checked = isOperating,
                        onCheckedChange = { isOperating = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Información detallada
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow("Nivel de llenado:", "${tankData.value.fillLevel.toInt()}%")
                    DetailRow("Contenido en litros:", tankData.value.capacity)
                    DetailRow("Potencia de la bomba:", tankData.value.pumpPower)
                    DetailRow("Estado:", tankData.value.status)
                    DetailRow("Modo de operación:", tankData.value.operationMode)
                    DetailRow("Caudal de colonia :", "${tankData.value.flow.toInt()} L/s")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF666666)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun LargeTankIllustration(
    fillLevel: Float,
    modifier: Modifier = Modifier
) {
    val animatedFillLevel by animateFloatAsState(
        targetValue = fillLevel,
        animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        label = "fill_level"
    )

    Canvas(modifier = modifier) {
        val tankWidth = size.width * 0.6f
        val tankHeight = size.height * 0.8f
        val tankLeft = (size.width - tankWidth) / 2f
        val tankTop = size.height * 0.1f

        // Dibujar el contenedor del tanque
        drawRoundRect(
            color = Color(0xFFE3F2FD),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
        )

        // Dibujar el agua con gradiente
        val waterHeight = (animatedFillLevel / 100f) * tankHeight
        if (waterHeight > 0) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF64B5F6),
                        Color(0xFF1976D2)
                    )
                ),
                topLeft = Offset(tankLeft, tankTop + tankHeight - waterHeight),
                size = Size(tankWidth, waterHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            )
        }

        // Dibujar el borde del tanque
        drawRoundRect(
            color = Color(0xFF1976D2),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Dibujar líneas horizontales más detalladas
        for (i in 1..5) {
            val y = tankTop + (tankHeight / 6f) * i
            drawLine(
                color = Color(0xFF1976D2).copy(alpha = 0.4f),
                start = Offset(tankLeft, y),
                end = Offset(tankLeft + tankWidth, y),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Marcadores de nivel
        val levelMarkers = listOf(20f, 40f, 60f, 80f)
        levelMarkers.forEach { level ->
            val y = tankTop + tankHeight - (level / 100f * tankHeight)
            drawLine(
                color = Color(0xFF1976D2).copy(alpha = 0.6f),
                start = Offset(tankLeft - 10.dp.toPx(), y),
                end = Offset(tankLeft, y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1976D2).copy(alpha = 0.6f),
                start = Offset(tankLeft + tankWidth, y),
                end = Offset(tankLeft + tankWidth + 10.dp.toPx(), y),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

data class TankDetailData(
    val name: String,
    var fillLevel: Float,
    val capacity: String,
    val pumpPower: String,
    var status: String,
    var flow: Float,
    val operationMode: String,
    var isOperating: Boolean
)

