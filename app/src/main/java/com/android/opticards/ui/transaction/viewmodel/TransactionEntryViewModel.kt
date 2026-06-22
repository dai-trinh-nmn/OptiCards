package com.android.opticards.ui.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.*
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionEntryViewModel : ViewModel() {
    val merchantName = MutableStateFlow("")
    val mccCode = MutableStateFlow("")
    val amountStr = MutableStateFlow("")
    val selectedDate = MutableStateFlow(System.currentTimeMillis())
    val paymentMethod = MutableStateFlow("CONTACTLESS")
    val selectedCard = MutableStateFlow<CardSuggestionItem?>(null)

    val availableMccsForMerchant = MutableStateFlow<List<String>>(emptyList())
    val availablePaymentChannels = MutableStateFlow<List<String>>(listOf("CONTACTLESS", "CHIP"))

    val allMerchants = MutableStateFlow<List<MerchantSimpleItem>>(emptyList())
    val allMccs = MutableStateFlow<List<MccDictionaryItem>>(emptyList())

    val ownedCards = MutableStateFlow<List<CardSuggestionItem>>(emptyList())
    val merchantDetail = MutableStateFlow<MerchantDetailResponse?>(null)

    val isLoading = MutableStateFlow(true)
    val isSubmitting = MutableStateFlow(false)
    val submitSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private var currentMerchantId: Int = 0
    private var debounceJob: Job? = null
    private var typingDebounceJob: Job? = null

    private var isManuallySelectedCard = false

    fun initData(merchantId: Int, initialMcc: String, initialAmount: Int, initialChannel: String) {
        currentMerchantId = merchantId
        mccCode.value = initialMcc
        amountStr.value = if (initialAmount > 0) initialAmount.toString() else ""

        viewModelScope.launch {
            isLoading.value = true

            launch {
                try {
                    val mRes = ApiClient.apiService.getMerchantDictionary()
                    if (mRes.isSuccessful) allMerchants.value = mRes.body() ?: emptyList()
                } catch (e: Exception) {}
            }
            launch {
                try {
                    val mccRes = ApiClient.apiService.getMccDictionary()
                    if (mccRes.isSuccessful) allMccs.value = mccRes.body() ?: emptyList()
                } catch (e: Exception) {}
            }

            if (merchantId > 0) {
                try {
                    val detailRes = ApiClient.apiService.getMerchantDetail(merchantId)
                    if (detailRes.isSuccessful) {
                        val detail = detailRes.body()
                        merchantDetail.value = detail
                        merchantName.value = detail?.name ?: ""

                        if (detail != null) {
                            availableMccsForMerchant.value = detail.mccCodes
                            updatePaymentMethodsForMcc(initialMcc, detail)
                        }
                    }
                } catch (e: Exception) {}
            } else {
                paymentMethod.value = if (initialChannel == "ONLINE") "ONLINE" else "CONTACTLESS"
                availablePaymentChannels.value = if (initialChannel == "ONLINE") listOf("ONLINE") else listOf("CONTACTLESS", "CHIP")
            }
            fetchSuggestions()
            isLoading.value = false
        }
    }

    fun onMerchantNameChanged(name: String) {
        typingDebounceJob?.cancel()
        typingDebounceJob = viewModelScope.launch {
            delay(200)
            merchantName.value = name
        }
    }

    fun onMerchantSelected(merchant: MerchantSimpleItem) {
        currentMerchantId = merchant.merchantId
        merchantName.value = merchant.name
        isManuallySelectedCard = false
        viewModelScope.launch {
            try {
                val detailRes = ApiClient.apiService.getMerchantDetail(merchant.merchantId)
                if (detailRes.isSuccessful) {
                    val body = detailRes.body()
                    merchantDetail.value = body
                    if (body != null && body.mccCodes.isNotEmpty()) {
                        availableMccsForMerchant.value = body.mccCodes
                        val firstMcc = body.mccCodes.first()
                        mccCode.value = firstMcc
                        updatePaymentMethodsForMcc(firstMcc, body)
                    }
                }
            } catch (e: Exception) {}
            scheduleFetch()
        }
    }

    fun onMccSelected(mcc: String) {
        mccCode.value = mcc
        isManuallySelectedCard = false
        merchantDetail.value?.let { updatePaymentMethodsForMcc(mcc, it) }
        scheduleFetch()
    }

    private fun updatePaymentMethodsForMcc(mcc: String, detail: MerchantDetailResponse) {
        val group = detail.mccGroups.find { it.mccCode == mcc }
        if (group != null && group.subServices.isNotEmpty()) {
            val channels = group.subServices.map { it.paymentChannel }.distinct()
            val uiChannels = mutableListOf<String>()

            if (channels.contains("ONLINE")) uiChannels.add("ONLINE")
            if (channels.contains("OFFLINE")) {
                uiChannels.add("CONTACTLESS")
                uiChannels.add("CHIP")
            }

            availablePaymentChannels.value = uiChannels
            if (!uiChannels.contains(paymentMethod.value)) {
                paymentMethod.value = uiChannels.firstOrNull() ?: "CONTACTLESS"
            }
        }
    }

    fun onAmountChanged(newAmount: String) {
        amountStr.value = newAmount.filter { it.isDigit() }.trimStart('0')
        scheduleFetch()
    }

    fun onPaymentMethodChanged(method: String) {
        paymentMethod.value = method
        isManuallySelectedCard = false
        scheduleFetch()
    }

    fun onCardSelected(card: CardSuggestionItem) {
        selectedCard.value = card
        isManuallySelectedCard = true
    }

    fun onDateSelected(millis: Long) {
        selectedDate.value = millis
    }

    private fun scheduleFetch() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            fetchSuggestions()
        }
    }

    private suspend fun fetchSuggestions() {
        try {
            val amt = amountStr.value.toIntOrNull() ?: 0
            val req = SuggestionRequest(mccCode.value, amt, paymentMethod.value, null, true)
            val res = ApiClient.apiService.getCardSuggestions(currentMerchantId, req)

            if (res.isSuccessful) {
                val cards = res.body()?.ownedCards ?: emptyList()
                ownedCards.value = cards

                if (isManuallySelectedCard) {
                    val currentSelectedId = selectedCard.value?.cardId
                    selectedCard.value = cards.find { it.cardId == currentSelectedId } ?: cards.firstOrNull()
                } else {
                    selectedCard.value = cards.firstOrNull()
                }
            }
        } catch (e: Exception) {}
    }

    val calculatedSurcharge: StateFlow<Int> = combine(amountStr, paymentMethod, merchantDetail, mccCode) { amtStr, method, detail, mcc ->
        if (amtStr.isBlank()) return@combine 0

        val amt = amtStr.toIntOrNull() ?: 0
        var surcharge = 0
        detail?.mccGroups?.find { it.mccCode == mcc }?.subServices?.let { subs ->
            val channelToFind = if (method == "ONLINE") "ONLINE" else "OFFLINE"
            val matchedSub = subs.find { it.paymentChannel == channelToFind } ?: subs.firstOrNull()
            matchedSub?.let {
                surcharge = it.surchargeFixed + (amt * (it.surchargePercent / 100.0)).toInt()
            }
        }
        surcharge
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun submitTransaction() {
        val uCardId = selectedCard.value?.userCardId
        if (uCardId == null) {
            errorMessage.value = "Thẻ không hợp lệ hoặc bạn chưa sở hữu thẻ này!"
            return
        }

        viewModelScope.launch {
            isSubmitting.value = true
            errorMessage.value = null
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val dateStr = sdf.format(java.util.Date(selectedDate.value))

                val amount = amountStr.value.toIntOrNull() ?: 0
                val cashback = selectedCard.value?.expectedAmount ?: 0

                val request = TransactionCreateRequest(
                    merchantId = currentMerchantId,
                    merchantName = merchantName.value,
                    mccCode = mccCode.value,
                    amount = amount,
                    paymentChannel = paymentMethod.value,
                    transactionDate = dateStr,
                    surcharge = calculatedSurcharge.value,
                    cashback = cashback
                )

                val response = ApiClient.apiService.recordTransaction(uCardId, request)
                if (response.isSuccessful) {
                    submitSuccess.value = true
                } else {
                    errorMessage.value = "Không thể ghi nhận giao dịch. Vui lòng thử lại!"
                }
            } catch (e: Exception) {
                errorMessage.value = "Lỗi kết nối mạng!"
            } finally {
                isSubmitting.value = false
            }
        }
    }

    fun resetSuccess() { submitSuccess.value = false }
    fun clearError() { errorMessage.value = null }
}