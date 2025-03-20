package com.group147.appartmentblog.screens.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group147.appartmentblog.databinding.FragmentChatboxBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class ChatboxFragment : Fragment() {

    private lateinit var binding: FragmentChatboxBinding
    private val apiKey = "AIzaSyBCEHp-I_N_BcXVxenFA8rLV-UC2Pmd22A"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.sendButton.setOnClickListener {
            val userInput = binding.messageEditText.text.toString().trim()
            if (userInput.isNotEmpty()) {
                binding.chatTextView.append("You: $userInput\n")
                getGeminiResponse(userInput)
            } else {
                Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getGeminiResponse(input: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GeminiAIService::class.java)
        val request = GeminiRequest(listOf(Content(input)))  // Ensure proper request format

        service.generateContent(request, apiKey).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (reply != null) {
                        binding.chatTextView.append("AI: $reply\n")
                    } else {
                        binding.chatTextView.append("AI: (No response)\n")
                    }
                } else {
                    binding.chatTextView.append("Error: ${response.errorBody()?.string()}\n")
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                binding.chatTextView.append("Error: ${t.message}\n")
            }
        })
    }
}

interface GeminiAIService {
    @POST("v1/models/gemini-1.5-flash:generateContent")  // Updated model
    fun generateContent(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String
    ): Call<GeminiResponse>
}

// Ensure correct request format
data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>) {
    constructor(text: String) : this(listOf(Part(text)))
}
data class Part(val text: String)

// Ensure correct response format
data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)
