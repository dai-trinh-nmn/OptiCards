package com.android.opticards.data.local

import android.content.Context
import android.content.SharedPreferences

class ReminderPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)

    var isReminderEnabled: Boolean
        get() = prefs.getBoolean("IS_ENABLED", false)
        set(value) = prefs.edit().putBoolean("IS_ENABLED", value).apply()

    var daysBefore: Int
        get() = prefs.getInt("DAYS_BEFORE", 1)
        set(value) = prefs.edit().putInt("DAYS_BEFORE", value).apply()

    var reminderHour: Int
        get() = prefs.getInt("HOUR", 8)
        set(value) = prefs.edit().putInt("HOUR", value).apply()

    var reminderMinute: Int
        get() = prefs.getInt("MINUTE", 0)
        set(value) = prefs.edit().putInt("MINUTE", value).apply()
}