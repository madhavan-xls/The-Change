package com.example.bestc.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bestc.data.UserData
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.bestc.ui.components.SimpleTimePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserData) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5
    
    // User data state
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var cigarettesPerDay by remember { mutableStateOf("") }
    var cigarettePrice by remember { mutableStateOf("") }
    var yearsOfSmoking by remember { mutableStateOf("") }
    var wakeUpTime by remember { mutableStateOf("06:00") }
    var sleepTime by remember { mutableStateOf("22:00") }
    
    // Validation state
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step $currentStep of $totalSteps") },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Gender and Age
            AnimatedVisibility(
                visible = currentStep == 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tell us about yourself",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    // Gender selection
                    Text(
                        text = "Select your gender",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { gender = "Male" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gender == "Male") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Male")
                        }
                        
                        Button(
                            onClick = { gender = "Female" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gender == "Female") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Female")
                        }
                    }
                    
                    // Age input
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError && age.isEmpty()
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (gender.isEmpty() || age.isEmpty()) {
                                showError = true
                                errorMessage = "Please fill all fields"
                            } else {
                                showError = false
                                currentStep++
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
            
            // Step 2: Smoking Habits
            AnimatedVisibility(
                visible = currentStep == 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Your Smoking Habits",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    OutlinedTextField(
                        value = cigarettesPerDay,
                        onValueChange = { cigarettesPerDay = it },
                        label = { Text("Cigarettes per day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError && cigarettesPerDay.isEmpty()
                    )
                    
                    OutlinedTextField(
                        value = yearsOfSmoking,
                        onValueChange = { yearsOfSmoking = it },
                        label = { Text("Years of smoking") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError && yearsOfSmoking.isEmpty()
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            val cigs = cigarettesPerDay.toIntOrNull() ?: 0
                            val years = yearsOfSmoking.toIntOrNull() ?: 0
                            
                            when {
                                cigs !in 1..100 -> {
                                    showError = true
                                    errorMessage = "Cigarettes per day must be between 1-100"
                                }
                                years !in 0..100 -> {
                                    showError = true
                                    errorMessage = "Years of smoking must be between 0-100"
                                }
                                else -> {
                                    showError = false
                                    currentStep++
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
            
            // Step 3: Cost
            AnimatedVisibility(
                visible = currentStep == 3,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Cost of Smoking",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    OutlinedTextField(
                        value = cigarettePrice,
                        onValueChange = { cigarettePrice = it },
                        label = { Text("Price per cigarette (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError && cigarettePrice.isEmpty()
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            val price = cigarettePrice.toDoubleOrNull() ?: 0.0
                            if (price <= 0) {
                                showError = true
                                errorMessage = "Price must be greater than 0"
                            } else {
                                showError = false
                                currentStep++
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
            
            // Step 4: Wake Up Time
            AnimatedVisibility(
                visible = currentStep == 4,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "When do you wake up?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    SimpleTimePicker(
                        time = wakeUpTime,
                        onTimeSelected = { wakeUpTime = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            currentStep++
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
            
            // Step 5: Sleep Time
            AnimatedVisibility(
                visible = currentStep == 5,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "When do you go to sleep?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    SimpleTimePicker(
                        time = sleepTime,
                        onTimeSelected = { sleepTime = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            try {
                                val validatedData = UserData(
                                    gender = gender,
                                    age = age.toInt().coerceIn(12, 100),
                                    cigarettesPerDay = cigarettesPerDay.toInt().coerceIn(1, 100),
                                    cigarettePrice = cigarettePrice.toDouble().coerceAtLeast(0.0),
                                    yearsOfSmoking = yearsOfSmoking.toInt().coerceIn(0, 100),
                                    wakeUpTime = wakeUpTime,
                                    sleepTime = sleepTime
                                )
                                onComplete(validatedData)
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = "Invalid inputs:\n" +
                                              "- Age: 12-100\n" +
                                              "- Cigarettes/Day: 1-100\n" +
                                              "- Price: ≥ 0\n" +
                                              "- Years: 0-100"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Complete Setup")
                    }
                }
            }
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 