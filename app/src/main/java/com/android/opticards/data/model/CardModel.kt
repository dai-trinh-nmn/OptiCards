package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class CashbackProgress(
    @SerializedName("ruleName") val ruleName: String,
    @SerializedName("currentAmount") val currentAmount: Long,
    @SerializedName("maxCap") val maxCap: Long
)
data class ConditionProgress(
    @SerializedName("conditionType") val conditionType: String,
    @SerializedName("current") val current: Long,
    @SerializedName("target") val target: Long,
    @SerializedName("isMet") val isMet: Boolean
)
data class UserCardOverview(
    @SerializedName("userCardId") val userCardId: Int,
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("cardImageUrl") val cardImageUrl: String,
    @SerializedName("cyclePeriod") val cyclePeriod: String,
    @SerializedName("cashbackProgresses") val cashbackProgresses: List<CashbackProgress>,
    @SerializedName("conditions") val conditions: List<ConditionProgress>,
    @SerializedName("needCategorySetup") val needsCategorySetup: Boolean? = false,
    @SerializedName("rewardCurrency") val rewardCurrency: String = "CASHBACK",
    @SerializedName("cardStatus") val cardStatus: String = "ACTIVE",
    @SerializedName("closeDate") val closeDate: String? = null
)
