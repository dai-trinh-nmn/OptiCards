package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class TransactionHistoryItem(
    @SerializedName("transactionId") val transactionId: Int,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("merchantLogoUrl") val merchantLogoUrl: String?,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("paymentChannel") val paymentChannel: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("cashback") val cashback: Int,
    @SerializedName("cashbackUnit") val cashbackUnit: String,
    @SerializedName("transactionDate") val transactionDate: String,
    @SerializedName("cycleLabel") val cycleLabel: String? = null,
    @SerializedName("hasBeenRefunded") val hasBeenRefunded: Boolean = false,
    @SerializedName("status") val status: String = "PURCHASE"
)

data class TransactionHistoryResponse(
    @SerializedName("transactions") val transactions: List<TransactionHistoryItem>,
    @SerializedName("totalCount") val totalCount: Int
)