package com.example.bestc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun LearnScreen(userData: UserData) {
    val recommendedDosage = when {
        userData.cigarettesPerDay < 7 -> "1mg"
        userData.cigarettesPerDay <= 20 -> "2mg"
        else -> "4mg"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dosage Recommendation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your Recommended Dosage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Based on your smoking habits (${userData.cigarettesPerDay} cigarettes/day):",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$recommendedDosage Nicotine Gum",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Weekly Changes Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Your Body's Recovery Journey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Week 1
                WeekCard(
                    weekNumber = 1,
                    title = "Initial Recovery Phase",
                    changes = listOf(
                        "20 minutes: Blood pressure and heart rate drop",
                        "8 hours: Carbon monoxide levels in blood drop by half",
                        "24 hours: Risk of heart attack begins to decrease",
                        "48 hours: Nerve endings start to regrow, sense of taste and smell improve",
                        "72 hours: Bronchial tubes begin to relax, breathing becomes easier",
                        "Possible withdrawal symptoms: headaches, anxiety, irritability"
                    )
                )

                // Week 2
                WeekCard(
                    weekNumber = 2,
                    title = "Circulation Improvement",
                    changes = listOf(
                        "Blood circulation improves significantly",
                        "Walking becomes easier as oxygen levels normalize",
                        "Lung function increases up to 30%",
                        "Withdrawal symptoms start to decrease",
                        "Energy levels begin to increase",
                        "Coughing might increase as lungs clear themselves"
                    )
                )

                // Week 3
                WeekCard(
                    weekNumber = 3,
                    title = "Physical Enhancement",
                    changes = listOf(
                        "Exercise becomes noticeably easier",
                        "Lung capacity continues to improve",
                        "Inflammation in blood vessels reduces",
                        "Risk of heart attack continues to drop",
                        "Skin complexion begins to improve",
                        "Immune system strengthens"
                    )
                )

                // Week 4
                WeekCard(
                    weekNumber = 4,
                    title = "Stability Phase",
                    changes = listOf(
                        "Nicotine withdrawal symptoms largely subside",
                        "Lung function improves up to 40%",
                        "Blood circulation reaches near-normal levels",
                        "Exercise capacity significantly improves",
                        "Risk of coronary heart disease begins to drop",
                        "Mental clarity and focus enhance"
                    )
                )

                // Week 5
                WeekCard(
                    weekNumber = 5,
                    title = "Long-term Benefits Begin",
                    changes = listOf(
                        "Risk of stroke starts to reduce",
                        "Lung function continues to improve",
                        "Blood pressure stabilizes",
                        "Circulation continues to improve",
                        "Energy levels reach new highs",
                        "Stress levels typically decrease"
                    )
                )

                // Week 6+
                WeekCard(
                    weekNumber = 6,
                    title = "Sustained Recovery",
                    changes = listOf(
                        "Risk of heart disease drops by 50%",
                        "Lung cancer risk begins to decrease",
                        "Breathing becomes significantly easier",
                        "Exercise capacity reaches near-normal",
                        "Overall health continues to improve",
                        "Risk of smoking-related diseases decreases substantially"
                    )
                )
            }
        }
    }
}

@Composable
private fun WeekCard(
    weekNumber: Int,
    title: String,
    changes: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Week $weekNumber: $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            changes.forEach { change ->
                Text(
                    text = "• $change",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 