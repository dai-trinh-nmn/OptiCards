package com.android.opticards.ui.onboarding

data class CardSpendInput(
    val openMonth: String = "",
    val openYear: String = "",
    val estimatedSpend: String = ""
)

data class UserCardDraft(
    val cardId: Int,
    val bankId: Int,
    val cardName: String,
    val bankName: String,
    var last4Physical: String? = null,
    var last4Virtual: String? = null,
    var activationDate: String? = null,
    var estimatedSpend: Long? = null
)