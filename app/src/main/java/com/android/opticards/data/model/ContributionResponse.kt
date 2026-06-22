package com.android.opticards.data.model

data class ContributionResponse (
    val status: String,
    val message: String,
    val data: ContributionData?
)

data class ContributionData(
    val merchantName: String,
    val mccCode: String,
    val note: String?,
    val imageUrl: String?
)