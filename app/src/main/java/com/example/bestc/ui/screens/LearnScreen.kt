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
import androidx.compose.ui.graphics.Color
import com.example.bestc.ui.components.HealthInfoCard

@Composable
fun LearnScreen(userData: UserData) {
    val scrollState = rememberScrollState()
    
    // Enhanced dosage calculation with clear ranges
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
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HealthInfoCard(
            title = "Recommended Nicotine Dosage",
            content = "Based on your ${userData.cigarettesPerDay} cigarettes/day:\n" +
                     "Start with $dosage gum every 1-2 hours\n" +
                     "Gradually reduce dosage over 12 weeks"
        )

        HealthInfoCard(
            title = "Cigarette Consumption",
            content = "Daily: ${userData.cigarettesPerDay} cigarettes\n" +
                     "Annual: ${userData.cigarettesPerDay * 365} cigarettes\n" +
                     "Lifetime: ${userData.cigarettesPerDay * 365 * userData.yearsOfSmoking} cigarettes"
        )

        HealthInfoCard(
            title = "Financial Impact",
            content = "Per cigarette: ₹${"%.2f".format(userData.cigarettePrice)}\n" +
                     "Daily cost: ₹${"%.2f".format(userData.cigarettesPerDay * userData.cigarettePrice)}\n" +
                     "Monthly cost: ₹${"%.2f".format(userData.cigarettesPerDay * userData.cigarettePrice * 30)}"
        )

        HealthInfoCard(
            title = "Health Progress",
            content = "After ${userData.yearsOfSmoking} years of smoking:\n" +
                     "• ${userData.yearsOfSmoking * 10}% damage reversal potential\n" +
                     "• ${100 - (userData.yearsOfSmoking * 5)}% lung capacity recovery"
        )

        HealthInfoCard(
            title = "Nicotine Addiction Facts",
            content = "• Cigarettes are addictive primarily due to nicotine\n" +
                     "• Nicotine reaches brain in 10-20 seconds when smoked\n" +
                     "• Gum absorption takes 15-30 minutes for same effect\n" +
                     "• Proper timing prevents withdrawal symptoms"
        )

        HealthInfoCard(
            title = "6-Week Progress Benefits",
            content = "Week 1: Blood pressure & heart rate normalize\n" +
                     "Week 2: Lung function improves up to 30%\n" +
                     "Week 3: Circulation & energy levels boost\n" +
                     "Week 4: Coughing reduces significantly\n" +
                     "Week 5: Lung cancer risk starts decreasing\n" +
                     "Week 6: Nicotine receptors reduce by 50%"
        )

        HealthInfoCard(
            title = "Why Timing Matters",
            content = "Nicotine gum works differently than cigarettes:\n" +
                     "• Chew slowly for 20-30 minutes\n" +
                     "• Avoid eating/drinking while chewing\n" +
                     "• Space gums 1-2 hours apart\n" +
                     "• Never exceed 24 gums/day"
        )

        HealthInfoCard(
            title = "⚠️ Important Caution",
            content = "If you smoke during this program:\n" +
                     "• STOP using nicotine gum immediately\n" +
                     "• Wait 24 hours after last cigarette\n" +
                     "• Restart program with Week 1\n" +
                     "Combining sources can lead to overdose!",
            titleColor = MaterialTheme.colorScheme.error
        )
    }
} 