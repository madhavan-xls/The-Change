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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessScreen(userData: UserData?, startDate: LocalDateTime?) {
    // Add null checks
    if (userData == null || startDate == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No schedule data available")
        }
        return
    }

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var hasScheduledAlarms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAllAlarms by remember { mutableStateOf(false) }

    // Calculate current week here
    val currentWeek = remember(startDate) {
        val currentDate = LocalDateTime.now()
        ChronoUnit.WEEKS.between(startDate, currentDate) + 1
    }

    val (timeGapMinutes, alarmTimes) = remember(userData.wakeUpTime, userData.sleepTime, startDate) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val wakeUpTime = try {
            LocalTime.parse(userData.wakeUpTime, formatter)
        } catch (e: Exception) {
            LocalTime.of(6, 0).also {
                errorMessage = "Invalid wake-up time format"
            }
        }
        
        val sleepTime = try {
            LocalTime.parse(userData.sleepTime, formatter)
        } catch (e: Exception) {
            LocalTime.of(22, 0).also {
                errorMessage = "Invalid sleep time format"
            }
        }
        
        if (wakeUpTime.isAfter(sleepTime)) {
            // Check if it's an overnight schedule
            val wakeUpDateTime = LocalDateTime.now().with(wakeUpTime)
            val sleepDateTime = LocalDateTime.now().with(sleepTime).plusDays(1)
            
            if (wakeUpDateTime.until(sleepDateTime, ChronoUnit.HOURS) < 6) {
                errorMessage = "Minimum 6 hours between wake and sleep times"
                return@remember 0L to emptyList()
            }
        }
        
        val gap = when {
            currentWeek <= 1 -> 90L
            currentWeek == 2L -> 120L
            currentWeek == 3L -> 150L
            currentWeek == 4L -> 180L
            currentWeek == 5L -> 210L
            else -> 240L
        }.coerceAtLeast(15L)

        val times = try {
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
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    ProcessScreenHelper.scheduleAlarms(
                        context = context,
                        userData = userData,
                        startDate = startDate
                    )
                }
                hasScheduledAlarms = true
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is IllegalArgumentException -> "Invalid schedule: ${e.message}"
                    is IllegalStateException -> "Too many alarms: ${e.message}"
                    else -> "Failed to schedule alarms"
                }
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular progress indicator for week
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = (currentWeek % 6).toFloat() / 6f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "Week\n$currentWeek",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Schedule summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        ScheduleItem(
                            icon = Icons.Default.WbSunny,
                            title = "Wake Up",
                            time = userData.wakeUpTime
                        )
                        
                        ScheduleItem(
                            icon = Icons.Default.Nightlight,
                            title = "Sleep Time",
                            time = userData.sleepTime
                        )
                        
                        ScheduleItem(
                            icon = Icons.Default.Schedule,
                            title = "Interval",
                            time = "${timeGapMinutes / 60}h ${timeGapMinutes % 60}m"
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Today's Reminders (${alarmTimes.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Alarm times list with show more/less
        item {
            Text(
                text = "Upcoming Alarms",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(alarmTimes.filter { it.isAfter(LocalTime.now()) }) { time ->
            AlarmTimeItem(time = time, isCompleted = false)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "Completed Alarms",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        items(alarmTimes.filter { it.isBefore(LocalTime.now()) }.reversed()) { time ->
            AlarmTimeItem(time = time, isCompleted = true)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (alarmTimes.size > 10) {
            item {
                Button(
                    onClick = { showAllAlarms = !showAllAlarms },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(if (showAllAlarms) "Show Less" else "Show More")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ScheduleItem(icon: ImageVector, title: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlarmTimeItem(time: LocalTime, isCompleted: Boolean) {
    val currentTime = LocalTime.now()
    val actuallyCompleted = remember(time) { time.isBefore(currentTime) && isCompleted }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (actuallyCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (actuallyCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Schedule
                },
                contentDescription = null,
                tint = if (actuallyCompleted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.titleMedium,
                color = if (actuallyCompleted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
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
    val maxCalculations = 1000
    var count = 0

    val sleepTimeAdjusted = if (sleepTime.isBefore(wakeUpTime)) {
        sleepTime.plusHours(24)
    } else {
        sleepTime
    }

    while (count < maxCalculations) {
        times.add(currentTime)
        
        // Check if next interval would exceed adjusted sleep time
        val nextTime = currentTime.plusMinutes(intervalMinutes)
        if (nextTime.isAfter(sleepTimeAdjusted)) {
            break
        }
        
        currentTime = nextTime
        count++
        
        if (count >= maxCalculations) {
            throw IllegalStateException("Excessive alarm time calculations")
        }
    }
    return times
} 