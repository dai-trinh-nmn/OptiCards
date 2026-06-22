package com.android.opticards.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PaymentReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Đã đến giờ! Bắt đầu tạo thông báo...")

        val cardName = intent.getStringExtra("EXTRA_CARD_NAME") ?: "Thẻ tín dụng"
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", 1001)

        val helper = NotificationHelper(context)
        helper.showNotification(
            title = "Đến hạn thanh toán thẻ!",
            message = "Thẻ $cardName của bạn sắp đến hạn thanh toán. Hãy kiểm tra và thanh toán ngay không ảnh hưởng tới tín dụng cá nhân!.",
            notificationId = notificationId
        )
    }
}