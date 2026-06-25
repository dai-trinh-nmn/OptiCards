package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serial

// --- CHI TIẾT MERCHANT ---
data class SubServiceDetail(
    @SerializedName("serviceId") val serviceId: Int,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("surchargeFixed") val surchargeFixed: Int,
    @SerializedName("surchargePercent") val surchargePercent: Float,
    @SerializedName("cardBrand") val cardBrand: String?,
    @SerializedName("paymentChannel") val paymentChannel: String,
    @SerializedName("note") val note: String?
)

data class MccGroupDetail(
    @SerializedName("mccCode") val mccCode: String,
    @SerializedName("mccDescription") val mccDescription: String,
    @SerializedName("subServices") val subServices: List<SubServiceDetail>
)

data class MerchantDetailResponse(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("category") val category: String,
    @SerializedName("mccCodes") val mccCodes: List<String>,
    @SerializedName("favoriteCount") val favoriteCount: Int,
    @SerializedName("isFavorited") val isFavorited: Boolean,
    @SerializedName("merchantUrl") val merchantUrl: String?,
    @SerializedName("mccGroups") val mccGroups: List<MccGroupDetail>
)

// --- GỢI Ý THẺ ---
data class SuggestionRequest(
    @SerializedName("mccCode") val mccCode: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("paymentChannel") val paymentChannel: String,
    @SerializedName("cardBrand") val cardBrand: String?,
    @SerializedName("fetchAllOwned") val fetchAllOwned: Boolean = false,
    @SerializedName("transactionDate") val transactionDate: String? = null
)

data class CardSuggestionItem(
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("userCardId") val userCardId: Int?,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("cashbackRate") val cashbackRate: Float,
    @SerializedName("expectedAmount") val expectedAmount: Int,
    @SerializedName("isMaxedOut") val isMaxedOut: Boolean,
    @SerializedName("note") val note: String?,
    @SerializedName("cashbackUnit") val cashbackUnit: String = "đ"
)

data class SuggestionResponse(
    @SerializedName("ownedCards") val ownedCards: List<CardSuggestionItem>,
    @SerializedName("discoverCards") val discoverCards: List<CardSuggestionItem>
)

data class MccDictionaryItem(
    @SerializedName("mccCode") val mccCode: String,
    @SerializedName("description") val description: String
)

data class MerchantSimpleItem(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String?
)

data class TransactionCreateRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("mccCode") val mccCode: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("paymentChannel") val paymentChannel: String,
    @SerializedName("transactionDate") val transactionDate: String,
    @SerializedName("surcharge") val surcharge: Int,
    @SerializedName("cashback") val cashback: Int
)