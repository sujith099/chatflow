package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.service.PresenceManager
import com.example.service.PresenceService
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()
    private val presenceManager by lazy { PresenceManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(presenceManager)
        PresenceService.init()
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "landing" -> LandingScreen(viewModel)
                        "login" -> LoginScreen(viewModel)
                        "signup" -> SignupScreen(viewModel)
                        "forgot_password" -> ForgotPasswordScreen(viewModel)
                        "dashboard" -> DashboardScreen(viewModel)
                        "chat" -> ChatScreen(viewModel)
                        "contacts" -> ContactsScreen(viewModel)
                        "create_group" -> GroupCreateScreen(viewModel)
                        "profile" -> ProfileScreen(viewModel)
                        "settings" -> SettingsScreen(viewModel)
                        "notifications" -> NotificationsScreen(viewModel)
                        "search" -> SearchScreen(viewModel)
                        else -> LandingScreen(viewModel)
                    }
                }
            }
        }
    }
}
