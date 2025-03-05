package com.example.bestc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bestc.data.SupabaseClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (!isOtpSent) "Enter Phone Number" else "Enter OTP",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!isOtpSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { 
                    // Only allow digits and limit to 10 numbers
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        phoneNumber = it
                    }
                },
                label = { Text("Phone Number") },
                prefix = { Text("+91 ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter 10-digit mobile number",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { 
                    // Only allow digits and limit to 6 numbers
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otp = it
                    }
                },
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter 6-digit OTP sent to +91 $phoneNumber",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        if (!isOtpSent) {
                            // Send OTP
                            SupabaseClient.signInWithPhone("+91$phoneNumber")
                            isOtpSent = true
                        } else {
                            // Verify OTP
                            SupabaseClient.verifyOtp("+91$phoneNumber", otp)
                            onAuthSuccess()
                        }
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && 
                     ((isOtpSent && otp.length == 6) || 
                      (!isOtpSent && phoneNumber.length == 10))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (!isOtpSent) "Send OTP" else "Verify")
            }
        }

        if (isOtpSent) {
            TextButton(
                onClick = { 
                    if (!isLoading) {
                        isOtpSent = false
                        otp = ""
                    }
                }
            ) {
                Text("Change Phone Number")
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
} 