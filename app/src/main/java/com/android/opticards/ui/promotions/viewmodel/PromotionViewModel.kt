package com.android.opticards.ui.promotions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.PromotionOverview
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PromotionViewModel : ViewModel() {

    private val _promotions = MutableStateFlow<List<PromotionOverview>>(emptyList())
    val promotions: StateFlow<List<PromotionOverview>> = _promotions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    init {
        loadPromotions("FOR_YOU")
    }

    fun onTabSelected(index: Int) {
        if (_selectedTabIndex.value == index) return

        _selectedTabIndex.value = index
        _promotions.value = emptyList()

        val filterType = when (index) {
            0 -> "FOR_YOU"
            1 -> "ACTIVE"
            2 -> "ENDED"
            else -> "FOR_YOU"
        }
        loadPromotions(filterType)
    }

    fun refreshData() {
        val filterType = when (_selectedTabIndex.value) {
            0 -> "FOR_YOU"
            1 -> "ACTIVE"
            2 -> "ENDED"
            else -> "FOR_YOU"
        }
        loadPromotions(filterType)
    }

    private fun loadPromotions(filterType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getPromotions(
                    filterType = filterType,
                    limit = 50,
                    offset = 0
                )
                if (response.isSuccessful && response.body() != null) {
                    _promotions.value = response.body()!!.promotions
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}