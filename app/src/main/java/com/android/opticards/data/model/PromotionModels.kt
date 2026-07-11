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
    val timeRemainingLabel: String
        get() {
            if (endDate.isNullOrEmpty()) return "Đã hết hạn"
            return try {
                val cleanDate = endDate.substringBefore('+').substringBefore('.')
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val end = format.parse(cleanDate) ?: return "Đã hết hạn"
                val diff = end.time - System.currentTimeMillis()

                if (diff <= 0) {
                    "Đã hết hạn"
                } else {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    if (days >= 1) {
                        "Còn $days ngày"
                    } else {
                        val hours = TimeUnit.MILLISECONDS.toHours(diff)
                        if (hours >= 1) {
                            "Còn $hours giờ"
                        } else {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                            if (minutes > 0) "Còn $minutes phút" else "Sắp hết hạn"
                        }
                    }
                }
            } catch (e: Exception) {
                "Đã hết hạn"
            }
        }

    val isExpired: Boolean
        get() = timeRemainingLabel == "Đã hết hạn"
}

data class PromotionListResponse(
    @SerializedName("promotions") val promotions: List<PromotionOverview>,
    @SerializedName("totalCount") val totalCount: Int
)