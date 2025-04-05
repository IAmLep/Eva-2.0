package com.example.eva20.utils

object Constants {
    // API Endpoints
    const val BASE_API_URL = "https://eva-backend-533306620971.europe-west1.run.app/api/"

    // SharedPreferences Keys
    const val PREF_USER_SETTINGS = "user_settings"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_AUTH_TOKEN = "auth_token"

    // Notification Channels
    const val CHANNEL_CHAT = "chat_notifications"
    const val CHANNEL_CALL = "call_notifications"

    // API Timeouts
    const val CONNECTION_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}