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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAM by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = time.ifEmpty { "Select Time" },
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Hour picker
                            NumberPicker(
                                value = selectedHour,
                                onValueChange = { selectedHour = it },
                                range = 1..12
                            )
                            
                            Text(":", style = MaterialTheme.typography.headlineLarge)
                            
                            // Minute picker
                            NumberPicker(
                                value = selectedMinute,
                                onValueChange = { selectedMinute = it },
                                range = 0..59,
                                format = { "%02d".format(it) }
                            )
                            
                            // AM/PM toggle
                            Switch(
                                checked = isAM,
                                onCheckedChange = { isAM = it },
                                thumbContent = {
                                    Text(if (isAM) "AM" else "PM")
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val hour = if (!isAM && selectedHour != 12) selectedHour + 12 
                                       else if (isAM && selectedHour == 12) 0 
                                       else selectedHour
                                val timeString = "%02d:%02d".format(hour, selectedMinute)
                                onTimeSelected(timeString)
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set Time")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() }
) {
    Column {
        IconButton(onClick = { 
            if (value < range.last) onValueChange(value + 1) 
        }) {
            Icon(Icons.Default.KeyboardArrowUp, null)
        }
        
        Text(
            text = format(value),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        IconButton(onClick = { 
            if (value > range.first) onValueChange(value - 1) 
        }) {
            Icon(Icons.Default.KeyboardArrowDown, null)
        }
    }
} 