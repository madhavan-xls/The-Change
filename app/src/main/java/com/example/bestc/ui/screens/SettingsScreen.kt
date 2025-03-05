package com.example.bestc.ui.screens

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bestc.R
import com.example.bestc.data.UserData
import com.example.bestc.services.ProcessScreenHelper
import java.time.LocalDateTime
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.Intent
import com.example.bestc.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userData: UserData,
    startDate: LocalDateTime,
    onUpdateSettings: (UserData) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var wakeUpTime by remember { mutableStateOf<String>(userData.wakeUpTime) }
    var sleepTime by remember { mutableStateOf<String>(userData.sleepTime) }
    var selectedAlarmSound by remember { mutableStateOf<Int>(0) }
    var vibrationEnabled by remember { mutableStateOf<Boolean>(true) }
    var alarmLabel by remember { mutableStateOf<String>("Time for Nicotine Gum") }
    
    val mediaPlayer = remember { MediaPlayer() }
    
    val alarmSounds = listOf(
        "Default" to RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        "Default 2" to RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
        "Default 3" to RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    )

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        // Time Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = wakeUpTime,
                    onValueChange = { 
                        wakeUpTime = it
                        onUpdateSettings(userData.copy(wakeUpTime = it))
                    },
                    label = { Text("Wake up time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sleepTime,
                    onValueChange = { 
                        sleepTime = it
                        onUpdateSettings(userData.copy(sleepTime = it))
                    },
                    label = { Text("Sleep time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Alarm Sound Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.titleMedium
                )

                alarmSounds.forEachIndexed { index, (name, uri) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAlarmSound == index,
                            onClick = { selectedAlarmSound = index }
                        )
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                try {
                                    mediaPlayer.reset()
                                    mediaPlayer.setDataSource(context, uri)
                                    mediaPlayer.prepare()
                                    mediaPlayer.start()
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play sound"
                            )
                        }
                    }
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration")
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }

                // Alarm Label
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    label = { Text("Notification Message") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Button(
            onClick = {
                if (!isSaving) {
                    scope.launch {
                        try {
                            isSaving = true
                            val updatedUserData = userData.copy(
                                wakeUpTime = wakeUpTime,
                                sleepTime = sleepTime
                            )
                            
                            // Update UI first
                            onUpdateSettings(updatedUserData)

                            // Then update alarms in background
                            withContext(Dispatchers.IO) {
                                ProcessScreenHelper.updateUserDataAndAlarms(
                                    context = context,
                                    userData = updatedUserData,
                                    startDate = startDate
                                )
                            }
                        } catch (e: Exception) {
                            showError = true
                            // Log the error if needed
                        } finally {
                            isSaving = false
                        }
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Changes")
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Error") },
                text = { Text("Failed to save changes. Please try again.") },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
} 