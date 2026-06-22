package com.android.opticards.ui.onboarding.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.CachePolicy
import kotlinx.coroutines.Dispatchers
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.BankResponse
import com.android.opticards.data.model.CardPayload
import com.android.opticards.data.model.CardTemplate
import com.android.opticards.data.model.MembershipPayload
import com.android.opticards.data.model.OnboardingSubmitPayload
import com.android.opticards.data.network.ApiClient
import com.android.opticards.ui.onboarding.CardSpendInput
import com.android.opticards.ui.onboarding.UserCardDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedCards = MutableStateFlow<List<UserCardDraft>>(emptyList())
    val selectedCards: StateFlow<List<UserCardDraft>> = _selectedCards.asStateFlow()

    private val _selectedMemberships = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedMemberships: StateFlow<Map<Int, String>> = _selectedMemberships.asStateFlow()

    private val _cardSpendInputs = MutableStateFlow<Map<Int, CardSpendInput>>(emptyMap())
    val cardSpendInputs: StateFlow<Map<Int, CardSpendInput>> = _cardSpendInputs.asStateFlow()

    private val _bankList = MutableStateFlow<List<BankResponse>>(emptyList())
    val bankList: StateFlow<List<BankResponse>> = _bankList.asStateFlow()

    private val _isSubmitSuccess = MutableStateFlow(false)
    val isSubmitSuccess: StateFlow<Boolean> = _isSubmitSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val apiService = ApiClient.apiService

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val responseBanks = apiService.getOnboardingInitData()
                if (responseBanks.isSuccessful && responseBanks.body() != null) {
                    val banks = responseBanks.body()!!
                    _bankList.value = banks
                    preloadAllImages(banks)
                }
            } catch (e: Exception) {
                Log.e("Onboarding", "Refresh failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearAllDrafts() {
        _selectedCards.value = emptyList()
        _selectedMemberships.value = emptyMap()
        _cardSpendInputs.value = emptyMap()
    }

    fun clearMembership() { _selectedMemberships.value = emptyMap() }
    fun clearCardSpend() { _cardSpendInputs.value = emptyMap() }

    private fun preloadAllImages(banks: List<BankResponse>) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            banks.forEach { bank ->
                val bankRequest = ImageRequest.Builder(context)
                    .data(bank.logoUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                context.imageLoader.enqueue(bankRequest)

                bank.cards.forEach { card ->
                    val cardRequest = ImageRequest.Builder(context)
                        .data(card.imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    context.imageLoader.enqueue(cardRequest)
                }
            }
        }
    }

    fun toggleCardSelection(bank: BankResponse, card: CardTemplate, isChecked: Boolean) {
        _selectedCards.update { currentList ->
            if (isChecked) {
                if (currentList.none { it.cardId == card.id }) {
                    currentList + UserCardDraft(cardId = card.id, bankId = bank.id, cardName = card.name, bankName = bank.name)
                } else currentList
            } else {
                currentList.filterNot { it.cardId == card.id }
            }
        }
    }

    fun getBanksRequiringMembership(): List<BankResponse> {
        val currentCards = _selectedCards.value
        val currentBanks = _bankList.value

        val cardsNeedingMembership = currentCards.filter { draft ->
            val originalBank = currentBanks.find { it.id == draft.bankId }
            val originalCard = originalBank?.cards?.find { it.id == draft.cardId }
            originalCard?.isMembershipLinked == true
        }
        val bankIdsNeedingMembership = cardsNeedingMembership.map { it.bankId }.distinct()
        return currentBanks.filter { it.id in bankIdsNeedingMembership }
    }

    fun selectMembership(bankId: Int, tier: String) {
        _selectedMemberships.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            if (newMap[bankId] == tier) {
                newMap.remove(bankId)
            } else {
                newMap[bankId] = tier
            }
            newMap
        }
    }

    fun updateOpenDate(cardId: Int, month: String, year: String) {
        val safeMonth = month.filter { it.isDigit() }.take(2)
        val safeYear = year.filter { it.isDigit() }.take(4)

        _cardSpendInputs.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            val current = newMap[cardId] ?: CardSpendInput()
            newMap[cardId] = current.copy(openMonth = safeMonth, openYear = safeYear)
            newMap
        }
    }

    fun updateEstimatedSpend(cardId: Int, amount: String) {
        val safeAmount = amount.filter { it.isDigit() }

        _cardSpendInputs.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            val current = newMap[cardId] ?: CardSpendInput()
            newMap[cardId] = current.copy(estimatedSpend = safeAmount)
            newMap
        }
    }

    fun getCardsForSpendTracking(): List<UserCardDraft> {
        val currentCards = _selectedCards.value
        val currentBanks = _bankList.value
        val currentMemberships = _selectedMemberships.value

        return currentCards.filter { draft ->
            val bank = currentBanks.find { it.id == draft.bankId }
            val card = bank?.cards?.find { it.id == draft.cardId }

            if (card != null) {
                val hasPolicy = card.hasSpendBasedWaiver
                val userMembership = currentMemberships[draft.bankId]
                val isAlreadyWaived = userMembership != null && card.waiverMembershipTiers.contains(userMembership)

                hasPolicy && !isAlreadyWaived
            } else {
                false
            }
        }
    }

    fun submitOnboarding() {
        viewModelScope.launch {
            try {
                val currentMemberships = _selectedMemberships.value
                val currentCards = _selectedCards.value
                val currentSpends = _cardSpendInputs.value

                val membershipsPayload = currentMemberships.map { (bankId, tierName) ->
                    MembershipPayload(bankId, tierName)
                }

                val cardsPayload = currentCards.map { draft ->
                    val spend = currentSpends[draft.cardId]

                    CardPayload(
                        cardId = draft.cardId,
                        activationDate = if (spend != null && spend.openMonth.isNotEmpty() && spend.openYear.isNotEmpty()) {
                            val y = spend.openYear.padStart(2, '0')
                            val m = spend.openMonth.padStart(2, '0')
                            "20$y-$m-01"
                        } else null,
                        accumulatedSpend = spend?.estimatedSpend?.toLongOrNull() ?: 0L
                    )
                }
                val payload = OnboardingSubmitPayload(membershipsPayload, cardsPayload)

                val response = apiService.submitOnboardingData(payload)
                if (response.isSuccessful) {
                    Log.d("Onboarding", "Submit thành công: ${response.body()}")
                    _isSubmitSuccess.value = true
                } else {
                    _errorMessage.value = "Đã có lỗi xảy ra. Vui lòng thử lại! (Mã lỗi: ${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng của thiết bị!"
            }
        }
    }

    fun resetSubmitStatus() { _isSubmitSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}