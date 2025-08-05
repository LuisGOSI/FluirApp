package com.example.fluirapp.screens.stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.max


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluirStatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Últimos 7 días") }
    var isPeriodDropdownExpanded by remember { mutableStateOf(false) }

    val periods = listOf(
        "Últimos 7 días",
        "Último mes",
        "Últimos 3 meses",
        "Último año"
    )

    // Actualizar período en el ViewModel cuando cambie
    LaunchedEffect(selectedPeriod) {
        viewModel.changePeriod(selectedPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Título
        Text(
            text = "Estadísticas",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        // Selector de período
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            PeriodDropdown(
                value = selectedPeriod,
                options = periods,
                isExpanded = isPeriodDropdownExpanded,
                onExpandedChange = { isPeriodDropdownExpanded = it },
                onValueChange = { selectedPeriod = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido principal con scroll
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Gráfica de barras - Consumo por tanque
            item {
                StatCard(title = "Consumo por Tanque") {
                    if (uiState.isLoading) {
                        LoadingIndicator()
                    } else if (uiState.errorMessage != null) {
                        ErrorMessage(
                            message = uiState.errorMessage!!,
                            onRetry = { viewModel.loadStatsData() }
                        )
                    } else {
                        BarChartComponent(
                            data = viewModel.getBarChartData(),
                            modifier = Modifier.height(300.dp)
                        )
                    }
                }
            }

            // Gráfica combinada - Temperatura vs Consumo Total
            item {
                StatCard(title = "Temperatura vs Consumo Total") {
                    if (uiState.isLoadingTemperature) {
                        LoadingIndicator()
                    } else if (uiState.temperatureErrorMessage != null) {
                        ErrorMessage(
                            message = uiState.temperatureErrorMessage!!,
                            onRetry = { viewModel.loadTemperatureConsumptionData() }
                        )
                    } else {
                        CombinedChartComponent(
                            data = viewModel.getCombinedChartData(),
                            modifier = Modifier.height(320.dp)
                        )
                    }
                }
            }

            // Gráfica de pastel - Distribución por tanque
            item {
                StatCard(title = "Distribución por Tanque") {
                    if (uiState.isLoadingPieChart) {
                        LoadingIndicator()
                    } else if (uiState.pieChartErrorMessage != null) {
                        ErrorMessage(
                            message = uiState.pieChartErrorMessage!!,
                            onRetry = { viewModel.loadPieChartData() }
                        )
                    } else {
                        PieChartComponent(
                            data = viewModel.getPieChartData(),
                            modifier = Modifier.height(250.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinedChartComponent(
    data: List<CombinedChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay datos disponibles",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Leyenda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Consumo Total (m³)", Color(0xFF64B5F6))
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem("Temperatura (°C)", Color(0xFFFF6B6B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfica combinada con Canvas
        CombinedChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Estadísticas resumen
        val avgConsumption = data.map { it.totalConsumption }.average()
        val avgTemperature = data.map { it.temperature }.average()
        val maxTemperature = data.maxOfOrNull { it.temperature } ?: 0f
        val maxConsumption = data.maxOfOrNull { it.totalConsumption } ?: 0f

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                StatsValue("Consumo Promedio", "${String.format("%.1f", avgConsumption)}m³")
                Spacer(modifier = Modifier.height(8.dp))
                StatsValue("Consumo Máximo", "${String.format("%.1f", maxConsumption)}m³")
            }
            Column {
                StatsValue("Temp. Promedio", "${String.format("%.1f", avgTemperature)}°C")
                Spacer(modifier = Modifier.height(8.dp))
                StatsValue("Temp. Máxima", "${String.format("%.1f", maxTemperature)}°C")
            }
        }
    }
}

@Composable
private fun CombinedChart(
    data: List<CombinedChartData>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val consumptionColor = Color(0xFF64B5F6)
    val temperatureColor = Color(0xFFFF6B6B)

    // Tomar solo los primeros 7 elementos
    val chartData = data.take(7)
    val maxConsumption = chartData.maxOfOrNull { it.totalConsumption } ?: 1f
    val maxTemperature = chartData.maxOfOrNull { it.temperature } ?: 1f
    val minTemperature = chartData.minOfOrNull { it.temperature } ?: 0f

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val chartHeight = canvasHeight * 0.75f
        val chartBottom = canvasHeight * 0.85f
        val chartTop = canvasHeight * 0.1f

        if (chartData.isNotEmpty()) {
            val barWidth = (canvasWidth / (chartData.size * 2f))
            val groupWidth = canvasWidth / chartData.size

            // Dibujar grid horizontal
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = chartBottom - (i.toFloat() / gridLines) * chartHeight
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Dibujar barras de consumo y línea de temperatura
            val temperaturePoints = mutableListOf<Offset>()

            chartData.forEachIndexed { index, item ->
                val centerX = (index + 0.5f) * groupWidth

                // Calcular altura de la barra (consumo)
                val barHeight = (item.totalConsumption / maxConsumption) * chartHeight

                // Dibujar barra de consumo
                drawRect(
                    color = consumptionColor,
                    topLeft = Offset(
                        x = centerX - barWidth / 2,
                        y = chartBottom - barHeight
                    ),
                    size = Size(barWidth, barHeight)
                )

                // Calcular posición Y para temperatura (escalado al rango de temperatura)
                val tempRange = maxTemperature - minTemperature
                val normalizedTemp = if (tempRange > 0) {
                    (item.temperature - minTemperature) / tempRange
                } else {
                    0.5f
                }
                val tempY = chartBottom - (normalizedTemp * chartHeight)

                // Agregar punto de temperatura
                temperaturePoints.add(Offset(centerX, tempY))

                // Dibujar punto de temperatura
                drawCircle(
                    color = temperatureColor,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, tempY)
                )

                // Dibujar etiquetas de fecha
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = Color.Gray.toArgb()
                        textSize = with(density) { 10.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    drawText(
                        item.date,
                        centerX,
                        chartBottom + with(density) { 20.dp.toPx() },
                        textPaint
                    )
                }
            }

            // Dibujar línea conectando puntos de temperatura
            for (i in 0 until temperaturePoints.size - 1) {
                drawLine(
                    color = temperatureColor,
                    start = temperaturePoints[i],
                    end = temperaturePoints[i + 1],
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Etiquetas del eje Y izquierdo (consumo)
            for (i in 1..gridLines) {
                val y = chartBottom - (i.toFloat() / gridLines) * chartHeight
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = consumptionColor.toArgb()
                        textSize = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    val value = (maxConsumption * i / gridLines)
                    drawText(
                        "${String.format("%.0f", value)}m³",
                        with(density) { (-10).dp.toPx() },
                        y + with(density) { 3.dp.toPx() },
                        textPaint
                    )
                }
            }

            // Etiquetas del eje Y derecho (temperatura)
            for (i in 1..gridLines) {
                val y = chartBottom - (i.toFloat() / gridLines) * chartHeight
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = temperatureColor.toArgb()
                        textSize = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.LEFT
                    }

                    val tempValue = minTemperature + (maxTemperature - minTemperature) * i / gridLines
                    drawText(
                        "${String.format("%.1f", tempValue)}°C",
                        canvasWidth + with(density) { 10.dp.toPx() },
                        y + with(density) { 3.dp.toPx() },
                        textPaint
                    )
                }
            }
        }
    }
}

    @Composable
    private fun StatCard(
        title: String,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2075C6),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                content()
            }
        }
    }

    @Composable
    private fun BarChartComponent(
        data: List<BarChartData>,
        modifier: Modifier = Modifier
    ) {
        if (data.isEmpty()) {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay datos disponibles",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        Column(modifier = modifier.fillMaxWidth()) {
            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem("El Dorado", Color(0xFF64B5F6))
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem("Manzanares", Color(0xFF42A5F5))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfica de barras con Canvas
            CustomBarChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Valores promedio
            val avgElDorado = data.map { it.elDorado }.average()
            val avgManzanares = data.map { it.manzanares }.average()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsValue("Promedio El Dorado", "${String.format("%.1f", avgElDorado)}m³")
                StatsValue("Promedio Manzanares", "${String.format("%.1f", avgManzanares)}m³")
            }
        }
    }

    @Composable
    private fun CustomBarChart(
        data: List<BarChartData>,
        modifier: Modifier = Modifier
    ) {
        val density = LocalDensity.current
        val elDoradoColor = Color(0xFF64B5F6)
        val manzanaresColor = Color(0xFF42A5F5)

        // Tomar solo los primeros 7 elementos para evitar sobrecarga visual
        val chartData = data.take(7)
        val maxValue = chartData.maxOfOrNull { max(it.elDorado, it.manzanares) } ?: 1f

        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight * 0.8f
            val chartBottom = canvasHeight * 0.9f

            if (chartData.isNotEmpty()) {
                val barWidth = (canvasWidth / (chartData.size * 4f)) // Espacio para 2 barras + margen
                val groupWidth = canvasWidth / chartData.size

                chartData.forEachIndexed { index, item ->
                    val groupCenterX = (index + 0.5f) * groupWidth

                    // Calcular alturas proporcionales
                    val elDoradoHeight = (item.elDorado / maxValue) * chartHeight
                    val manzanaresHeight = (item.manzanares / maxValue) * chartHeight

                    // Dibujar barra El Dorado
                    drawRect(
                        color = elDoradoColor,
                        topLeft = Offset(
                            x = groupCenterX - barWidth - barWidth * 0.1f,
                            y = chartBottom - elDoradoHeight
                        ),
                        size = Size(barWidth, elDoradoHeight)
                    )

                    // Dibujar barra Manzanares
                    drawRect(
                        color = manzanaresColor,
                        topLeft = Offset(
                            x = groupCenterX + barWidth * 0.1f,
                            y = chartBottom - manzanaresHeight
                        ),
                        size = Size(barWidth, manzanaresHeight)
                    )

                    // Dibujar etiquetas de fecha
                    drawContext.canvas.nativeCanvas.apply {
                        val textPaint = android.graphics.Paint().apply {
                            color = Color.Gray.toArgb()
                            textSize = with(density) { 10.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        drawText(
                            item.date,
                            groupCenterX,
                            chartBottom + with(density) { 20.dp.toPx() },
                            textPaint
                        )
                    }
                }

                // Dibujar líneas de referencia horizontales
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = chartBottom - (i.toFloat() / gridLines) * chartHeight
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Etiquetas del eje Y
                    if (i > 0) {
                        drawContext.canvas.nativeCanvas.apply {
                            val textPaint = android.graphics.Paint().apply {
                                color = Color.Gray.toArgb()
                                textSize = with(density) { 9.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }

                            val value = (maxValue * i / gridLines)
                            drawText(
                                "${String.format("%.0f", value)}m³",
                                with(density) { (-5).dp.toPx() },
                                y + with(density) { 3.dp.toPx() },
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LegendItem(label: String, color: Color) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }

    @Composable
    private fun StatsValue(label: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2075C6)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun PlaceholderChart(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFF5F5F5),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun LoadingIndicator() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF64B5F6),
                modifier = Modifier.size(48.dp)
            )
        }
    }

    @Composable
    private fun ErrorMessage(
        message: String,
        onRetry: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error al cargar datos",
                fontSize = 16.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF64B5F6)
                )
            ) {
                Text("Reintentar")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PeriodDropdown(
        value: String,
        options: List<String>,
        isExpanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onValueChange: (String) -> Unit
    ) {
        val animatedScale by animateFloatAsState(
            targetValue = if (isExpanded) 1.05f else 1f,
            animationSpec = tween(durationMillis = 150),
            label = "scale"
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.scale(animatedScale)
        ) {
            Card(
                modifier = Modifier
                    .menuAnchor()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onExpandedChange(!isExpanded) },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2075C6)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF2075C6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(
                    Color.White,
                    RoundedCornerShape(16.dp)
                )
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF64B5F6).copy(alpha = 0.1f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 150),
                        label = "background"
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF2075C6) else Color.Black
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            onExpandedChange(false)
                        },
                        modifier = Modifier.background(backgroundColor)
                    )
                }
            }
        }
    }

@Composable
fun PieChartComponent(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay datos disponibles", color = Color.Gray)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gráfico de pastel
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            PieChartCanvas(data = data)

            // Texto en el centro
            Text(
                text = "100%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        TotalPieChart(data[0])


        // Leyenda
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            data.forEach { item ->
                LegendItem(item)
            }
        }
    }
}

@Composable
private fun PieChartCanvas(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { mutableStateListOf<Float>().apply {
        addAll(data.map { 0f })
    } }

    LaunchedEffect(Unit) {
        animationPlayed = true
        data.forEachIndexed { index, _ ->
            delay(100) // Pequeño delay para animación escalonada
            animatedProgress[index] = data[index].value / 100f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 1.8f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f // Empieza desde arriba

        data.forEachIndexed { index, item ->
            val sweepAngle = 360f * animatedProgress[index]

            drawArc(
                color = Color(item.color),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(canvasSize, canvasSize),
                topLeft = Offset(
                    (size.width - canvasSize) / 2f,
                    (size.height - canvasSize) / 2f
                )
            )

            startAngle += sweepAngle
        }

        // Círculo central para efecto donut
        drawCircle(
            color = Color.White,
            radius = radius * 0.25f,
            center = center
        )
    }
}

@Composable
private fun LegendItem(item: PieChartData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(item.color), CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${item.label} (${item.value}%)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun TotalPieChart(item: PieChartData) {
    Text(
        text = "Total: ${item.liters}L",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
}
