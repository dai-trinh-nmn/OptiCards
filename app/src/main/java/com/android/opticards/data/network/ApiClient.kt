package com.android.opticards.data.network

import com.android.opticards.data.local.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://pi-server.tail59c177.ts.net/"

    private var tokenManager: TokenManager? = null
    private var onUserBanned: (() -> Unit)? = null
    private var onSessionExpired: (() -> Unit)? = null

    fun initialize(
        manager: TokenManager,
        onBanned: (() -> Unit)? = null,
        onExpired: (() -> Unit)? = null
    ) {
        tokenManager = manager
        onUserBanned = onBanned
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                tokenManager?.getAccessToken()?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val response = chain.proceed(requestBuilder.build())

                if (response.code == 403) {
                    onUserBanned?.invoke()
                }

                response
            }
            .authenticator(TokenAuthenticator(tokenManager!!, onSessionExpired))
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}