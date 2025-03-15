package com.group147.appartmentblog.util.geoToAdress

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

@Serializable
data class GoogleGeocodeResponse(
    val results: List<Result>,
    val status: String
)

@Serializable
data class Result(
    @SerialName("formatted_address") val formattedAddress: String
)

suspend fun getGoogleAddressFromLatLng(latitude: Double, longitude: Double, apiKey: String): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        val googleResponse = Json.decodeFromString<GoogleGeocodeResponse>(response)
        if (googleResponse.status == "OK" && googleResponse.results.isNotEmpty()) {
            return@withContext googleResponse.results[0].formattedAddress
        } else {
            return@withContext null
        }
    } catch (e: Exception) {
        println("Error during Google Maps API request: ${e.message}")
        return@withContext null
    }
}