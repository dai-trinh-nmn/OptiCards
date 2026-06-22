package com.android.opticards.utils

import com.android.opticards.data.model.MerchantOverview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AppEvent {
    data class MerchantFavoriteToggled(
        val merchantId: Int,
        val isFavorited: Boolean,
        val merchant: MerchantOverview? = null
    ) : AppEvent()
}

object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun publish(event: AppEvent) {
        _events.emit(event)
    }
}