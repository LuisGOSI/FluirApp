package com.example.fluirapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluirapp.mqtt.MqttClientManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {
    val sensorData = MqttClientManager.dataFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        MqttClientManager.connectAndSubscribe("fluir/sensor/agua")
    }

    override fun onCleared() {
        super.onCleared()
        MqttClientManager.disconnect()
    }
}