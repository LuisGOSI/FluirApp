package com.example.fluirapp.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val historyCollection = db.collection("consumo_diario")

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        historyCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.mapNotNull { doc ->
                    try {
                        val date = doc.getString("date") ?: ""

                        // Obtener datos de El_dorado
                        val elDorado = doc.get("El_dorado") as? Map<String, Any>
                        val elDoradoLitros = (elDorado?.get("total_litros") as? Number)?.toDouble() ?: 0.0

                        // Obtener datos de Manzanares
                        val manzanares = doc.get("Manzanares") as? Map<String, Any>
                        val manzanaresLitros = (manzanares?.get("total_litros") as? Number)?.toDouble() ?: 0.0

                        // Calcular consumo total de agua (suma de ambos tanques)
                        val totalWaterConsumption = elDoradoLitros + manzanaresLitros

                        // Para energía, usaremos un cálculo estimado o 0 si no tienes ese dato
                        // Puedes ajustar esta lógica según tus necesidades
                        val estimatedEnergyConsumption = totalWaterConsumption * 0.005 // Estimación

                        ConsumptionRecord(
                            date = date,
                            waterConsumption = totalWaterConsumption / 1000, // Convertir litros a m³
                            energyConsumption = estimatedEnergyConsumption
                        )
                    } catch (e: Exception) {
                        Log.e("HistoryViewModel", "Error parsing document ${doc.id}: ${e.message}")
                        null
                    }
                }

                Log.d("HistoryViewModel", "Loaded ${records.size} records")
                _uiState.value = _uiState.value.copy(
                    consumptionData = records,
                    isLoading = false
                )
            }
            .addOnFailureListener { exception ->
                Log.w("HistoryViewModel", "Error getting documents: ", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar los datos: ${exception.message}"
                )
            }
    }

    fun filterByMonth(month: String, year: String) {
        val monthNumber = getMonthNumber(month)
        val filteredData = _uiState.value.consumptionData.filter { record ->
            try {
                val dateParts = record.date.split("-")
                if (dateParts.size >= 3) {
                    val recordYear = dateParts[0]
                    val recordMonth = dateParts[1].toIntOrNull()
                    recordYear == year && recordMonth == monthNumber
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        _uiState.value = _uiState.value.copy(filteredData = filteredData)
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName) {
            "Enero" -> 1
            "Febrero" -> 2
            "Marzo" -> 3
            "Abril" -> 4
            "Mayo" -> 5
            "Junio" -> 6
            "Julio" -> 7
            "Agosto" -> 8
            "Septiembre" -> 9
            "Octubre" -> 10
            "Noviembre" -> 11
            "Diciembre" -> 12
            else -> 1
        }
    }
}

data class HistoryUiState(
    val consumptionData: List<ConsumptionRecord> = emptyList(),
    val filteredData: List<ConsumptionRecord> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ConsumptionRecord(
    val date: String = "",
    val waterConsumption: Double = 0.0,
    val energyConsumption: Double = 0.0
)