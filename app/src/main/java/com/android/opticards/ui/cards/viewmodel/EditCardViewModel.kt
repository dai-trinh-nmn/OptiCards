package com.android.opticards.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.CardDetailResponse
import com.android.opticards.data.model.UpdateCardRequest
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SubmitState { IDLE, LOADING, SUCCESS, ERROR }

data class EditCardFormState(
    val openMonth: String = "",
    val openYear: String = "",
    val estimatedSpend: String = "",
    val hasSpendBasedWaiver: Boolean = true
)

class EditCardViewModel : ViewModel() {

    private val _formState = MutableStateFlow(EditCardFormState())
    val formState = _formState.asStateFlow()

    private val _submitState = MutableStateFlow(SubmitState.IDLE)
    val submitState = _submitState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var isInitialized = false
    private var baselineState = EditCardFormState()

    val hasChanges: StateFlow<Boolean> = _formState
        .map { current -> current != baselineState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun initData(cardDetail: CardDetailResponse) {
        if (isInitialized) return
        isInitialized = true

        var initialSpend = ""
        if (cardDetail.accumulatedSpend > 0) {
            initialSpend = cardDetail.accumulatedSpend.toString()
        }

        var initialY = ""
        var initialM = ""
        cardDetail.activationDate?.let { dateStr ->
            if (dateStr.length >= 10) {
                val parts = dateStr.substring(0, 10).split("-")
                if (parts.size >= 3) {
                    initialY = parts[0].takeLast(2)
                    initialM = parts[1]
                }
            }
        }

        val loadedState = EditCardFormState(
            openMonth = initialM,
            openYear = initialY,
            estimatedSpend = initialSpend,
            hasSpendBasedWaiver = cardDetail.hasSpendBasedWaiver
        )

        baselineState = loadedState
        _formState.value = loadedState
    }

    fun updateOpenMonth(value: String) {
        if (value.length <= 2 && value.all { it.isDigit() }) {
            _formState.update { it.copy(openMonth = value) }
        }
    }

    fun updateOpenYear(value: String) {
        if (value.length <= 2 && value.all { it.isDigit() }) {
            _formState.update { it.copy(openYear = value) }
        }
    }

    fun updateEstimatedSpend(value: String) {
        _formState.update { it.copy(estimatedSpend = value) }
    }

    fun saveCardDetails(userCardId: Int) {
        viewModelScope.launch {
            _submitState.value = SubmitState.LOADING
            _errorMessage.value = null

            try {
                val current = _formState.value

                val requestBody = UpdateCardRequest(
                    open_month = if (current.hasSpendBasedWaiver) current.openMonth.toIntOrNull() else null,
                    open_year = if (current.hasSpendBasedWaiver) current.openYear.toIntOrNull() else null,
                    estimated_spend = if (current.hasSpendBasedWaiver) current.estimatedSpend.replace(".", "").toLongOrNull() else 0L
                )

                val response = ApiClient.apiService.updateCardDetail(userCardId, requestBody)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.SUCCESS
                } else {
                    _submitState.value = SubmitState.ERROR
                    _errorMessage.value = "Lỗi khi cập nhật thông tin thẻ"
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.ERROR
                _errorMessage.value = "Không thể kết nối đến máy chủ"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun resetSubmitState() { _submitState.value = SubmitState.IDLE
    }
}