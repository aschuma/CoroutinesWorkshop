package com.kotlinconf.workshop.network

import com.kotlinconf.workshop.WorkshopServerConfig.HOST
import com.kotlinconf.workshop.WorkshopServerConfig.PORT
import com.kotlinconf.workshop.WorkshopServerConfig.WS_SERVER_URL
import com.kotlinconf.workshop.kettle.CelsiusTemperature
import com.kotlinconf.workshop.util.log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class KettleService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private val host = "http://0.0.0.0:9020/"
    private val onEndpoint = "$host/kettle/on"
    private val offEndpoint = "$host/kettle/off"
    private val temperatureEndpoint = "$host/kettle/temperature"

    private suspend fun openWebSocketSession(): DefaultClientWebSocketSession {
        return client.webSocketSession(
            method = HttpMethod.Get,
            host = HOST,
            port = PORT,
            path = "/kettle-ws"
        ).also {
            log("Opening a web socket session for $WS_SERVER_URL/kettle-ws")
        }
    }

    suspend fun switchOn(desiredTemperature: CelsiusTemperature) {
        client.post(onEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(desiredTemperature)
        }
    }

    suspend fun switchOff() {
        client.post(offEndpoint)
    }

    suspend fun getTemperature(): CelsiusTemperature {
        return client.get(temperatureEndpoint).body<CelsiusTemperature>()
            .also { log("Loading temperature: $it Celsius") }
    }

    fun observeTemperature(): Flow<CelsiusTemperature> = flow {
        while (true) {
            delay(1000)
            emit(getTemperature())
        }
    }

    fun observeTemperatureViaWebsockets(): Flow<CelsiusTemperature> = flow {
        val socketSession = openWebSocketSession()
        while (socketSession.isActive) {
            val temperature: CelsiusTemperature = socketSession.receiveDeserialized()
            log("Received element via websocket: $temperature")
            emit(temperature)
        }
    }
}