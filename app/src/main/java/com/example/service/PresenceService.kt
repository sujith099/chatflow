package com.example.service

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PresenceService : DefaultLifecycleObserver {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        updatePresence(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        updatePresence(false)
    }

    private fun updatePresence(isOnline: Boolean) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            ChatRepository.updateOnlineStatus(isOnline, timestamp)
        }
    }
}
