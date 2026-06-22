package com.android.opticards.data.model

data class BankResponse(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val cards: List<CardTemplate>,
    val membershipTiers: List<String> = emptyList()
)

data class CardTemplate(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isMembershipLinked: Boolean = false,
    val hasSpendBasedWaiver: Boolean = false,
    val waiverMembershipTiers: List<String> = emptyList(),
)