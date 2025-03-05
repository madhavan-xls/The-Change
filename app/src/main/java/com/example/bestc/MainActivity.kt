package com.example.bestc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bestc.data.UserData
import com.example.bestc.navigation.Destinations
import com.example.bestc.services.AlarmService
import com.example.bestc.services.ProcessScreenHelper
import com.example.bestc.ui.screens.*
import com.example.bestc.ui.theme.BestcTheme
import java.time.LocalDateTime
import com.example.bestc.data.SupabaseClient

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startAlarmService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()

        setContent {
            BestcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isEmpty()) {
            startAlarmService()
        } else {
            requestPermissionLauncher.launch(notGrantedPermissions.toTypedArray())
        }
    }

    private fun startAlarmService() {
        try {
            val serviceIntent = Intent(this, AlarmService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<String?>(null) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var startDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var selectedTab by remember { mutableStateOf(Destinations.HOME) }

    val context = LocalContext.current

    // Check if user is already authenticated
    LaunchedEffect(Unit) {
        try {
            SupabaseClient.client.gotrue.currentSessionOrNull()?.let {
                isAuthenticated = true
                // Load user data from SharedPreferences
                val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                userData = ProcessScreenHelper.getUserDataFromPrefs(prefs)
                startDate = ProcessScreenHelper.getStartDateFromPrefs(prefs)
                currentScreen = if (userData == null) "onboarding" else "main"
            }
        } catch (e: Exception) {
            isAuthenticated = false
        }
    }

    when {
        !isAuthenticated -> {
            AuthScreen(
                onAuthSuccess = {
                    isAuthenticated = true
                    currentScreen = "onboarding"
                }
            )
        }
        else -> {
            when (currentScreen) {
                null -> {
                    // Loading state
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                "onboarding" -> {
                    OnboardingScreen(
                        onComplete = { 
                            userData = it
                            currentScreen = "confirmation"
                        }
                    )
                }
                "confirmation" -> {
                    userData?.let { data ->
                        ConfirmationScreen(
                            userData = data,
                            onConfirm = { 
                                startDate = LocalDateTime.now()
                                startDate?.let { date ->
                                    ProcessScreenHelper.saveUserDataToPrefs(context, data, date)
                                }
                                currentScreen = "main"
                            },
                            onBack = {
                                currentScreen = "onboarding"
                            }
                        )
                    }
                }
                "main" -> {
                    userData?.let { data ->
                        startDate?.let { date ->
                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text("Home") },
                                            selected = selectedTab == Destinations.HOME,
                                            onClick = { selectedTab = Destinations.HOME }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Info, contentDescription = "Learn") },
                                            label = { Text("Learn") },
                                            selected = selectedTab == Destinations.LEARN,
                                            onClick = { selectedTab = Destinations.LEARN }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.List, contentDescription = "Process") },
                                            label = { Text("Process") },
                                            selected = selectedTab == Destinations.PROCESS,
                                            onClick = { selectedTab = Destinations.PROCESS }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                            label = { Text("Settings") },
                                            selected = selectedTab == Destinations.SETTINGS,
                                            onClick = { selectedTab = Destinations.SETTINGS }
                                        )
                                    }
                                }
                            ) { padding ->
                                Box(modifier = Modifier.padding(padding)) {
                                    when (selectedTab) {
                                        Destinations.HOME -> HomeScreen(userData = data, startDate = date)
                                        Destinations.LEARN -> LearnScreen(userData = data)
                                        Destinations.PROCESS -> ProcessScreen(userData = data, startDate = date)
                                        Destinations.SETTINGS -> SettingsScreen(
                                            userData = data,
                                            startDate = date,
                                            onUpdateSettings = { newUserData ->
                                                userData = newUserData
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}