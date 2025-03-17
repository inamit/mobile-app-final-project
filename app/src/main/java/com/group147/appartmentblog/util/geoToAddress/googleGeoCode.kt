package com.group147.appartmentblog.util.geoToAddress

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class GoogleGeocodeResponse(
    val results: List<Result>,
    val status: String
)

@Serializable
data class Result(
    @SerialName("formatted_address") val formattedAddress: String
)

object JsonUtil {
    val json = Json {
        ignoreUnknownKeys = true // Avoid failures on unknown fields
        isLenient = true         // Allow lenient parsing
        encodeDefaults = true    // Encode default values
    }
}

suspend fun getGoogleAddressFromLatLng(latitude: Double, longitude: Double, apiKey: String): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("GeoAPI", "HTTP error: ${connection.responseCode}")
            return@withContext null
        }

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val googleResponse = JsonUtil.json.decodeFromString<GoogleGeocodeResponse>(response)
        if (googleResponse.status == "OK" && googleResponse.results.isNotEmpty()) {
            return@withContext googleResponse.results[0].formattedAddress
        } else {
            Log.e("GeoAPI", "API response error: $response")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("GeoAPI", "Error during Google Maps API request", e)
        return@withContext null
    }
}