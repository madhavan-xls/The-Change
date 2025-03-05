package com.example.bestc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min
import kotlinx.coroutines.delay

import com.example.bestc.ui.components.HealthInfoCard

@Composable
fun HomeScreen(
    userData: UserData,
    startDate: LocalDateTime
) {
    val currentDateState = rememberUpdatedTime()
    val currentDate = currentDateState.value

    val daysSinceStart = remember(currentDate) {
        ChronoUnit.DAYS.between(
            startDate.toLocalDate(),
            LocalDateTime.now().toLocalDate()
        ) + 1
    }

    val weeksSinceStart = daysSinceStart / 7
    val currentWeek = min(weeksSinceStart + 1, 6)
    
    val totalProgress = (currentWeek.toFloat() / 6) * 100
    val weekProgress = ((daysSinceStart % 7).toFloat() / 7) * 100

    // Savings calculation
    val dailySavings = userData.cigarettesPerDay * userData.cigarettePrice
    val totalSavings = remember(daysSinceStart) {
        dailySavings * daysSinceStart
    }

    // Auto-updating time effect
    LaunchedEffect(Unit) {
        while(true) {
            delay(60_000) // Update every minute
            currentDateState.value = LocalDateTime.now()
        }
    }

    // Add dosage calculation at the top
    val dosage = remember(userData.cigarettesPerDay) {
        when {
            userData.cigarettesPerDay < 7 -> "1mg"
            userData.cigarettesPerDay in 7..20 -> "2mg"
            else -> "4mg"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "6-Week Journey Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = totalProgress / 100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                )
                
                Text(
                    text = "Week $currentWeek of 6",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Current Week Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Week Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = weekProgress / 100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                
                Text(
                    text = "Day $daysSinceStart of Week $currentWeek",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Savings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Money Saved",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Daily Savings:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "₹${"%.2f".format(dailySavings)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Saved:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "₹${"%.2f".format(totalSavings)}", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Health Benefits Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Health Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val milestones = getHealthMilestones(daysSinceStart)
                milestones.forEach { milestone ->
                    Text(
                        text = "• $milestone",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        HealthInfoCard(
            title = "Recommended Nicotine Dosage",
            content = "Based on your ${userData.cigarettesPerDay} cigarettes/day:\n" +
                     "Start with $dosage gum every 1-2 hours\n" +
                     "Gradually reduce dosage over 6 weeks"
        )
    }
}

private fun getHealthMilestones(daysSinceStart: Long): List<String> {
    return when {
        daysSinceStart < 1 -> listOf("Your journey begins today!")
        daysSinceStart < 3 -> listOf(
            "Blood oxygen levels are returning to normal",
            "Carbon monoxide levels are dropping"
        )
        daysSinceStart < 7 -> listOf(
            "Sense of taste and smell improving",
            "Breathing is becoming easier"
        )
        daysSinceStart < 14 -> listOf(
            "Circulation is improving",
            "Lung function is increasing"
        )
        daysSinceStart < 30 -> listOf(
            "Heart attack risk has started to drop",
            "Energy levels are increasing"
        )
        else -> listOf(
            "Significant health improvements achieved",
            "Keep going strong!"
        )
    }
}

@Composable
private fun rememberUpdatedTime(): MutableState<LocalDateTime> {
    val timeState = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            timeState.value = LocalDateTime.now()
        }
    }
    return timeState
} 