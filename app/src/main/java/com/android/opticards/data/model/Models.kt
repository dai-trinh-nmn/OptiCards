import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("creditCardCount") val creditCardCount: Int,
    @SerializedName("memberships") val memberships: List<String>,
    @SerializedName("hasMembershipWaiverCard") val hasMembershipWaiverCard: Boolean,
    @SerializedName("membershipOptions") val membershipOptions: List<MembershipOptionItem>
)

data class StatementDueItem(
    @SerializedName("userCardId") val userCardId: Int,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("cardImageUrl") val cardImageUrl: String,
    @SerializedName("statementMonth") val statementMonth: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("daysLeft") val daysLeft: Int
)

data class SettleStatementRequest(
    @SerializedName("statementMonth") val statementMonth: String
)

data class MembershipOptionItem(
    @SerializedName("bankId") val bankId: Int,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("logoUrl") val logoUrl: String,
    @SerializedName("currentTier") val currentTier: String?,
    @SerializedName("availableTiers") val availableTiers: List<String>
)