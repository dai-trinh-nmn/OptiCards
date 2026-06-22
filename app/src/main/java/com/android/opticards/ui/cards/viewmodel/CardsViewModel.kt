package com.android.opticards.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.UserCardOverview
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardsViewModel : ViewModel() {

    private val _cards = MutableStateFlow<List<UserCardOverview>>(emptyList())
    val cards: StateFlow<List<UserCardOverview>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { fetchCards() }

    fun fetchCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getUserCards()
                if (response.isSuccessful) {
                    _cards.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}