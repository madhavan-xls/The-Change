package com.example.bestc.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.gotrue

object SupabaseClient {
    private const val SUPABASE_URL = "https://tjgeiplqxonylotmgpld.supabase.co"
    private const val SUPABASE_KEY = "your-anon-key-here" // The key that starts with eyJ...

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
    }

    suspend fun signInWithPhone(phoneNumber: String) {
        client.gotrue.signInWith(io.github.jan.supabase.gotrue.providers.Phone) {
            this.phone = phoneNumber
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String) {
        client.gotrue.verifyPhoneOtp(
            phoneNumber = phoneNumber,
            token = otp,
            type = io.github.jan.supabase.gotrue.providers.Phone
        )
    }
} 