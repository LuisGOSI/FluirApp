package com.example.fluirapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluirapp.mqtt.MqttClientManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init{
        startPeriodicFirestoreUpdates()
    }

    private fun startPeriodicFirestoreUpdates(){
        viewModelScope.launch {
            while (true) {
                val tank1Level = MqttClientManager.tank1DataFlow.value
                val tank2Level = MqttClientManager.tank2DataFlow.value

                tank1Level?.let { level ->
                    createNewTankRecord("tank_1", level)
                }

                tank2Level?.let { level ->
                    createNewTankRecord("tank_2", level)
                }

                delay(60000)

            }
        }
    }

    private suspend fun createNewTankRecord(tankId: String, level: Float){

        val currentTime = System.currentTimeMillis()
        val readableTime = dateFormat.format(Date(currentTime))

        val tankRecord = hashMapOf(
            "tank_id" to tankId,
            "level" to level,
            "timestamp" to currentTime,
            "readable_time" to readableTime
        )

        try {
            firestore.collection("lecturas_tanques")
                .add(tankRecord)
                .await()
        }catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onCleared() {
        super.onCleared()
        MqttClientManager.disconnect()
    }
}