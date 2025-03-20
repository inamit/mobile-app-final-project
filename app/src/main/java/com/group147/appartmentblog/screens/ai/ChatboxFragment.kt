package com.group147.appartmentblog.screens.chat

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group147.appartmentblog.databinding.FragmentChatboxBinding
import com.group147.appartmentblog.model.service.GeminiService
import com.group147.appartmentblog.screens.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatboxFragment : Fragment() {

    private lateinit var binding: FragmentChatboxBinding
    private lateinit var geminiService: GeminiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatboxBinding.inflate(inflater, container, false)
        geminiService = GeminiService(requireContext())

        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentInfo = arguments?.getString("apartmentInfo") ?: ""
        if (apartmentInfo.isNotEmpty()) {
            getGeminiResponse(apartmentInfo)
        }

        binding.sendButton.setOnClickListener {
            val userInput = binding.messageEditText.text.toString().trim()
            binding.messageEditText.text.clear()
            if (userInput.isNotEmpty()) {
                binding.chatTextView.append("You: $userInput\n")
                getGeminiResponse(userInput)
            } else {
                Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getGeminiResponse(input: String) {
        geminiService.generateContent(input, object : Callback<GeminiService.GeminiResponse> {
            override fun onResponse(call: Call<GeminiService.GeminiResponse>, response: Response<GeminiService.GeminiResponse>) {
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

            override fun onFailure(call: Call<GeminiService.GeminiResponse>, t: Throwable) {
                binding.chatTextView.append("Error: ${t.message}\n")
            }
        })
    }
}