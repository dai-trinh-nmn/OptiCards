package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class CardDetailResponse(
    @SerializedName("userCardId") val userCardId: Int,
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("cardImageUrl") val cardImageUrl: String,
    @SerializedName("cardStatus") val cardStatus: String,
    @SerializedName("cyclePeriod") val cyclePeriod: String,
    @SerializedName("specialWarning") val specialWarning: String?,

    @SerializedName("hasFlexCategories") val hasFlexCategories: Boolean,

    @SerializedName("annualFeeStatus") val annualFeeStatus: String,
    @SerializedName("annualFeeNotice") val annualFeeNotice: String,
    @SerializedName("annualFeeProgress") val annualFeeProgress: Float,

    @SerializedName("totalCashbackCurrent") val totalCashbackCurrent: Long,
    @SerializedName("totalCashbackMax") val totalCashbackMax: Long,
    @SerializedName("cashbackBreakdowns") val cashbackBreakdowns: List<CashbackCategoryProgress>,
    @SerializedName("conditions") val conditions: List<ConditionProgress>,
    @SerializedName("cashbackRuleSummary") val cashbackRuleSummary: String,
    @SerializedName("cashbackTncUrl") val cashbackTncUrl: String?,
    @SerializedName("rewardCurrency") val rewardCurrency: String = "CASHBACK",
    @SerializedName("activationDate") val activationDate: String? = null,
    @SerializedName("accumulatedSpend") val accumulatedSpend: Long = 0L,
    @SerializedName("recentTransactions") val recentTransactions: List<RecentTransactionItem>,
    @SerializedName("hasSpendBasedWaiver") val hasSpendBasedWaiver: Boolean = false
)

data class CashbackCategoryProgress(
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("currentAmount") val currentAmount: Long,
    @SerializedName("maxCap") val maxCap: Long
)

data class RecentTransactionItem(
    @SerializedName("transactionId") val transactionId: Int,
    @SerializedName("merchantName") val merchantName: String?,
    @SerializedName("amount") val amount: Long,
    @SerializedName("transactionDate") val transactionDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("cashback") val cashback: Long = 0L,
    @SerializedName("cashbackUnit") val cashbackUnit: String = "đ",
    @SerializedName("hasBeenRefunded") val hasBeenRefunded: Boolean = false
)

data class CategoryOption(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("cashbackPercentages") val cashbackPercentage: Float,
    @SerializedName("maxCap") val maxCap: Long?,
    @SerializedName("notice") val notice: String?
)

data class FlexCategoryInfoResponse(
    @SerializedName("maxCategories") val maxCategories: Int,
    @SerializedName("remainingChanges") val remainingChanges: Int,
    @SerializedName("effectTiming") val effectTiming: String,
    @SerializedName("currentSelectedIds") val currentSelectedIds: List<Int>,
    @SerializedName("availableCategories") val availableCategories: List<CategoryOption>,
)

data class UpdateCategoryRequest(
    @SerializedName("selectedCategoryIds") val selectedCategoryIds: List<Int>
)

data class UpdateCardRequest(
    @SerializedName("open_month") val open_month: Int?,
    @SerializedName("open_year") val open_year: Int?,
    @SerializedName("estimated_spend") val estimated_spend: Long?,
)
