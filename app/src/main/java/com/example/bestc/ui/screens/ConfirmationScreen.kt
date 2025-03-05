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
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    userData: UserData,
    onConfirm: (UserData) -> Unit,
    onBack: () -> Unit
) {
    // Format currency in Rupees
    val rupeesFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dailyCost = userData.cigarettesPerDay * userData.cigarettePrice
    val yearlyCost = dailyCost * 365

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Confirm Your Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Gender", userData.gender)
                DetailRow("Age", "${userData.age} years")
                DetailRow("Daily cigarettes", "${userData.cigarettesPerDay}")
                DetailRow("Price per cigarette", rupeesFormat.format(userData.cigarettePrice))
                DetailRow("Years of smoking", "${userData.yearsOfSmoking}")
                DetailRow("Wake up time", userData.wakeUpTime)
                DetailRow("Sleep time", userData.sleepTime)
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                DetailRow(
                    "Daily spending",
                    rupeesFormat.format(dailyCost),
                    MaterialTheme.colorScheme.error
                )
                DetailRow(
                    "Yearly spending",
                    rupeesFormat.format(yearlyCost),
                    MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = { onConfirm(userData) },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Confirm")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
} 