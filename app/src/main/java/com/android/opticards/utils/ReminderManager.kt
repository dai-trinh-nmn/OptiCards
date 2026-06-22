package com.android.opticards.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReminderManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(userCardId: Int, cardName: String, triggerTimeMillis: Long) {
        val intent = Intent(context, PaymentReminderReceiver::class.java).apply {
            putExtra("EXTRA_CARD_NAME", cardName)
            putExtra("EXTRA_NOTIFICATION_ID", userCardId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            userCardId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                    Log.d("ReminderManager", "Đã đặt báo thức chính xác cho thẻ $cardName")
                } else {
                    Log.e("ReminderManager", "Ứng dụng chưa được cấp quyền SCHEDULE_EXACT_ALARM")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                Log.d("ReminderManager", "Đã đặt báo thức cho thẻ $cardName (Android < 12)")
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(userCardId: Int) {
        val intent = Intent(context, PaymentReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            userCardId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("ReminderManager", "Đã hủy báo thức cho thẻ ID: $userCardId")
    }
}