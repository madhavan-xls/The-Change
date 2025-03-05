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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Logout
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
import android.content.Context
import androidx.compose.foundation.layout.padding
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import android.app.Activity

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
                AppContent()
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

@Composable
private fun AppContent() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    
    val isLoggedIn by remember {
        derivedStateOf {
            prefs.contains("wakeUpTime") && 
            prefs.contains("sleepTime") &&
            prefs.contains("cigarettesPerDay") &&
            prefs.contains("startDate") &&
            prefs.contains("cigarettePrice")
        }
    }

    if (isLoggedIn) {
        MainAppContent(
            onLogout = { 
                prefs.edit().apply {
                    clear()
                    commit()
                }
            },
            prefs = prefs
        )
    } else {
        OnboardingScreen(
            onComplete = { userData ->
                ProcessScreenHelper.saveUserData(
                    context = context,
                    userData = userData,
                    startDate = LocalDateTime.now()
                )
                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    onLogout: () -> Unit,
    prefs: SharedPreferences
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(Destinations.HOME) }
    val userData = remember { ProcessScreenHelper.getUserDataFromPrefs(prefs) }
    val startDate = remember { ProcessScreenHelper.getStartDateFromPrefs(prefs) }

    // Add null safety checks
    if (userData == null || startDate == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading user data...")
        }
        return
    }

    // Handle logout with LaunchedEffect
    var triggerLogout by remember { mutableStateOf(false) }
    if (triggerLogout) {
        LaunchedEffect(Unit) {
            context.startActivity(
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Change App") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
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
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                Destinations.HOME -> HomeScreen(
                    userData = userData, 
                    startDate = startDate
                )
                Destinations.LEARN -> LearnScreen(userData = userData)
                Destinations.PROCESS -> ProcessScreen(
                    userData = userData,
                    startDate = startDate
                )
                Destinations.SETTINGS -> SettingsScreen(
                    userData = userData,
                    startDate = startDate,
                    onUpdateSettings = { updatedData ->
                        ProcessScreenHelper.saveUserData(
                            context = context,
                            userData = updatedData,
                            startDate = startDate
                        )
                    },
                    onLogout = { 
                        prefs.edit().apply {
                            clear()
                            commit()
                        }
                        triggerLogout = true
                    }
                )
            }
        }
    }
}