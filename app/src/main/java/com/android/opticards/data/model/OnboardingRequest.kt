package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class CardPayload(
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("activationDate") val activationDate: String?,
    @SerializedName("accumulatedSpend") val accumulatedSpend: Long,
)

data class MembershipPayload(
    @SerializedName("bankId") val bankId: Int,
    @SerializedName("tierName") val tierName: String
)

data class OnboardingSubmitPayload(
    @SerializedName("memberships") val memberships: List<MembershipPayload>,
    @SerializedName("cards") val cards: List<CardPayload>
)