package com.android.opticards.utils

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.singleClick(
    timeoutMillis: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    clickable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= timeoutMillis) {
            lastClickTime = currentTime
            onClick()
        }
    }
}