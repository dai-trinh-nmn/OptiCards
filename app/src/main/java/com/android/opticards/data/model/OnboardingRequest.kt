package com.android.opticards.data.model

data class CardPayload(
    val cardId: Int,
    val activationDate: String?,
    val accumulatedSpend: Long,
)

data class MembershipPayload(
    val bankId: Int,
    val tierName: String
)

data class OnboardingSubmitPayload(
    val memberships: List<MembershipPayload>,
    val cards: List<CardPayload>
)