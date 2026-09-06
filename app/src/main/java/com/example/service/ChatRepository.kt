package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.*

object ChatRepository {
    // In-memory mock/fallback state for immediate robust full-stack experience
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "user_current",
            name = "Alex Morgan",
            username = "alexmorgan",
            email = "alex@chatflow.app",
            photoURL = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            bio = "Product Designer & traveler 🚀",
            status = "Available",
            isOnline = true
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(
        listOf(
            UserProfile("user_1", "Sarah Jenkins", "sarahs", "sarah@chatflow.app", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", "Coffee addict & coder ☕", "Online", true),
            UserProfile("user_2", "David Kim", "dkim", "david@chatflow.app", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", "Building the future of AI 🤖", "In a meeting", true),
            UserProfile("user_3", "Elena Rostova", "elena_r", "elena@chatflow.app", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150", "Traveling the world ✈️", "Offline", false),
            UserProfile("user_4", "Marcus Vance", "marcusv", "marcus@chatflow.app", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", "Music producer & dj 🎧", "Online", true)
        )
    )
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(
        listOf(
            Conversation(
                conversationId = "conv_1",
                type = "private",
                name = "Sarah Jenkins",
                photoURL = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                participants = listOf("user_current", "user_1"),
                lastMessage = "Let's review the designs tomorrow!",
                lastMessageAt = System.currentTimeMillis() - 120000,
                unreadCount = mapOf("user_current" to 2)
            ),
            Conversation(
                conversationId = "conv_2",
                type = "group",
                name = "Design Team Sync",
                photoURL = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                createdBy = "user_current",
                participants = listOf("user_current", "user_1", "user_2"),
                lastMessage = "David uploaded new wireframes.",
                lastMessageAt = System.currentTimeMillis() - 3600000,
                unreadCount = mapOf("user_current" to 0)
            ),
            Conversation(
                conversationId = "conv_3",
                type = "private",
                name = "David Kim",
                photoURL = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                participants = listOf("user_current", "user_2"),
                lastMessage = "Did you check the Gemini API docs?",
                lastMessageAt = System.currentTimeMillis() - 86400000,
                unreadCount = mapOf("user_current" to 0)
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(
        mapOf(
            "conv_1" to listOf(
                Message("m1", "user_1", "Sarah Jenkins", "Hey Alex! How is the new release coming along?", createdAt = System.currentTimeMillis() - 300000, deliveredTo = listOf("user_current"), readBy = listOf("user_current")),
                Message("m2", "user_current", "Alex Morgan", "Going great! Just polishing the animations now.", createdAt = System.currentTimeMillis() - 240000, deliveredTo = listOf("user_1"), readBy = listOf("user_1")),
                Message("m3", "user_1", "Sarah Jenkins", "Let's review the designs tomorrow!", createdAt = System.currentTimeMillis() - 120000, deliveredTo = listOf("user_current"), readBy = emptyList(), reactions = mapOf("user_current" to "❤️"))
            ),
            "conv_2" to listOf(
                Message("m4", "user_2", "David Kim", "Welcome everyone to the Design Team Sync!", createdAt = System.currentTimeMillis() - 7200000),
                Message("m5", "user_2", "David Kim", "David uploaded new wireframes.", createdAt = System.currentTimeMillis() - 3600000, type = "file", fileName = "wireframes_v2.pdf", fileURL = "https://example.com/file.pdf")
            )
        )
    )
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification("n1", "user_current", "Sarah Jenkins sent you a message", "Let's review the designs tomorrow!", System.currentTimeMillis() - 120000, false),
            AppNotification("n2", "user_current", "Added to group", "You were added to Design Team Sync", System.currentTimeMillis() - 7200000, true)
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val blockedUsers: StateFlow<List<String>> = _blockedUsers.asStateFlow()

    // Actions
    fun sendMessage(conversationId: String, text: String, type: String = "text", fileURL: String = "", fileName: String = "", replyTo: String? = null, replyText: String? = null) {
        val currentUsr = _currentUser.value ?: return
        val newMessage = Message(
            messageId = "msg_${System.currentTimeMillis()}",
            senderId = currentUsr.uid,
            senderName = currentUsr.name,
            text = text,
            type = type,
            fileURL = fileURL,
            fileName = fileName,
            replyTo = replyTo,
            replyText = replyText,
            createdAt = System.currentTimeMillis(),
            deliveredTo = listOf(currentUsr.uid)
        )

        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId].orEmpty().toMutableList()
        list.add(newMessage)
        currentMap[conversationId] = list
        _messages.value = currentMap

        // Update conversation lastMessage
        val convList = _conversations.value.map { conv ->
            if (conv.conversationId == conversationId) {
                conv.copy(lastMessage = if (type == "image") "📷 Photo" else if (type == "file") "📎 $fileName" else text, lastMessageAt = System.currentTimeMillis())
            } else conv
        }
        _conversations.value = convList
    }

    fun addReaction(conversationId: String, messageId: String, emoji: String) {
        val currentUsr = _currentUser.value ?: return
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId]?.map { msg ->
            if (msg.messageId == messageId) {
                val newReactions = msg.reactions.toMutableMap()
                if (newReactions[currentUsr.uid] == emoji) {
                    newReactions.remove(currentUsr.uid)
                } else {
                    newReactions[currentUsr.uid] = emoji
                }
                msg.copy(reactions = newReactions)
            } else msg
        }.orEmpty()
        currentMap[conversationId] = list
        _messages.value = currentMap
    }

    fun editMessage(conversationId: String, messageId: String, newText: String) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId]?.map { msg ->
            if (msg.messageId == messageId) {
                msg.copy(text = newText, edited = true, updatedAt = System.currentTimeMillis())
            } else msg
        }.orEmpty()
        currentMap[conversationId] = list
        _messages.value = currentMap
    }

    fun deleteMessage(conversationId: String, messageId: String, deleteForEveryone: Boolean) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId]?.map { msg ->
            if (msg.messageId == messageId) {
                if (deleteForEveryone) {
                    msg.copy(deleted = true, text = "This message was deleted")
                } else {
                    // delete for me
                    msg.copy(text = "You deleted this message")
                }
            } else msg
        }.orEmpty()
        currentMap[conversationId] = list
        _messages.value = currentMap
    }

    fun pinMessage(conversationId: String, messageId: String) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId]?.map { msg ->
            if (msg.messageId == messageId) {
                msg.copy(isPinned = !msg.isPinned)
            } else msg
        }.orEmpty()
        currentMap[conversationId] = list
        _messages.value = currentMap
    }

    fun createGroup(name: String, participants: List<String>, photoURL: String, description: String) {
        val currentUsr = _currentUser.value ?: return
        val newConv = Conversation(
            conversationId = "conv_${System.currentTimeMillis()}",
            type = "group",
            name = name,
            photoURL = photoURL.ifEmpty { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150" },
            createdBy = currentUsr.uid,
            participants = participants + currentUsr.uid,
            lastMessage = "Group created",
            lastMessageAt = System.currentTimeMillis()
        )
        _conversations.value = listOf(newConv) + _conversations.value
    }

    fun startPrivateChat(otherUser: UserProfile): String {
        val currentUsr = _currentUser.value ?: return ""
        val existing = _conversations.value.find { it.type == "private" && it.participants.contains(otherUser.uid) }
        if (existing != null) return existing.conversationId

        val newConv = Conversation(
            conversationId = "conv_${System.currentTimeMillis()}",
            type = "private",
            name = otherUser.name,
            photoURL = otherUser.photoURL,
            participants = listOf(currentUsr.uid, otherUser.uid),
            lastMessage = "Started conversation with ${otherUser.name}",
            lastMessageAt = System.currentTimeMillis()
        )
        _conversations.value = listOf(newConv) + _conversations.value
        return newConv.conversationId
    }

    fun toggleBlockUser(userId: String) {
        val blocked = _blockedUsers.value.toMutableList()
        if (blocked.contains(userId)) blocked.remove(userId) else blocked.add(userId)
        _blockedUsers.value = blocked
    }

    fun updateProfile(name: String, username: String, bio: String, status: String, photoURL: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            name = name,
            username = username,
            bio = bio,
            status = status,
            photoURL = photoURL,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateOnlineStatus(online: Boolean, timestamp: Long) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(isOnline = online)
        // Also update in users list if present
        _users.value = _users.value.map {
            if (it.uid == current.uid) it.copy(isOnline = online) else it
        }
    }

    fun markNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
