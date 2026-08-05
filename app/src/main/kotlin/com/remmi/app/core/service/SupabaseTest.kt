package com.remmi.app.core.services

import android.util.Log
import com.remmi.app.core.service.SupabaseService
import io.github.jan.supabase.postgrest.from

suspend fun testSupabase() {

    try {

        val rows = SupabaseService.client
            .from("test")
            .select()

        Log.d("Supabase", rows.data)

    } catch (e: Exception) {

        Log.e("Supabase", e.toString())

    }

}