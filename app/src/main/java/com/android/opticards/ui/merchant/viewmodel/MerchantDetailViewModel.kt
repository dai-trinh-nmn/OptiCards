package com.android.opticards.ui.merchant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.*
import com.android.opticards.data.network.ApiClient
import com.android.opticards.utils.AppEvent
import com.android.opticards.utils.AppEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantDetailViewModel : ViewModel() {

    private val _merchant = MutableStateFlow<MerchantDetailResponse?>(null)
    val merchant = _merchant.asStateFlow()

    private val _selectedMcc = MutableStateFlow("")
    val selectedMcc = _selectedMcc.asStateFlow()

    private val _inputAmount = MutableStateFlow("")
    val inputAmount = _inputAmount.asStateFlow()

    private val _suggestions = MutableStateFlow<SuggestionResponse?>(null)
    val suggestions = _suggestions.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(true)
    val isLoadingDetail = _isLoadingDetail.asStateFlow()

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions = _isLoadingSuggestions.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var debounceJob: Job? = null

    fun loadMerchantDetail(merchantId: Int) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            try {
                val response = ApiClient.apiService.getMerchantDetail(merchantId)
                if (response.isSuccessful) {
                    val detail = response.body()
                    _merchant.value = detail
                    if (detail != null && detail.mccCodes.isNotEmpty()) {
                        val firstMcc = detail.mccCodes.first()
                        _selectedMcc.value = firstMcc

                        val firstGroup = detail.mccGroups.find { it.mccCode == firstMcc }
                        val firstSub = firstGroup?.subServices?.firstOrNull()

                        fetchSuggestions(
                            merchantId,
                            firstMcc,
                            0,
                            firstSub?.paymentChannel ?: "ANY",
                            firstSub?.cardBrand
                        )
                    }
                } else {
                    _errorMessage.value = "Lỗi tải chi tiết: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Không thể kết nối đến máy chủ"
                e.printStackTrace()
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    fun onParametersChanged(merchantId: Int, mccCode: String, amount: String, paymentChannel: String, cardBrand: String?) {
        _selectedMcc.value = mccCode
        _inputAmount.value = amount.filter { it.isDigit() }.trimStart('0')

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            fetchSuggestions(merchantId, mccCode, _inputAmount.value.toIntOrNull() ?: 0, paymentChannel, cardBrand)
        }
    }

    private suspend fun fetchSuggestions(merchantId: Int, mccCode: String, amount: Int, paymentChannel: String, cardBrand: String?) {
        _isLoadingSuggestions.value = true
        try {
            val request = SuggestionRequest(mccCode, amount, paymentChannel, cardBrand)
            val response = ApiClient.apiService.getCardSuggestions(merchantId, request)
            if (response.isSuccessful) {
                _suggestions.value = response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoadingSuggestions.value = false
        }
    }

    fun toggleFavorite(merchantId: Int) {
        val current = _merchant.value ?: return
        val newStatus = !current.isFavorited
        val updated = current.copy(isFavorited = newStatus)
        _merchant.value = updated

        viewModelScope.launch {
            val overview = MerchantOverview(
                merchantId = merchantId,
                name = current.name,
                logoUrl = current.logoUrl,
                category = current.category,
                mccCodes = current.mccCodes,
                isFavorited = newStatus
            )
            AppEventBus.publish(AppEvent.MerchantFavoriteToggled(merchantId, newStatus, overview))

            try {
                ApiClient.apiService.toggleFavoriteMerchant(merchantId)
            } catch (e: Exception) {
                _merchant.value = current
                val rollbackOverview = overview.copy(isFavorited = current.isFavorited)
                AppEventBus.publish(AppEvent.MerchantFavoriteToggled(merchantId, current.isFavorited, rollbackOverview))
                _errorMessage.value = "Không thể đồng bộ trạng thái yêu thích"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}