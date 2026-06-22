package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PromotionOverview(
    @SerializedName("promoId") val promoId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("tncUrl") val tncUrl: String?,
    @SerializedName("tag") val tag: String?
) {
    val calculatedDaysLeft: Int
        get() {
            if (endDate.isNullOrEmpty()) return 0
            return try {
                val cleanDate = endDate.substringBefore('+').substringBefore('.')
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val end = format.parse(cleanDate) ?: return 0
                val diff = end.time - System.currentTimeMillis()

                if (diff < 0) 0 else TimeUnit.MILLISECONDS.toDays(diff).toInt()
            } catch (e: Exception) {
                0
            }
        }
}

data class PromotionListResponse(
    @SerializedName("promotions") val promotions: List<PromotionOverview>,
    @SerializedName("totalCount") val totalCount: Int
)