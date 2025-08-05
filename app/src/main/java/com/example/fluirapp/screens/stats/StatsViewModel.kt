package com.example.fluirapp.screens.stats

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Response

// Modelo de datos para la respuesta de la API
data class ConsumptionData(
    val date: String,
    val El_dorado: Double,
    val Manzanares: Double
)

// Modelo para la API de temperatura y consumo
data class TemperatureConsumptionData(
    val date: String,
    val temperatura: Double,
    val consumo_total: Double
)

//Modelo de datos para la grafica de participacion de colonias
data class PieChartApiResponse(
    val El_dorado: Double,
    val Manzanares: Double,
    val total_litros: Double
)

// Interface para la API
interface StatsApiService {
    @GET("consumo_diario") // Endpoint para datos de tanques
    suspend fun getConsumptionData(): Response<List<ConsumptionData>>

    @GET("temp_vs_consumo") // Nuevo endpoint para temperatura y consumo
    suspend fun getTemperatureConsumptionData(): Response<List<TemperatureConsumptionData>>

    @GET("participacion_vecindario") // Endpoint para datos de participación de colonias
    suspend fun getPieChartData(): Response<PieChartApiResponse>
}

// Estados de la UI
data class StatsUiState(
    val consumptionData: List<ConsumptionData> = emptyList(),
    val temperatureConsumptionData: List<TemperatureConsumptionData> = emptyList(),
    val pieChartData: PieChartApiResponse? = null,
    val isLoading: Boolean = false,
    val isLoadingTemperature: Boolean = false,
    val isLoadingPieChart: Boolean = false,
    val errorMessage: String? = null,
    val temperatureErrorMessage: String? = null,
    val pieChartErrorMessage: String? = null,
    val selectedPeriod: String = "Últimos 7 días"
)

class StatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    // Configuración de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://predictoragua.onrender.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(StatsApiService::class.java)

    init {
        loadStatsData()
        loadTemperatureConsumptionData()
        loadPieChartData()
    }

    fun loadStatsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = apiService.getConsumptionData()

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    Log.d("StatsViewModel", "Loaded ${data.size} consumption records")

                    _uiState.value = _uiState.value.copy(
                        consumptionData = data,
                        isLoading = false
                    )
                } else {
                    val errorMsg = "Error del servidor: ${response.code()}"
                    Log.e("StatsViewModel", errorMsg)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                Log.e("StatsViewModel", errorMsg, e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun loadTemperatureConsumptionData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTemperature = true, temperatureErrorMessage = null)

            try {
                val response = apiService.getTemperatureConsumptionData()

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    Log.d("StatsViewModel", "Loaded ${data.size} temperature-consumption records")

                    _uiState.value = _uiState.value.copy(
                        temperatureConsumptionData = data,
                        isLoadingTemperature = false
                    )
                } else {
                    val errorMsg = "Error del servidor: ${response.code()}"
                    Log.e("StatsViewModel", errorMsg)
                    _uiState.value = _uiState.value.copy(
                        isLoadingTemperature = false,
                        temperatureErrorMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                Log.e("StatsViewModel", errorMsg, e)
                _uiState.value = _uiState.value.copy(
                    isLoadingTemperature = false,
                    temperatureErrorMessage = errorMsg
                )
            }
        }
    }

    fun loadPieChartData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingPieChart = true,
                pieChartErrorMessage = null
            )

            try {
                val response = apiService.getPieChartData()

                if (response.isSuccessful) {
                    val data = response.body()
                    _uiState.value = _uiState.value.copy(
                        pieChartData = data,
                        isLoadingPieChart = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPieChart = false,
                        pieChartErrorMessage = "Error del servidor: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPieChart = false,
                    pieChartErrorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changePeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        filterDataByPeriod(period)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterDataByPeriod(period: String) {
        val allConsumptionData = _uiState.value.consumptionData // Cachear datos completos
        val allTempConsumptionData = _uiState.value.temperatureConsumptionData

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        val filteredConsumptionData = when (period) {
            "Últimos 7 días" -> {
                val startDate = today.minusDays(6)
                allConsumptionData.filter {
                    val date = LocalDate.parse(it.date, formatter)
                    !date.isBefore(startDate) && !date.isAfter(today)
                }
            }
            "Últimos 30 días" -> {
                val startDate = today.minusDays(29)
                allConsumptionData.filter {
                    val date = LocalDate.parse(it.date, formatter)
                    !date.isBefore(startDate) && !date.isAfter(today)
                }
            }
            else -> allConsumptionData // Si no hay filtro, mostrar todo
        }

        val filteredTempConsumptionData = when (period) {
            "Últimos 7 días" -> {
                val startDate = today.minusDays(6)
                allTempConsumptionData.filter {
                    val date = LocalDate.parse(it.date, formatter)
                    !date.isBefore(startDate) && !date.isAfter(today)
                }
            }
            "Últimos 30 días" -> {
                val startDate = today.minusDays(29)
                allTempConsumptionData.filter {
                    val date = LocalDate.parse(it.date, formatter)
                    !date.isBefore(startDate) && !date.isAfter(today)
                }
            }
            else -> allTempConsumptionData
        }
        _uiState.value = _uiState.value.copy(consumptionData = filteredConsumptionData, temperatureConsumptionData = filteredTempConsumptionData)
    }

    // Función para obtener datos formateados para la gráfica de barras
    fun getBarChartData(): List<BarChartData> {
        return _uiState.value.consumptionData.map { consumption ->
            BarChartData(
                date = formatDateForChart(consumption.date),
                elDorado = (consumption.El_dorado / 1000).toFloat(), // Convertir a m³
                manzanares = (consumption.Manzanares / 1000).toFloat(), // Convertir a m³
                total = ((consumption.El_dorado + consumption.Manzanares) / 1000).toFloat()
            )
        }
    }

    private fun formatDateForChart(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}" // DD/MM
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    // Función para obtener datos formateados para la gráfica combinada
    fun getCombinedChartData(): List<CombinedChartData> {
        return _uiState.value.temperatureConsumptionData.map { data ->
            CombinedChartData(
                date = formatDateForChart(data.date),
                temperature = data.temperatura.toFloat(),
                totalConsumption = (data.consumo_total / 1000).toFloat() // Convertir a m³
            )
        }
    }

    fun getPieChartData(): List<PieChartData> {
        val pieData = _uiState.value.pieChartData ?: return emptyList()

        return listOf(
            PieChartData(
                label = "El Dorado",
                value = pieData.El_dorado.toFloat(),
                liters = pieData.total_litros.toFloat(),
                color = 0xFF4CAF50.toLong() // Verde
            ),
            PieChartData(
                label = "Manzanares",
                value = pieData.Manzanares.toFloat(),
                liters = pieData.total_litros.toFloat(),
                color = 0xFF2196F3.toLong() // Azul
            )
        )
    }
}

// Modelos de datos para las gráficas
data class BarChartData(
    val date: String,
    val elDorado: Float,
    val manzanares: Float,
    val total: Float
)

data class CombinedChartData(
    val date: String,
    val temperature: Float,
    val totalConsumption: Float
)

data class LineChartData(
    val date: String,
    val value: Float
)

data class PieChartData(
    val label: String,
    val value: Float,
    val liters: Float,
    val color: Long
)