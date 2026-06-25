package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class GoogleTokenRequest(
    @SerializedName("idToken") val idToken: String
)

data class AuthResponse(
    val success: Boolean?,
    val message: String?,
    val role: String = "user",
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    val isOnboarded: Boolean?,
    val isBanned: Boolean? = false,
    val user: UserInfo?
)

data class UserInfo(
    val email: String?,
    @SerializedName("fullName") val fullName: String?,
    val avatarUrl: String?
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    val success: Boolean?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)