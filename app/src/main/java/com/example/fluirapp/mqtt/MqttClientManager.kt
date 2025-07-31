package com.example.fluirapp.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.text.Charsets.UTF_8

object MqttClientManager {
    private lateinit var client: Mqtt5AsyncClient

    private val _dataFlow = MutableStateFlow<Float?>(null)
    val dataFlow: StateFlow<Float?> = _dataFlow

    fun connectAndSubscribe(topic: String) {
        client = MqttClient.builder()
            .useMqttVersion5()
            .identifier(UUID.randomUUID().toString())
            .serverHost("07f4912ab0d1438abfb5d6e4dc579e4a.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .simpleAuth()
            .username("fluirTeam")
            .password(UTF_8.encode("Cisco123"))
            .applySimpleAuth()
            .buildAsync()

        client.connect().whenComplete { _, throwable ->
            if (throwable == null) {
                client.subscribeWith()
                    .topicFilter(topic)
                    .callback { publish ->
                        val payload = publish.payload.orElse(null)

                        if (payload != null) {
                            try {
                                val bytes = ByteArray(payload.remaining())
                                payload.get(bytes)
                                val msg = String(bytes, StandardCharsets.UTF_8).trim()

                                println("📨 Mensaje recibido: '$msg'")

                                val value = msg.toFloatOrNull()
                                if (value != null) {
                                    _dataFlow.value = value
                                } else {
                                    println("⚠️ No es un número válido: '$msg'")
                                }
                            } catch (e: Exception) {
                                println("❌ Excepción procesando payload: ${e.message}")
                            }
                        } else {
                            println("⚠️ Payload nulo.")
                        }
                    }
                    .send()
            } else {
                println("Error al conectar MQTT: ${throwable.message}")
            }
        }
    }

    fun disconnect() {
        if (::client.isInitialized) {
            client.disconnect()
        }
    }
}