package com.example.eva20.utils

import android.util.Log

object Logger {
    private const val APP_TAG = "EvaApp"
    private var isDebugMode = true  // Set to false for production

    fun d(tag: String, message: String) {
        if (isDebugMode) {
            Log.d("$APP_TAG:$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        Log.i("$APP_TAG:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$APP_TAG:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$APP_TAG:$tag", message, throwable)
        } else {
            Log.e("$APP_TAG:$tag", message)
        }
    }

    // Set this to false when building for production
    fun setDebugMode(debug: Boolean) {
        isDebugMode = debug
    }
}