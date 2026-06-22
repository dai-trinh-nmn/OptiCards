package com.android.opticards.data.model

data class CashbackProgress(
    val ruleName: String,
    val currentAmount: Long,
    val maxCap: Long
)
data class ConditionProgress(
    val conditionType: String,
    val current: Long,
    val target: Long,
    val isMet: Boolean
)
data class UserCardOverview(
    val userCardId: Int,
    val cardId: Int,
    val cardName: String,
    val cardImageUrl: String,
    val cyclePeriod: String,
    val cashbackProgresses: List<CashbackProgress>,
    val conditions: List<ConditionProgress>,
    val needsCategorySetup: Boolean? = false,
    val rewardCurrency: String = "CASHBACK",
    val cardStatus: String = "ACTIVE",
    val closeDate: String? = null
)
