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

    // StateFlows separados para cada tanque
    private val _tank1DataFlow = MutableStateFlow<Float?>(null)
    val tank1DataFlow: StateFlow<Float?> = _tank1DataFlow

    private val _tank2DataFlow = MutableStateFlow<Float?>(null)
    val tank2DataFlow: StateFlow<Float?> = _tank2DataFlow

    // StateFlows separados para cada caudal de las colonias
    private val _colony1Flow = MutableStateFlow<Float?>(null)
    val colony1Flow: StateFlow<Float?> = _colony1Flow

    private val _colony2Flow = MutableStateFlow<Float?>(null)
    val colony2Flow: StateFlow<Float?> = _colony2Flow


    // StateFlow genérico (mantener compatibilidad)
    private val _dataFlow = MutableStateFlow<Float?>(null)
    val dataFlow: StateFlow<Float?> = _dataFlow

    private var isConnected = false

    fun connect() {
        if (isConnected || ::client.isInitialized) return

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
                isConnected = true
                println("MQTT conectado exitosamente")
            } else {
                println("Error al conectar MQTT: ${throwable.message}")
                isConnected = false
            }
        }
    }

    fun subscribeToFlow1(topic: String){
        if (!isConnected) connect()

        client.subscribeWith()
            .topicFilter(topic)
            .callback{ publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    try {
                        val bytes = ByteArray(payload.remaining())
                        payload.get(bytes)
                        val msg = String(bytes, StandardCharsets.UTF_8).trim()

                        println("Caudal colonia 1 - Mensaje recibido: '$msg'")

                        val value = msg.toFloatOrNull()
                        if (value != null) {
                            _colony1Flow.value = value
                            _dataFlow.value = value // Para compatibilidad
                        } else {
                            println("Caudal colonia 1 - No es un número válido: '$msg'")
                        }
                    } catch (e: Exception) {
                        println("Caudal colonia 1 - Excepción procesando payload: ${e.message}")
                    }
                }
            }
            .send()
    }

    fun subscribeToFlow2(topic: String){
        if (!isConnected) connect()

        client.subscribeWith()
            .topicFilter(topic)
            .callback{ publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    try {
                        val bytes = ByteArray(payload.remaining())
                        payload.get(bytes)
                        val msg = String(bytes, StandardCharsets.UTF_8).trim()

                        println("Caudal colonia 2 - Mensaje recibido: '$msg'")

                        val value = msg.toFloatOrNull()
                        if (value != null) {
                            _colony2Flow.value = value
                        } else {
                            println("Caudal colonia 2 - No es un número válido: '$msg'")
                        }
                    } catch (e: Exception) {
                        println("Caudal colonia 2 - Excepción procesando payload: ${e.message}")
                    }
                }
            }
            .send()
    }

    fun subscribeToTank1(topic: String) {
        if (!isConnected) {
            connect()
        }

        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    try {
                        val bytes = ByteArray(payload.remaining())
                        payload.get(bytes)
                        val msg = String(bytes, StandardCharsets.UTF_8).trim()

                        println("Tanque 1 - Mensaje recibido: '$msg'")

                        val value = msg.toFloatOrNull()
                        if (value != null) {
                            _tank1DataFlow.value = value
                            _dataFlow.value = value // Para compatibilidad
                        } else {
                            println("Tanque 1 - No es un número válido: '$msg'")
                        }
                    } catch (e: Exception) {
                        println("Tanque 1 - Excepción procesando payload: ${e.message}")
                    }
                }
            }
            .send()
    }

    fun subscribeToTank2(topic: String) {
        if (!isConnected) {
            connect()
        }

        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    try {
                        val bytes = ByteArray(payload.remaining())
                        payload.get(bytes)
                        val msg = String(bytes, StandardCharsets.UTF_8).trim()

                        println("Tanque 2 - Mensaje recibido: '$msg'")

                        val value = msg.toFloatOrNull()
                        if (value != null) {
                            _tank2DataFlow.value = value
                        } else {
                            println("Tanque 2 - No es un número válido: '$msg'")
                        }
                    } catch (e: Exception) {
                        println("Tanque 2 - Excepción procesando payload: ${e.message}")
                    }
                }
            }
            .send()
    }

    fun subscribeToAlerts(topic: String, alertCallback: (String) -> Unit) {
        if (!isConnected) {
            connect()
        }

        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    try {
                        val bytes = ByteArray(payload.remaining())
                        payload.get(bytes)
                        val msg = String(bytes, StandardCharsets.UTF_8).trim()

                        println("Alerta - Mensaje recibido: '$msg'")

                        alertCallback(msg)
                    } catch (e: Exception) {
                        println("Alerta - Excepción procesando payload: ${e.message}")
                    }
                }
            }
            .send()
    }

    fun publish(topic: String, message: String) {
        if (!isConnected) {
            connect()
        }

        client.publishWith()
            .topic(topic)
            .payload(message.toByteArray(StandardCharsets.UTF_8))
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    println("Error al publicar en el topic '$topic': ${throwable.message}")
                } else {
                    println("Mensaje publicado en el topic '$topic': '$message'")
                }
            }
    }

    fun disconnect() {
        if (::client.isInitialized && isConnected) {
            client.disconnect()
            isConnected = false
            println("MQTT desconectado")
        }
    }
}