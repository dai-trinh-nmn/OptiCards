package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class MerchantOverview (
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("category") val category: String,
    @SerializedName("mccCodes") val mccCodes: List<String> = emptyList(),
    @SerializedName("favoriteCount") val favoriteCount: Int = 0,
    @SerializedName("isFavorited") val isFavorited: Boolean = false
)

data class HomeDashboardResponse(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("cardCount") val cardCount: Int,
    @SerializedName("topMerchants") val topMerchants: List<MerchantOverview>,
    @SerializedName("favoriteMerchants") val favoriteMerchants: List<MerchantOverview> = emptyList(),
    @SerializedName("promotions") val promotions: List<PromotionOverview> = emptyList()
)

data class FavoriteToggleResponse(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("isFavorited") val isFavorited: Boolean
)