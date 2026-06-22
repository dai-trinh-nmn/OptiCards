package com.android.opticards.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.opticards.data.local.TokenManager
import com.android.opticards.ui.auth.screens.LoginScreen
import com.android.opticards.ui.contribution.screens.ContributionScreen
import com.android.opticards.ui.home.screens.HomeScreen
import com.android.opticards.ui.home.screens.SearchScreen
import com.android.opticards.ui.onboarding.viewmodel.OnboardingViewModel
import com.android.opticards.ui.onboarding.screens.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.android.opticards.data.network.ApiClient.apiService
import com.android.opticards.ui.cards.screens.CardDetailScreen
import com.android.opticards.ui.cards.screens.CardsScreen
import com.android.opticards.ui.cards.screens.CategorySelectionScreen
import com.android.opticards.ui.cards.screens.EditCardScreen
import com.android.opticards.ui.cards.viewmodel.EditCardViewModel
import com.android.opticards.ui.home.screens.FavoriteListScreen
import com.android.opticards.ui.home.viewmodel.HomeViewModel
import com.android.opticards.ui.home.screens.TopMerchantListScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import com.android.opticards.ui.merchant.screens.MerchantDetailScreen
import com.android.opticards.ui.more.screens.MoreScreen
import com.android.opticards.ui.promotion.PromotionScreen
import com.android.opticards.ui.promotions.viewmodel.PromotionViewModel
import com.android.opticards.ui.transaction.TransactionHistoryScreen
import com.android.opticards.ui.transaction.screens.TransactionEntryScreen

@Composable
fun AppNavigation(
    tokenManager: TokenManager,
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    val navController = rememberNavController()

    val startDestination = remember {
        if (tokenManager.isLoggedIn()) {
            if (tokenManager.isOnboarded()) "HomeScreen" else "CardSelectionScreen"
        } else {
            "LoginScreen"
        }
    }

    var homeRefreshTrigger by remember { mutableIntStateOf(0) }
    var cardsRefreshTrigger by remember { mutableIntStateOf(0) }
    var transactionRefreshTrigger by remember { mutableIntStateOf(0) }
    var promotionRefreshTrigger by remember { mutableIntStateOf(0) }
    var moreRefreshTrigger by remember { mutableIntStateOf(0) }

    val navigateToTab: (Int) -> Unit = { tabIndex ->
        val currentRoute = navController.currentDestination?.route

        val targetRoute = when (tabIndex) {
            0 -> "HomeScreen"
            1 -> "CardsScreen"
            2 -> "TransactionHistoryScreen"
            3 -> "PromotionScreen"
            4 -> "MoreScreen"
            else -> "HomeScreen"
        }

        if (currentRoute == targetRoute) {
            when (tabIndex) {
                0 -> homeRefreshTrigger++
                1 -> cardsRefreshTrigger++
                2 -> transactionRefreshTrigger++
                3 -> promotionRefreshTrigger++
                4 -> moreRefreshTrigger++
            }
        } else {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("LoginScreen") {
            LoginScreen(
                onLoginSuccess = { isOnboarded ->
                    if (isOnboarded) {
                        navController.safeNavigate("HomeScreen") {
                            popUpTo("LoginScreen") { inclusive = true }
                        }
                    } else {
                        navController.safeNavigate("CardSelectionScreen") {
                            popUpTo("LoginScreen") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("CardSelectionScreen") {
            CardSelectionScreen(
                viewModel = onboardingViewModel,
                onNext = {
                    val banksNeedingMembership = onboardingViewModel.getBanksRequiringMembership()
                    if (banksNeedingMembership.isNotEmpty()) {
                        navController.safeNavigate("MembershipSelectionScreen")
                    } else {
                        navController.safeNavigate("CardDetailScreenTwo")
                    }
                },
                onGoToFinish = { navController.safeNavigate("FinishSetupScreen") }
            )
        }

        composable("MembershipSelectionScreen") {
            MembershipSelectionScreen(
                viewModel = onboardingViewModel,
                onBack = { navController.safePopBackStack() },
                onNext = {
                    val cardsForSpend = onboardingViewModel.getCardsForSpendTracking()
                    if (cardsForSpend.isNotEmpty()) {
                        navController.safeNavigate("CardDetailScreenTwo")
                    } else {
                        onboardingViewModel.submitOnboarding()
                        navController.safeNavigate("FinishSetupScreen")
                    }
                },
                onSkip = {
                    val cardsForSpend = onboardingViewModel.getCardsForSpendTracking()
                    if (cardsForSpend.isNotEmpty()) {
                        navController.safeNavigate("CardDetailScreenTwo")
                    } else {
                        onboardingViewModel.submitOnboarding()
                        navController.safeNavigate("FinishSetupScreen")
                    }
                }
            )
        }

        composable("CardDetailScreenTwo") {
            CardDetailScreenTwo(
                viewModel = onboardingViewModel,
                onBack = { navController.safePopBackStack() },
                onFinish = { navController.safeNavigate("FinishSetupScreen") },
                onSkip = { navController.safeNavigate("FinishSetupScreen") }
            )
        }

        composable("FinishSetupScreen") {
            FinishSetupScreen(
                onGoHome = {
                    tokenManager.setOnboarded(true)
                    navController.safeNavigate("HomeScreen") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("HomeScreen") { backStackEntry ->
            val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = backStackEntry)

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSearch = { navController.safeNavigate("SearchScreen") },
                onNavigateToFavorites = { navController.safeNavigate("FavoriteListScreen") },
                onNavigateToTopMerchants = { navController.safeNavigate("TopMerchantListScreen") },
                onTabSelected = navigateToTab,
                onNavigateToMerchantDetail = { merchantId ->
                    navController.safeNavigate("MerchantDetail/$merchantId")
                }
            )
        }

        composable("FavoriteListScreen") {
            val homeEntry = remember(it) { navController.getBackStackEntry("HomeScreen") }
            val sharedViewModel: HomeViewModel = viewModel(viewModelStoreOwner = homeEntry)

            FavoriteListScreen(
                viewModel = sharedViewModel,
                onNavigateBack = { navController.safePopBackStack() },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.safeNavigate("MerchantDetail/$merchantId")
                }
            )
        }

        composable("TopMerchantListScreen") {
            val homeEntry = remember(it) { navController.getBackStackEntry("HomeScreen") }
            val sharedViewModel: HomeViewModel = viewModel(viewModelStoreOwner = homeEntry)

            TopMerchantListScreen(
                viewModel = sharedViewModel,
                onNavigateBack = { navController.safePopBackStack() },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.safeNavigate("MerchantDetail/$merchantId")
                }
            )
        }

        composable("SearchScreen") {
            SearchScreen(
                onNavigateBack = { navController.safePopBackStack() },
                onNavigateToContribution = { query: String, isFromNoResults: Boolean ->
                    val encodedQuery = android.net.Uri.encode(query)
                    navController.safeNavigate("ContributionScreen?query=$encodedQuery&isFromNoResults=$isFromNoResults")
                },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.safeNavigate("MerchantDetail/$merchantId")
                }
            )
        }

        composable(
            route = "ContributionScreen?query={query}&isFromNoResults={isFromNoResults}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("isFromNoResults") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val rawQuerry = backStackEntry.arguments?.getString("query") ?: ""
            val decodedQuery = android.net.Uri.decode(rawQuerry)
            val isFromNoResults = backStackEntry.arguments?.getBoolean("isFromNoResults") ?: false

            ContributionScreen(
                initialQuery = decodedQuery,
                onNavigateBack = {
                    if (isFromNoResults) {
                        navController.popBackStack("HomeScreen", inclusive = false)
                    } else {
                        navController.safePopBackStack()
                    }
                }
            )
        }

        composable("CardsScreen") {
            CardsScreen(
                refreshTrigger = cardsRefreshTrigger,
                onAddCardClick = {
                    onboardingViewModel.clearAllDrafts()
                    onboardingViewModel.resetSubmitStatus()
                    onboardingViewModel.refreshData()
                    navController.safeNavigate("CardSelectionScreen")
                },
                onCardDetailClick = { cardId -> navController.safeNavigate("CardDetail/$cardId") },
                onCategorySetupClick = { cardId -> navController.safeNavigate("CategorySelection/$cardId") },
                onTabSelected = navigateToTab
            )
        }

        composable(
            route = "CardDetail/{userCardId}",
            arguments = listOf(navArgument("userCardId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userCardId = backStackEntry.arguments?.getInt("userCardId") ?: 0

            CardDetailScreen(
                userCardId = userCardId,
                onNavigateBack = { navController.safePopBackStack() },
                onEditTokenClick = { bankName, cardName, logoUrl ->
                    val encodedUrl = URLEncoder.encode(logoUrl, StandardCharsets.UTF_8.toString())
                    navController.safeNavigate("EditCard/$userCardId?bankName=$bankName&cardName=$cardName&logoUrl=$encodedUrl")
                },
                onChangeCategoryClick = {
                    navController.safeNavigate("CategorySelection/$userCardId")
                }
            )
        }

        composable(
            route = "EditCard/{userCardId}?bankName={bankName}&cardName={cardName}&logoUrl={logoUrl}",
            arguments = listOf(
                navArgument("userCardId") { type = NavType.IntType },
                navArgument("bankName") { type = NavType.StringType; defaultValue = "" },
                navArgument("cardName") { type = NavType.StringType; defaultValue = "" },
                navArgument("logoUrl") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val userCardId = backStackEntry.arguments?.getInt("userCardId") ?: 0
            val bankName = backStackEntry.arguments?.getString("bankName") ?: ""
            val cardName = backStackEntry.arguments?.getString("cardName") ?: ""
            val logoUrl = backStackEntry.arguments?.getString("logoUrl") ?: ""

            val editViewModel: EditCardViewModel = viewModel()

            LaunchedEffect(userCardId) {
                try {
                    val response = apiService.getCardDetail(userCardId)
                    if (response.isSuccessful && response.body() != null) {
                        editViewModel.initData(response.body()!!)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            EditCardScreen(
                userCardId = userCardId,
                viewModel = editViewModel,
                bankName = bankName,
                cardName = cardName,
                logoUrl = logoUrl,
                onBack = { navController.safePopBackStack() },
                onSave = { navController.safePopBackStack() }
            )
        }

        composable(
            route = "CategorySelection/{userCardId}",
            arguments = listOf(navArgument("userCardId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userCardId = backStackEntry.arguments?.getInt("userCardId") ?: 0
            CategorySelectionScreen(
                userCardId = userCardId,
                onNavigateBack = { navController.safePopBackStack() }
            )
        }

        composable(
            route = "MerchantDetail/{merchantId}",
            arguments = listOf(navArgument("merchantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val merchantId = backStackEntry.arguments?.getInt("merchantId") ?: 0
            MerchantDetailScreen(
                merchantId = merchantId,
                onNavigateBack = { navController.safePopBackStack() },
                onNavigateToContribution = { encodedMerchantName: String ->
                    navController.safeNavigate("ContributionScreen?query=$encodedMerchantName&isFromNoResults=false")
                },
                onNavigateToTransactionEntry = { mId: Int, mccCode: String, amount: Int, paymentChannel: String ->
                    navController.safeNavigate("TransactionEntry/$mId?mccCode=$mccCode&amount=$amount&paymentChannel=$paymentChannel")
                }
            )
        }

        composable(
            route = "TransactionEntry/{merchantId}?mccCode={mccCode}&amount={amount}&paymentChannel={paymentChannel}",
            arguments = listOf(
                navArgument("merchantId") { type = NavType.IntType },
                navArgument("mccCode") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("amount") { type = NavType.IntType; defaultValue = 0 },
                navArgument("paymentChannel") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val merchantId = backStackEntry.arguments?.getInt("merchantId") ?: 0
            val mccCode = backStackEntry.arguments?.getString("mccCode") ?: ""
            val amount = backStackEntry.arguments?.getInt("amount") ?: 0
            val paymentChannel = backStackEntry.arguments?.getString("paymentChannel") ?: "ANY"

            TransactionEntryScreen(
                merchantId = merchantId,
                mccCodeParam = mccCode,
                initialAmount = amount,
                initialChannel = paymentChannel,
                onNavigateBack = { isSuccess ->
                    if (isSuccess) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh_history", true)
                    }
                    navController.safePopBackStack()
                }
            )
        }

        composable("TransactionHistoryScreen") { backStackEntry ->
            val historyViewModel: com.android.opticards.ui.transaction.viewmodel.TransactionHistoryViewModel = viewModel()

            val savedStateHandle = backStackEntry.savedStateHandle
            val shouldRefresh = savedStateHandle.get<Boolean>("refresh_history") ?: false

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    historyViewModel.loadInitialData()
                    savedStateHandle.remove<Boolean>("refresh_history")
                }
            }

            TransactionHistoryScreen(
                viewModel = historyViewModel,
                refreshTrigger = transactionRefreshTrigger,
                onNavigateToTransactionEntry = {
                    navController.safeNavigate("TransactionEntry/0?mccCode=&amount=0&paymentChannel=ANY")
                },
                onTabSelected = navigateToTab
            )
        }

        composable("PromotionScreen") {
            val promotionViewModel: PromotionViewModel = viewModel()
            PromotionScreen(
                viewModel = promotionViewModel,
                refreshTrigger = promotionRefreshTrigger,
                onTabSelected = navigateToTab
            )
        }
        composable("MoreScreen") {
            val moreViewModel: com.android.opticards.ui.more.viewmodel.MoreViewModel = viewModel()

            MoreScreen(
                viewModel = moreViewModel,
                refreshTrigger = moreRefreshTrigger,
                onTabSelected = navigateToTab,
                onLogoutClick = {
                    tokenManager.clearAllToken()
                    navController.navigate("LoginScreen") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

private var lastNavTime = 0L
private const val NAV_COOLDOWN = 300L

fun NavHostController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val currentTime = System.currentTimeMillis()
    val isSameRoute = currentDestination?.route == route

    if (!isSameRoute && (currentTime - lastNavTime >= NAV_COOLDOWN)) {
        lastNavTime = currentTime
        navigate(route, builder)
    }
}

fun NavHostController.safePopBackStack() {
    val currentTime = System.currentTimeMillis()
    val hasBackStack = previousBackStackEntry != null

    if (hasBackStack && (currentTime - lastNavTime >= NAV_COOLDOWN)) {
        lastNavTime = currentTime
        popBackStack()
    }
}