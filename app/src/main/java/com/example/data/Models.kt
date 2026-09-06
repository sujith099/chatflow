package com.example.data

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val photoURL: String = "",
    val bio: String = "",
    val status: String = "Hey there! I am using ChatFlow.",
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val conversationId: String = "",
    val type: String = "private", // "private" or "group"
    val name: String = "",
    val photoURL: String = "",
    val createdBy: String = "",
    val participants: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val lastMessageAt: Long = System.currentTimeMillis(),
    val unreadCount: Map<String, Int> = emptyMap(),
    val isPinned: Map<String, Boolean> = emptyMap(),
    val isMuted: Map<String, Boolean> = emptyMap(),
    val isArchived: Map<String, Boolean> = emptyMap()
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val type: String = "text", // "text", "image", "file", "audio"
    val fileURL: String = "",
    val fileName: String = "",
    val replyTo: String? = null,
    val replyText: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val edited: Boolean = false,
    val deleted: Boolean = false,
    val deliveredTo: List<String> = emptyList(),
    val readBy: List<String> = emptyList(),
    val isPinned: Boolean = false
)

data class AppNotification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
