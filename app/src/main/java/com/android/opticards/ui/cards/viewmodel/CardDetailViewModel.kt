package com.android.opticards.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.CardDetailResponse
import com.android.opticards.data.model.TransactionHistoryItem
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayGroup(val dateLabel: String, val items: List<TransactionHistoryItem>)
data class CycleGroup(val cycleLabel: String, val dayGroups: List<DayGroup>)

class CardDetailViewModel : ViewModel() {

    private val _cardDetail = MutableStateFlow<CardDetailResponse?>(null)
    val cardDetail = _cardDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _cancelState = MutableStateFlow(SubmitState.IDLE)
    val cancelState = _cancelState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _groupedTransactions = MutableStateFlow<List<CycleGroup>>(emptyList())
    val groupedTransactions = _groupedTransactions.asStateFlow()

    private val _isTransactionsLoading = MutableStateFlow(false)
    val isTransactionsLoading = _isTransactionsLoading.asStateFlow()

    fun loadCardDetail(userCardId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.apiService.getCardDetail(userCardId)
                if (response.isSuccessful) {
                    _cardDetail.value = response.body()
                } else {
                    _errorMessage.value = "Không thể tải thông tin chi tiết thẻ từ máy chủ"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Không thể kết nối internet. Vui lòng thử lại!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllCardTransactions(userCardId: Int) {
        viewModelScope.launch {
            _isTransactionsLoading.value = true
            try {
                val response = ApiClient.apiService.getTransactionHistory(
                    userCardId = userCardId, limit = 1000, offset = 0
                )
                if (response.isSuccessful) {
                    val items = response.body()?.transactions ?: emptyList()
                    _groupedTransactions.value = groupTransactionsByCycleAndDate(items)
                }
            } catch (e: Exception) {}
            _isTransactionsLoading.value = false
        }
    }

    private fun groupTransactionsByCycleAndDate(items: List<TransactionHistoryItem>): List<CycleGroup> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputDayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val cal = Calendar.getInstance()
        val todayStr = outputDayFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = outputDayFormat.format(cal.time)

        val cycleGroupsMap = items.groupBy { it.cycleLabel ?: "Kỳ không xác định" }

        return cycleGroupsMap.map { (cycle, cycleItems) ->
            val dayGroupsMap = cycleItems.groupBy { item ->
                try {
                    val date = inputFormat.parse(item.transactionDate)
                    if (date != null) outputDayFormat.format(date) else "Không rõ ngày"
                } catch (e: Exception) { "Không rõ ngày" }
            }

            val dayGroups = dayGroupsMap.map { (dateStr, dayItems) ->
                val structuralLabel = when (dateStr) {
                    todayStr -> "Hôm nay"
                    yesterdayStr -> "Hôm qua"
                    else -> dateStr
                }
                DayGroup(dateLabel = structuralLabel, items = dayItems)
            }

            CycleGroup(cycleLabel = cycle, dayGroups = dayGroups)
        }
    }

    fun cancelCard(userCardId: Int) {
        viewModelScope.launch {
            _cancelState.value = SubmitState.LOADING
            try {
                val response = ApiClient.apiService.cancelCard(userCardId)
                if (response.isSuccessful) _cancelState.value = SubmitState.SUCCESS
                else _errorMessage.value = "Yêu cầu hủy thẻ thất bại từ hệ thống"
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối máy chủ. Vui lòng kiểm tra mạng!"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun resetCancelState() { _cancelState.value = SubmitState.IDLE }
}