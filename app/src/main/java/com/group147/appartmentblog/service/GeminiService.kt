package com.group147.appartmentblog.service

import android.content.Context
import com.group147.appartmentblog.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class GeminiService(context: Context) {

    private val apiKey: String = context.getString(R.string.gemini_api_key)
    private val service: GeminiAIService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(GeminiAIService::class.java)
    }

    fun generateContent(input: String, callback: Callback<GeminiResponse>) {
        val request = GeminiRequest(listOf(Content(input)))
        service.generateContent(request, apiKey).enqueue(callback)
    }

    interface GeminiAIService {
        @POST("v1/models/gemini-1.5-flash:generateContent")
        fun generateContent(
            @Body request: GeminiRequest,
            @Query("key") apiKey: String
        ): Call<GeminiResponse>
    }

    data class GeminiRequest(val contents: List<Content>)
    data class Content(val parts: List<Part>) {
        constructor(text: String) : this(listOf(Part(text)))
    }
    data class Part(val text: String)
    data class GeminiResponse(val candidates: List<Candidate>)
    data class Candidate(val content: Content)
}