package com.example.service

import com.example.data.Message

object GeminiService {
    suspend fun summarizeConversation(messages: List<Message>): String {
        return "AI Assistant Summary: The conversation focused on project updates, design reviews, and scheduling the next sync."
    }

    suspend fun generateSmartReplies(lastMessage: String): List<String> {
        return listOf("Sounds good!", "I'll check and let you know.", "Thanks for the update!")
    }

    suspend fun rewriteMessage(text: String, tone: String): String {
        return when (tone) {
            "Professional" -> "Regarding your note: $text, please let me know how we should proceed."
            "Friendly" -> "Hey! $text 😊 Let me know what you think!"
            "Casual" -> "Yeah, $text ✌️"
            else -> text
        }
    }
}
