package com.example.bestc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessScreen(
    userData: UserData,
    startDate: LocalDateTime
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var hasScheduledAlarms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDate = LocalDateTime.now()
    val weeksSinceStart = ChronoUnit.WEEKS.between(startDate, currentDate)
    val currentWeek = weeksSinceStart + 1

    // Calculate times in a remember block to prevent recalculation
    val (timeGapMinutes, alarmTimes) = remember(userData.wakeUpTime, userData.sleepTime, startDate) {
        val gap = when {
            currentWeek <= 1 -> 90L
            currentWeek == 2L -> 120L
            currentWeek == 3L -> 150L
            currentWeek == 4L -> 180L
            currentWeek == 5L -> 210L
            else -> 240L
        }

        val times = try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val wakeUpTime = try {
                LocalTime.parse(userData.wakeUpTime, formatter)
            } catch (e: Exception) {
                LocalTime.of(6, 0) // Default fallback
            }
            
            val sleepTime = try {
                LocalTime.parse(userData.sleepTime, formatter)
            } catch (e: Exception) {
                LocalTime.of(22, 0) // Default fallback
            }
            
            calculateAlarmTimes(wakeUpTime, sleepTime, gap)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        gap to times
    }

    // Schedule alarms in background only once with a delay
    LaunchedEffect(Unit) {
        if (!hasScheduledAlarms && !isLoading) {
            // Add a small delay to prevent immediate scheduling
            delay(500)
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    ProcessScreenHelper.scheduleAlarms(context, userData, startDate)
                }
                hasScheduledAlarms = true
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to schedule alarms: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Show loading indicator if needed
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Setting up your schedule...")
            }
        }
        return
    }

    // Show error if any
    errorMessage?.let {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(it)
                Button(onClick = { errorMessage = null }) {
                    Text("Dismiss")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Week $currentWeek: ${timeGapMinutes / 60}h ${timeGapMinutes % 60}m intervals",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Today's Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    alarmTimes.forEach { time ->
                        Text(
                            text = "• ${time.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun calculateAlarmTimes(
    wakeUpTime: LocalTime,
    sleepTime: LocalTime,
    intervalMinutes: Long
): List<LocalTime> {
    val times = mutableListOf<LocalTime>()
    var currentTime = wakeUpTime

    while (currentTime.isBefore(sleepTime)) {
        times.add(currentTime)
        currentTime = currentTime.plusMinutes(intervalMinutes)
        
        if (currentTime.isAfter(sleepTime)) {
            break
        }
    }

    return times
} 