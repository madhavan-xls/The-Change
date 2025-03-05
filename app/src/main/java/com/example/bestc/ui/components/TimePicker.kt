package com.example.bestc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.Dialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTimePicker(
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(6) }
    var selectedMinute by remember { mutableStateOf(0) }

    LaunchedEffect(time) {
        try {
            val parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            selectedHour = parsedTime.hour
            selectedMinute = parsedTime.minute
        } catch (e: Exception) {
            selectedHour = 6
            selectedMinute = 0
        }
    }

    val displayTime = remember(selectedHour, selectedMinute) {
        String.format("%02d:%02d", selectedHour, selectedMinute)
    }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(displayTime)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    onTimeSelected(displayTime)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            title = { Text("Select Time") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hour", style = MaterialTheme.typography.labelMedium)
                        NumberPicker(
                            value = selectedHour,
                            onValueChange = { selectedHour = it },
                            range = 0..23,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                    
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minute", style = MaterialTheme.typography.labelMedium)
                        NumberPicker(
                            value = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            range = 0..55,
                            step = 5,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    step: Int = 1
) {
    // Convert IntRange to progression with step
    val progression = remember(range, step) {
        range step step
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { 
                if (value < progression.last) onValueChange((value + step).coerceAtMost(progression.last))
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }
        
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.headlineMedium
        )
        
        IconButton(
            onClick = { 
                if (value > progression.first) onValueChange((value - step).coerceAtLeast(progression.first))
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
} 