package com.android.opticards.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.*
import com.android.opticards.data.network.ApiClient
import com.android.opticards.utils.AppEvent
import com.android.opticards.utils.AppEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _homeData = MutableStateFlow<HomeDashboardResponse?>(null)
    val homeData: StateFlow<HomeDashboardResponse?> = _homeData.asStateFlow()

    init {
        fetchDashboardData()

        viewModelScope.launch {
            AppEventBus.events.collectLatest { event ->
                when (event) {
                    is AppEvent.MerchantFavoriteToggled -> {
                        _homeData.update { currentData ->
                            if (currentData == null) return@update null

                            val newTopMerchants = currentData.topMerchants.map {
                                if (it.merchantId == event.merchantId) it.copy(isFavorited = event.isFavorited) else it
                            }

                            val newFavMerchants = if (event.isFavorited) {
                                val existing = currentData.favoriteMerchants.find { it.merchantId == event.merchantId }
                                if (existing != null) {
                                    currentData.favoriteMerchants.map { if (it.merchantId == event.merchantId) it.copy(isFavorited = true) else it }
                                } else if (event.merchant != null) {
                                    currentData.favoriteMerchants + event.merchant.copy(isFavorited = true)
                                } else {
                                    currentData.favoriteMerchants
                                }
                            } else {
                                currentData.favoriteMerchants.filter { it.merchantId != event.merchantId }
                            }

                            currentData.copy(
                                topMerchants = newTopMerchants,
                                favoriteMerchants = newFavMerchants
                            )
                        }
                    }
                }
            }
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getHomeDashboard()
                if (response.isSuccessful) {
                    _homeData.value = response.body()
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    fun toggleFavorite(merchant: MerchantOverview) {
        val currentData = _homeData.value ?: return
        val targetId = merchant.merchantId
        val newStatus = !merchant.isFavorited
        val updatedMerchant = merchant.copy(isFavorited = newStatus)

        val oldData = currentData.copy()

        _homeData.update { data ->
            if (data == null) return@update null
            val newTopMerchants = data.topMerchants.map {
                if (it.merchantId == targetId) it.copy(isFavorited = newStatus) else it
            }
            val newFavMerchants = if (newStatus) {
                val existing = data.favoriteMerchants.find { it.merchantId == targetId }
                if (existing != null) {
                    data.favoriteMerchants.map { if (it.merchantId == targetId) it.copy(isFavorited = true) else it }
                } else {
                    data.favoriteMerchants + updatedMerchant
                }
            } else {
                data.favoriteMerchants.filter { it.merchantId != targetId }
            }
            data.copy(topMerchants = newTopMerchants, favoriteMerchants = newFavMerchants)
        }

        viewModelScope.launch {
            AppEventBus.publish(AppEvent.MerchantFavoriteToggled(targetId, newStatus, updatedMerchant))
            try {
                val response = ApiClient.apiService.toggleFavoriteMerchant(targetId)
                if (!response.isSuccessful) {
                    _homeData.value = oldData
                    _errorMessage.value = "Lỗi đồng bộ yêu thích"
                    AppEventBus.publish(AppEvent.MerchantFavoriteToggled(targetId, !newStatus, merchant))
                }
            } catch (e: Exception) {
                _homeData.value = oldData
                _errorMessage.value = "Lỗi kết nối mạng"
            }
        }
    }
}