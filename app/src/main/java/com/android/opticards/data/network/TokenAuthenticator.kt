package com.android.opticards.data.network

import com.android.opticards.data.local.TokenManager
import com.android.opticards.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val onSessionExpired: (() -> Unit)?
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount >= 3) {
            return null
        }

        val currentRefreshToken = tokenManager.getRefreshToken() ?: return null

        val newToken = runBlocking {
            try {
                val refreshRequest = RefreshTokenRequest(currentRefreshToken)

                val refreshResponse = ApiClient.apiService.refreshAccessToken(refreshRequest)

                if (refreshResponse.success == true && refreshResponse.accessToken != null) {
                    tokenManager.saveAccessToken(refreshResponse.accessToken)

                    refreshResponse.refreshToken?.let {newRefreshToken ->
                        if (newRefreshToken.isNotBlank()) {
                            tokenManager.saveRefreshToken(newRefreshToken)
                        }
                    }
                    refreshResponse.accessToken
                } else {
                    onSessionExpired?.invoke()
                    null
                }
            } catch (e: Exception) {
                onSessionExpired?.invoke()
                null
            }
        }
        return if (newToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            null
        }
    }

    private val Response.responseCount: Int
        get() {
            var result = 1
            var prior = priorResponse
            while (prior != null) {
                result++
                prior = prior.priorResponse
            }
            return result
        }
}