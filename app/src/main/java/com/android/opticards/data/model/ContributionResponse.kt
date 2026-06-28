package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class ContributionResponse (
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ContributionData?
)

data class ContributionData(
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("mccCode") val mccCode: String,
    @SerializedName("note") val note: String?,
    @SerializedName("imageUrl") val imageUrl: String?
)