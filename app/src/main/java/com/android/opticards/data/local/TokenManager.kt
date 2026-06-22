package com.android.opticards.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("OptiCardsPrefs", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString("JWT_TOKEN", token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString("JWT_TOKEN", null)
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString("REFRESH_TOKEN", token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString("REFRESH_TOKEN", null)
    }

    fun setOnboarded(status: Boolean) {
        prefs.edit().putBoolean("IS_ONBOARDED", status).apply()
    }

    fun isOnboarded(): Boolean {
        return prefs.getBoolean("IS_ONBOARDED", false)
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun clearAllToken() {
        prefs.edit().clear().apply()
    }
}