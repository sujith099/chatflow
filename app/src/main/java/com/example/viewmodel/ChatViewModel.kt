package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.service.ChatRepository
import com.example.service.GeminiService

class ChatViewModel : ViewModel() {
    val currentUser = ChatRepository.currentUser
    val users = ChatRepository.users
    val conversations = ChatRepository.conversations
    val messages = ChatRepository.messages
    val notifications = ChatRepository.notifications
    val blockedUsers = ChatRepository.blockedUsers

    // App state
    private val _currentScreen = MutableStateFlow("landing") // landing, login, signup, forgot_password, dashboard, chat, contacts, create_group, profile, settings, notifications, search, archived, blocked, ai_assistant
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _themeMode = MutableStateFlow("system") // light, dark, system
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // AI Assistant state
    private val _aiSummary = MutableStateFlow("")
    val aiSummary: StateFlow<String> = _aiSummary.asStateFlow()

    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun navigateTo(screen: String, conversationId: String? = null) {
        if (conversationId != null) {
            _activeConversationId.value = conversationId
        }
        _currentScreen.value = screen
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun sendMessage(text: String, type: String = "text", fileURL: String = "", fileName: String = "", replyTo: String? = null, replyText: String? = null) {
        val convId = _activeConversationId.value ?: return
        if (text.isBlank() && fileURL.isBlank()) return
        ChatRepository.sendMessage(convId, text, type, fileURL, fileName, replyTo, replyText)

        // Generate smart replies after receiving message
        viewModelScope.launch {
            val replies = GeminiService.generateSmartReplies(text)
            _smartReplies.value = replies
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        val convId = _activeConversationId.value ?: return
        ChatRepository.addReaction(convId, messageId, emoji)
    }

    fun editMessage(messageId: String, newText: String) {
        val convId = _activeConversationId.value ?: return
        ChatRepository.editMessage(convId, messageId, newText)
    }

    fun deleteMessage(messageId: String, deleteForEveryone: Boolean) {
        val convId = _activeConversationId.value ?: return
        ChatRepository.deleteMessage(convId, messageId, deleteForEveryone)
    }

    fun pinMessage(messageId: String) {
        val convId = _activeConversationId.value ?: return
        ChatRepository.pinMessage(convId, messageId)
    }

    fun createGroup(name: String, participants: List<String>, photoURL: String, description: String) {
        ChatRepository.createGroup(name, participants, photoURL, description)
        navigateTo("dashboard")
    }

    fun startPrivateChat(user: UserProfile) {
        val convId = ChatRepository.startPrivateChat(user)
        navigateTo("chat", convId)
    }

    fun toggleBlockUser(userId: String) {
        ChatRepository.toggleBlockUser(userId)
    }

    fun updateProfile(name: String, username: String, bio: String, status: String, photoURL: String) {
        ChatRepository.updateProfile(name, username, bio, status, photoURL)
    }

    fun markNotificationsRead() {
        ChatRepository.markNotificationsRead()
    }

    fun summarizeActiveChat() {
        val convId = _activeConversationId.value ?: return
        val msgs = messages.value[convId].orEmpty()
        viewModelScope.launch {
            _isAiLoading.value = true
            val summary = GeminiService.summarizeConversation(msgs)
            _aiSummary.value = summary
            _isAiLoading.value = false
        }
    }

    suspend fun rewriteText(text: String, tone: String): String {
        return GeminiService.rewriteMessage(text, tone)
    }
}
