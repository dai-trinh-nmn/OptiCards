package com.android.opticards.data.model

data class MerchantOverview (
    val merchantId: Int,
    val name: String,
    val logoUrl: String?,
    val category: String,
    val mccCodes: List<String> = emptyList(),
    val favoriteCount: Int = 0,
    val isFavorited: Boolean = false
)

data class HomeDashboardResponse(
    val fullName: String,
    val avatarUrl: String?,
    val cardCount: Int,
    val topMerchants: List<MerchantOverview>,
    val favoriteMerchants: List<MerchantOverview> = emptyList(),
    val promotions: List<PromotionOverview> = emptyList()
)

data class FavoriteToggleResponse(
    val merchantId: Int,
    val isFavorited: Boolean
)