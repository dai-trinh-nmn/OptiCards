package com.android.opticards.ui.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.TransactionHistoryItem
import com.android.opticards.data.model.UserCardOverview
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TransactionGroup(
    val dateLabel: String,
    val items: List<TransactionHistoryItem>
)

class TransactionHistoryViewModel : ViewModel() {

    private val _groupedTransactions = MutableStateFlow<List<TransactionGroup>>(emptyList())
    val groupedTransactions: StateFlow<List<TransactionGroup>> = _groupedTransactions.asStateFlow()

    private val allFetchedItems = mutableListOf<TransactionHistoryItem>()

    private val _filterCards = MutableStateFlow<List<UserCardOverview>>(emptyList())
    val filterCards: StateFlow<List<UserCardOverview>> = _filterCards.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isNextPageLoading = MutableStateFlow(false)
    val isNextPageLoading: StateFlow<Boolean> = _isNextPageLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchMerchantQuery = MutableStateFlow("")
    val searchMerchantQuery: StateFlow<String> = _searchMerchantQuery.asStateFlow()

    val selectedCardLabel = MutableStateFlow("Chọn thẻ")
    val selectedDateLabel = MutableStateFlow("Thời gian")

    val selectedCardId = MutableStateFlow<Int?>(null)
    private var filterStartDate: String? = null
    private var filterEndDate: String? = null

    private var currentOffset = 0
    private val pageSize = 20
    private var isLastPage = false
    private var searchJob: Job? = null

    init {
        loadInitialData()
        fetchCardsForFilter()
    }

    private fun fetchCardsForFilter() {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getUserCards()
                if (response.isSuccessful) {
                    _filterCards.value = response.body()?.filter { it.cardStatus == "ACTIVE" } ?: emptyList()
                }
            } catch (e: Exception) { }
        }
    }

    fun loadInitialData() {
        currentOffset = 0
        isLastPage = false
        allFetchedItems.clear()

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            fetchHistoryData(isRefresh = true)
            _isLoading.value = false
        }
    }

    fun loadNextPage() {
        if (_isNextPageLoading.value || isLastPage) return
        viewModelScope.launch {
            _isNextPageLoading.value = true
            currentOffset += pageSize
            fetchHistoryData(isRefresh = false)
            _isNextPageLoading.value = false
        }
    }

    fun onSearchMerchantChanged(query: String) {
        _searchMerchantQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadInitialData()
        }
    }

    fun applyCardFilter(userCardId: Int?, label: String) {
        selectedCardId.value = userCardId
        selectedCardLabel.value = label
        loadInitialData()
    }

    fun applyDateFilter(startMillis: Long?, endMillis: Long?) {
        if (startMillis != null && endMillis != null) {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

            filterStartDate = isoFormat.format(Date(startMillis))
            filterEndDate = isoFormat.format(Date(endMillis + 86399999L))

            selectedDateLabel.value = "${displayFormat.format(Date(startMillis))} - ${displayFormat.format(Date(endMillis))}"
        } else {
            filterStartDate = null
            filterEndDate = null
            selectedDateLabel.value = "Thời gian"
        }
        loadInitialData()
    }

    private suspend fun fetchHistoryData(isRefresh: Boolean) {
        try {
            val response = ApiClient.apiService.getTransactionHistory(
                userCardId = selectedCardId.value,
                startDate = filterStartDate,
                endDate = filterEndDate,
                merchantName = _searchMerchantQuery.value.takeIf { it.isNotBlank() },
                limit = pageSize,
                offset = currentOffset
            )

            if (response.isSuccessful) {
                val data = response.body()
                val newItems = data?.transactions ?: emptyList()
                val totalCount = data?.totalCount ?: 0

                if (isRefresh) allFetchedItems.clear()
                allFetchedItems.addAll(newItems)

                if (allFetchedItems.size >= totalCount || newItems.size < pageSize) isLastPage = true

                _groupedTransactions.value = groupItemsByDate(allFetchedItems)
            } else {
                _errorMessage.value = "Lỗi kết nối hệ thống: ${response.code()}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Lỗi kết nối mạng, vui lòng thử lại!"
            e.printStackTrace()
        }
    }

    private fun groupItemsByDate(items: List<TransactionHistoryItem>): List<TransactionGroup> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputDayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val cal = Calendar.getInstance()
        val todayStr = outputDayFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = outputDayFormat.format(cal.time)

        val rawGroups = items.groupBy { item ->
            try {
                val date = inputFormat.parse(item.transactionDate)
                if (date != null) outputDayFormat.format(date) else "Không rõ ngày"
            } catch (e: Exception) { "Không rõ ngày" }
        }

        return rawGroups.map { (dateStr, itemList) ->
            val structuralLabel = when (dateStr) {
                todayStr -> "Hôm nay"
                yesterdayStr -> "Hôm qua"
                else -> dateStr
            }
            TransactionGroup(dateLabel = structuralLabel, items = itemList)
        }
    }

    fun clearError() { _errorMessage.value = null }
}