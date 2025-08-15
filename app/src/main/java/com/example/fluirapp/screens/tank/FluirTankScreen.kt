package com.example.fluirapp.screens.tank

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate

@Composable
fun TankDetailScreen(
    navController: NavController,
    tankId: String
) {
    val tank1FillLevel by MqttClientManager.tank1DataFlow.collectAsState()
    val tank2FillLevel by MqttClientManager.tank2DataFlow.collectAsState()
    val flow1Level by MqttClientManager.colony1Flow.collectAsState()
    val flow2Level by MqttClientManager.colony2Flow.collectAsState()

    // Datos del tanque según ID
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
                    capacity = "350,000 L",
                    pumpPower = "30kW",
                    status = "Apagado",
                    flow = 0f,
                    operationMode = "Automático",
                    isOperating = true
                )
            }
        )
    }

    // Estados para API
    var alerta by remember { mutableStateOf<String?>(null) }
    var consumo by remember { mutableStateOf<Double?>(null) }

    // Lista de ubicaciones para demo
    val locations = listOf(
        "León, Gto" to Pair(21.12, -101.68),
        "CDMX" to Pair(19.43, -99.13),
        "Guadalajara" to Pair(20.67, -103.35),
        "Monterrey" to Pair(25.67, -100.31),
        "Alaska" to Pair(61.37, -152.40),
        "Hawaii" to Pair(19.89, -155.50),
        "Tokio, Japón" to Pair(35.68, 139.76),
        "Buenos Aires, Argentina" to Pair(-34.61, -58.38),
    )
    var selectedLocation by remember { mutableStateOf(locations[0]) }
    var expanded by remember { mutableStateOf(false) }

    // Función para obtener datos de predicción
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchPrediction(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            try {
                val date = LocalDate.now().toString()
                val url =
                    "https://predictoragua.onrender.com/predict?date=$date&lat=$lat&lon=$lon"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body()?.string() ?: return@withContext

                val json = JSONObject(body)
                val coloniaKey = if (tankId == "tank_1") "El_dorado" else "Manzanares"
                val coloniaData = json.getJSONObject(coloniaKey)

                alerta = coloniaData.getString("alerta")
                consumo = coloniaData.getDouble("consumo_predecido")
            } catch (e: Exception) {
                alerta = "Error al obtener datos"
                consumo = null
            }
        }
    }

    // Conexión MQTT y primera petición API
    LaunchedEffect(Unit) {
        MqttClientManager.connect()
        MqttClientManager.subscribeToTank1("fluir/tanque/1")
        MqttClientManager.subscribeToTank2("fluir/tanque/2")
        MqttClientManager.subscribeToFlow1("fluir/caudal/1")
        MqttClientManager.subscribeToFlow2("fluir/caudal/2")
        fetchPrediction(selectedLocation.second.first, selectedLocation.second.second)
    }

    // Actualización con MQTT
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
    var isOperating by remember { mutableStateOf(currentTankData.isOperating) }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
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

            // Banner API
            alerta?.let { alertaValue ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Alerta: $alertaValue", style = MaterialTheme.typography.titleMedium)
                        consumo?.let {
                            Text(
                                "Consumo predicho: ${"%,.2f".format(it)} L",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Selector ubicación
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedLocation.first)
                }

                val scope = rememberCoroutineScope()

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location.first) },
                            onClick = {
                                selectedLocation = location
                                expanded = false
                                scope.launch {
                                    fetchPrediction(location.second.first, location.second.second)
                                }
                            }
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tanque
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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

            // Switch operación
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
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

            // Detalles
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF666666))
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
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "fill_level"
    )
    Canvas(modifier = modifier) {
        val tankWidth = size.width * 0.6f
        val tankHeight = size.height * 0.8f
        val tankLeft = (size.width - tankWidth) / 2f
        val tankTop = size.height * 0.1f

        drawRoundRect(
            color = Color(0xFFE3F2FD),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
        )
        val waterHeight = (animatedFillLevel / 100f) * tankHeight
        if (waterHeight > 0) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                ),
                topLeft = Offset(tankLeft, tankTop + tankHeight - waterHeight),
                size = Size(tankWidth, waterHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            )
        }
        drawRoundRect(
            color = Color(0xFF1976D2),
            topLeft = Offset(tankLeft, tankTop),
            size = Size(tankWidth, tankHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        for (i in 1..5) {
            val y = tankTop + (tankHeight / 6f) * i
            drawLine(
                color = Color(0xFF1976D2).copy(alpha = 0.4f),
                start = Offset(tankLeft, y),
                end = Offset(tankLeft + tankWidth, y),
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
