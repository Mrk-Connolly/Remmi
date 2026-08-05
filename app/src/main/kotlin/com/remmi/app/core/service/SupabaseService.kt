package com.remmi.app.core.service

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseService {

    // TODO: Move these into BuildConfig or local.properties before publishing.
    private const val SUPABASE_URL =
        "https://lmgexteedqzchmjdagxn.supabase.co"

    private const val SUPABASE_ANON_KEY =
        "sb_publishable_NHFmOe4l9Yhz8nbfZay_pg_fi5j6boy"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}