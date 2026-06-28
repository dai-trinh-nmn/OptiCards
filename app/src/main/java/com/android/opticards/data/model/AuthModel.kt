package com.android.opticards.data.model

import com.google.gson.annotations.SerializedName

data class GoogleTokenRequest(
    @SerializedName("idToken") val idToken: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("role") val role: String = "user",
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("isOnboarded") val isOnboarded: Boolean?,
    @SerializedName("isBanned") val isBanned: Boolean? = false,
    @SerializedName("user") val user: UserInfo?
)

data class UserInfo(
    @SerializedName("email") val email: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)