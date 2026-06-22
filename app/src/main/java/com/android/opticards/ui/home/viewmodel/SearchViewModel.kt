package com.android.opticards.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.MerchantOverview
import com.android.opticards.data.network.ApiClient
import com.android.opticards.utils.AppEvent
import com.android.opticards.utils.AppEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<MerchantOverview>>(emptyList())
    val searchResults: StateFlow<List<MerchantOverview>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    init {
        viewModelScope.launch {
            AppEventBus.events.collectLatest { event ->
                when (event) {
                    is AppEvent.MerchantFavoriteToggled -> {
                        _searchResults.update { list ->
                            list.map { merchant ->
                                if (merchant.merchantId == event.merchantId) {
                                    merchant.copy(isFavorited = event.isFavorited)
                                } else merchant
                            }
                        }
                    }
                }
            }
        }
    }

    fun searchMerchants(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _hasSearched.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.apiService.searchMerchants(query)
                if (response.isSuccessful) {
                    _searchResults.value = response.body() ?: emptyList()
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                _errorMessage.value = "Không thể kết nối máy chủ"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun resetSearch() {
        _hasSearched.value = false
        _searchResults.value = emptyList()
        _errorMessage.value = null
    }

    fun toggleFavorite(merchantId: Int) {
        val oldList = _searchResults.value
        var updatedMerchant: MerchantOverview? = null

        _searchResults.update { list ->
            list.map { merchant ->
                if (merchant.merchantId == merchantId) {
                    val toggled = merchant.copy(isFavorited = !merchant.isFavorited)
                    updatedMerchant = toggled
                    toggled
                } else merchant
            }
        }

        viewModelScope.launch {
            updatedMerchant?.let {
                AppEventBus.publish(AppEvent.MerchantFavoriteToggled(it.merchantId, it.isFavorited, it))
            }
            try {
                val response = ApiClient.apiService.toggleFavoriteMerchant(merchantId)
                if (!response.isSuccessful) {
                    _searchResults.value = oldList
                    _errorMessage.value = "Không thể lưu yêu thích. Vui lòng thử lại!"
                    updatedMerchant?.let {
                        AppEventBus.publish(AppEvent.MerchantFavoriteToggled(it.merchantId, !it.isFavorited, oldList.find { m -> m.merchantId == it.merchantId }))
                    }
                }
            } catch (e: Exception) {
                _searchResults.value = oldList
                _errorMessage.value = "Lỗi kết nối khi lưu yêu thích!"
            }
        }
    }
}