package com.example.bestc.data

data class UserData(
    val gender: String = "",
    val age: Int = 0,
    val cigarettesPerDay: Int = 0,
    val cigarettePrice: Double = 0.0, // Price in Rupees
    val yearsOfSmoking: Int = 0,
    val wakeUpTime: String = "",
    val sleepTime: String = ""
) 