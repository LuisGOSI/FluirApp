package com.example.fluirapp.screens.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fluirapp.screens.settings.drawWaterEffects
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluirHistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedMonth by remember { mutableStateOf("Abril") }
    var selectedYear by remember { mutableStateOf("2025") }
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val years = listOf("2023", "2024", "2025")

    // Filtrar datos cuando cambien los valores seleccionados
    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.filterByMonth(selectedMonth, selectedYear)
    }

    // Usar datos filtrados del ViewModel
    val displayData = if (uiState.filteredData.isEmpty() && !uiState.isLoading) {
        uiState.consumptionData
    } else {
        uiState.filteredData
    }


    // Animaciones de fondo
    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val dropAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {



        // Título
        Text(
            text = "Histórico",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        // Card principal con la tabla
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF64B5F6),
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TableHeaderCell("Fecha", Modifier.weight(1f))
                    TableHeaderCell("Consumo\nde agua", Modifier.weight(1f))
                    TableHeaderCell("Consumo\nde energía", Modifier.weight(1f))
                }

                // Filas de datos
                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    if (uiState.isLoading) {
                        item {
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
                    } else if (displayData.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (uiState.errorMessage != null)
                                            "Error al cargar datos"
                                        else "No hay datos para $selectedMonth $selectedYear",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    if (uiState.errorMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = uiState.errorMessage!!,
                                            fontSize = 12.sp,
                                            color = Color.Red,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(displayData) { record ->
                            HistoryRow(record = record)
                            if (record != displayData.last()) {
                                Divider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Filtros de fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    // Dropdown para mes
                    FilterDropdown(
                        value = selectedMonth,
                        options = months,
                        isExpanded = isMonthDropdownExpanded,
                        onExpandedChange = { isMonthDropdownExpanded = it },
                        onValueChange = { selectedMonth = it }
                    )

                    // Dropdown para año
                    FilterDropdown(
                        value = selectedYear,
                        options = years,
                        isExpanded = isYearDropdownExpanded,
                        onExpandedChange = { isYearDropdownExpanded = it },
                        onValueChange = { selectedYear = it }
                    )
                }
            }
        }
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawWaterEffects(waveOffset, dropAnimation)
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun HistoryRow(record: ConsumptionRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(formatDate(record.date), Modifier.weight(1f))
        TableCell("${String.format("%.1f", record.waterConsumption)}m³", Modifier.weight(1f))
        TableCell("${String.format("%.1f", record.energyConsumption)}kWh", Modifier.weight(1f))
    }
}

// Función para formatear la fecha de YYYY-MM-DD a DD/MM/YYYY
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
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
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2075C6)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF2075C6),
                    modifier = Modifier.size(20.dp)
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