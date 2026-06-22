package com.android.opticards.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }
    context.startActivity(intent)
}