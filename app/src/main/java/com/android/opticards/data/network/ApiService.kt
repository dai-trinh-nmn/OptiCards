package com.android.opticards.data.network

import SettleStatementRequest
import StatementDueItem
import UserProfileResponse
import com.android.opticards.data.model.AuthResponse
import com.android.opticards.data.model.BankResponse
import com.android.opticards.data.model.CardDetailResponse
import com.android.opticards.data.model.ContributionResponse
import com.android.opticards.data.model.FavoriteToggleResponse
import com.android.opticards.data.model.FlexCategoryInfoResponse
import com.android.opticards.data.model.GoogleTokenRequest
import com.android.opticards.data.model.HomeDashboardResponse
import com.android.opticards.data.model.MccDictionaryItem
import com.android.opticards.data.model.MembershipPayload
import com.android.opticards.data.model.MerchantDetailResponse
import com.android.opticards.data.model.MerchantOverview
import com.android.opticards.data.model.MerchantSimpleItem
import com.android.opticards.data.model.OnboardingSubmitPayload
import com.android.opticards.data.model.PromotionListResponse
import com.android.opticards.data.model.RefreshTokenRequest
import com.android.opticards.data.model.RefreshTokenResponse
import com.android.opticards.data.model.SuggestionRequest
import com.android.opticards.data.model.SuggestionResponse
import com.android.opticards.data.model.TransactionCreateRequest

import com.android.opticards.data.model.TransactionHistoryResponse
import com.android.opticards.data.model.UpdateCardRequest
import com.android.opticards.data.model.UpdateCategoryRequest
import com.android.opticards.data.model.UserCardOverview
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
import okhttp3.RequestBody
import okhttp3.MultipartBody
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    // AUTHENTICATION
    @POST("api/auth/google")
    suspend fun verifyGoogleToken(@Body token: GoogleTokenRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    // ONBOARDING
    @GET("api/v1/onboarding/onboarding-init")
    suspend fun getOnboardingInitData(): Response<List<BankResponse>>

    @POST("api/v1/onboarding/complete")
    suspend fun submitOnboardingData(@Body payload: OnboardingSubmitPayload): Response<Any>

    // HOMESCREEN
    @GET("api/v1/home/dashboard")
    suspend fun getHomeDashboard(): Response<HomeDashboardResponse>

    @GET("api/v1/merchants/search")
    suspend fun searchMerchants(@Query("q") keyword: String): Response<List<MerchantOverview>>

    // CONTRIBUTION
    @Multipart
    @POST("api/v1/merchants/contribute")
    suspend fun submitContribution(
        @Part("merchantName") merchantName: RequestBody,
        @Part("mccCode") mccCode: RequestBody,
        @Part("note") note: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<ContributionResponse>

    // FAVORITE
    @POST("api/v1/merchants/{merchantId}/favorite")
    suspend fun toggleFavoriteMerchant(@Path("merchantId") merchantId: Int): Response<FavoriteToggleResponse>

    // CARD
    @GET("api/v1/cards")
    suspend fun getUserCards(): Response<List<UserCardOverview>>

    @GET("api/v1/cards/{userCardId}/detail")
    suspend fun getCardDetail(@Path("userCardId") userCardId: Int): Response<CardDetailResponse>

    @GET("api/v1/cards/{userCardId}/flex-categories")
    suspend fun getFlexCategories(@Path("userCardId") userCardId: Int): Response<FlexCategoryInfoResponse>

    @POST("api/v1/cards/{userCardId}/flex-categories")
    suspend fun updateFlexCategories(
        @Path("userCardId") userCardId: Int,
        @Body request: UpdateCategoryRequest
    ): Response<Map<String, String>>

    @PUT("api/v1/cards/{user_card_id}")
    suspend fun updateCardDetail(
        @Path("user_card_id") userCardId: Int,
        @Body request: UpdateCardRequest
    ): Response<Any>

    @PUT("api/v1/cards/{userCardId}/cancel")
    suspend fun cancelCard(@Path("userCardId") userCardId: Int): Response<Map<String, String>>

    // MERCHANT
    @GET("api/v1/merchants/{merchantId}/detail")
    suspend fun getMerchantDetail(@Path("merchantId") merchantId: Int): Response<MerchantDetailResponse>

    @POST("api/v1/merchants/{merchantId}/suggest")
    suspend fun getCardSuggestions(@Path("merchantId") merchantId: Int, @Body request: SuggestionRequest): Response<SuggestionResponse>

    @GET("api/v1/merchants/dictionary/mcc")
    suspend fun getMccDictionary(): Response<List<MccDictionaryItem>>

    @GET("api/v1/merchants/dictionary/merchants")
    suspend fun getMerchantDictionary(): Response<List<MerchantSimpleItem>>

    // TRANSACTIONS
    @POST("api/v1/cards/{userCardId}/transactions")
    suspend fun recordTransaction(
        @Path("userCardId") userCardId: Int,
        @Body request: TransactionCreateRequest
    ): Response<Any>

    @GET("api/v1/cards/transactions/history")
    suspend fun getTransactionHistory(
        @Query("userCardId") userCardId: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("merchantName") merchantName: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<TransactionHistoryResponse>

    // PROMOTION
    @GET("api/v1/promotions")
    suspend fun getPromotions(
        @Query("filter_type") filterType: String, // "FOR_YOU", "ACTIVE", "ENDED"
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<PromotionListResponse>

    // MORE
    @GET("api/v1/more/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @GET("api/v1/more/statements/due")
    suspend fun getDueStatements(): Response<List<StatementDueItem>>

    @POST("api/v1/more/statements/{user_card_id}/settle")
    suspend fun settleStatement(
        @Path("user_card_id") userCardId: Int,
        @Body request: SettleStatementRequest
    ): Response<Any>

    @PUT("api/v1/more/memberships")
    suspend fun updateMembership(
        @Body request: MembershipPayload
    ): Response<Any>
}