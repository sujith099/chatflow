package com.example.service

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.presenceDataStore by preferencesDataStore(name = "presence_prefs")

class PresenceManager(private val context: Context) : DefaultLifecycleObserver {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val IS_ONLINE_KEY = booleanPreferencesKey("is_online")
        private val LAST_SEEN_KEY = longPreferencesKey("last_seen")
    }

    val isOnlineFlow: Flow<Boolean> = context.presenceDataStore.data
        .map { preferences -> preferences[IS_ONLINE_KEY] ?: true }

    val lastSeenFlow: Flow<Long> = context.presenceDataStore.data
        .map { preferences -> preferences[LAST_SEEN_KEY] ?: System.currentTimeMillis() }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updatePresence(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        updatePresence(false)
    }

    private fun updatePresence(online: Boolean) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            context.presenceDataStore.edit { preferences ->
                preferences[IS_ONLINE_KEY] = online
                preferences[LAST_SEEN_KEY] = timestamp
            }
            // Update repository / Firestore status
            ChatRepository.updateOnlineStatus(online, timestamp)
        }
    }
}
