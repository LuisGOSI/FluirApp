package com.example.fluirapp.screens.pumps

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fluirapp.mqtt.MqttClientManager
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ThresholdSettingsScreen(
    navController: NavController
) {
    val mqttManager = MqttClientManager

    // Estados
    var selectedPump by remember { mutableStateOf("Bomba 1") }
    var minThreshold by remember { mutableStateOf(20f) }
    var maxThreshold by remember { mutableStateOf(80f) }

    // Lista de bombas disponibles
    val pumps = listOf("Bomba 1", "Bomba 2")

    // Efectos de agua (como en tus otras pantallas)
    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF367BBC).copy(alpha = 0.1f))
        ) {
            // Fondo con efectos de agua
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawWaterEffects(waveOffset, 0f)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Card principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2075C6).copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Título
                        Text(
                            "Configuración de umbrales",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Selector de bomba
                        Column {
                            Text(
                                "Selecciona la bomba a configurar:",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF1976D2)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                pumps.forEach { pump ->
                                    FilterChip(
                                        selected = selectedPump == pump,
                                        onClick = { selectedPump = pump },
                                        label = { Text(pump) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF1976D2),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFE3F2FD)
                                        ),
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }

                        // Configuración de mínimo
                        Column {
                            Text(
                                "Porcentaje mínimo permitido:",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF1976D2)
                                )
                            )

                            SliderWithLabel(
                                value = minThreshold,
                                onValueChange = { minThreshold = it },
                                valueRange = 0f..maxThreshold - 5f,
                                text = "${minThreshold.toInt()}%"
                            )
                        }

                        // Configuración de máximo
                        Column {
                            Text(
                                "Porcentaje máximo permitido:",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF1976D2)
                                )
                            )

                            SliderWithLabel(
                                value = maxThreshold,
                                onValueChange = { maxThreshold = it },
                                valueRange = minThreshold + 5f..100f,
                                text = "${maxThreshold.toInt()}%"
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón Guardar
                        Button(
                            onClick = {
                                // Publicar configuración por MQTT
                                val topic = if (selectedPump == "Bomba 1")
                                    "fluir/umbrales/1" else "fluir/umbrales/2"

                                val payload = JSONObject().apply {
                                    put("min", minThreshold.toInt())
                                    put("max", maxThreshold.toInt())
                                }.toString()

                                mqttManager.publish(topic, payload)

                                // Regresar a pantalla anterior
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text(
                                "Guardar configuración",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Componente reutilizable para Slider con valor numérico
@Composable
fun SliderWithLabel(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    text: String
) {
    Column {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1976D2),
                activeTrackColor = Color(0xFF1976D2),
                inactiveTrackColor = Color(0xFF90CAF9)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center)
    }
}

// Función de efectos de agua (similar a tus otras pantallas)
fun DrawScope.drawWaterEffects(waveOffset: Float, dropAnimation: Float) {
    val width = size.width
    val height = size.height

    for (i in 0..2) {
        val amplitude = 15f + i * 8f
        val frequency = 0.006f + i * 0.003f
        val yPos = height * 0.85f + i * 25f
        val alpha = 0.04f - i * 0.008f

        val path = Path()
        path.moveTo(0f, yPos)

        for (x in 0..width.toInt() step 8) {
            val y = yPos + amplitude * sin(x * frequency + waveOffset + i * PI.toFloat() / 4)
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(path, Color(0xFF367BBC).copy(alpha = alpha))
    }
}