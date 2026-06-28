package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class BankResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String,
    @SerializedName("cards") val cards: List<CardTemplate>,
    @SerializedName("membershipTiers") val membershipTiers: List<String> = emptyList()
)

data class CardTemplate(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("isMembershipLinked") val isMembershipLinked: Boolean = false,
    @SerializedName("hasSpendBasedWaiver") val hasSpendBasedWaiver: Boolean = false,
    @SerializedName("waiverMembershipTiers") val waiverMembershipTiers: List<String> = emptyList(),
)