package com.android.opticards.utils

import StatementDueItem
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.opticards.data.local.ReminderPrefs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Thiết bị vừa khởi động lại! Đang khôi phục hệ thống nhắc nhở...")

            val reminderPrefs = ReminderPrefs(context)
            if (!reminderPrefs.isReminderEnabled) return
            val reminderManager = ReminderManager(context)
            val gson = Gson()

            try {
                val jsonString = reminderPrefs.cachedDueStatements
                val type = object : TypeToken<List<StatementDueItem>>() {}.type
                val cachedItems: List<StatementDueItem> = gson.fromJson(jsonString, type) ?: emptyList()

                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val now = System.currentTimeMillis()

                cachedItems.forEach { item ->
                    val date = format.parse(item.dueDate)
                    if (date != null) {
                        val cal = Calendar.getInstance()
                        cal.time = date

                        cal.add(Calendar.DAY_OF_YEAR, -reminderPrefs.daysBefore)
                        cal.set(Calendar.HOUR_OF_DAY, reminderPrefs.reminderHour)
                        cal.set(Calendar.MINUTE, reminderPrefs.reminderMinute)
                        cal.set(Calendar.SECOND, 0)

                        val triggerTime = cal.timeInMillis

                        if (triggerTime > now) {
                            reminderManager.scheduleReminder(item.userCardId, item.cardName, triggerTime)
                            Log.d("BootReceiver", "Đã phục hồi báo thức cho thẻ: ${item.cardName}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Lỗi khi phục hồi báo thức: ${e.message}")
            }
        }
    }
}