package com.example.fluirapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluirapp.mqtt.MqttClientManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        MqttClientManager.disconnect()
    }
}