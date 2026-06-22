package com.android.opticards.ui.home.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.ui.components.MerchantCard
import com.android.opticards.ui.components.PromotionCard
import com.android.opticards.ui.components.OptiCardsBottomNavigation
import com.android.opticards.ui.components.CustomPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.android.opticards.ui.home.viewmodel.HomeViewModel
import com.android.opticards.utils.singleClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit,
    onNavigateToTopMerchants: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onNavigateToMerchantDetail: (Int) -> Unit,
    onNavigateToPromotion: (Int) -> Unit = {}
) {
    val homeData by viewModel.homeData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    if (errorMessage != null) {
        CustomPopup(
            message = errorMessage!!,
            primaryButtonText = "Đóng",
            onPrimaryClick = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }

    Scaffold(
        bottomBar = {
            OptiCardsBottomNavigation(currentTab = 0, onTabSelected = onTabSelected)
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchDashboardData() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    HomeHeader(
                        fullName = homeData?.fullName ?: "Khách",
                        cardCount = homeData?.cardCount ?: 0,
                        avatarUrl = homeData?.avatarUrl,
                        modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }

                item {
                    MccSearchBar(
                        onSearchClick = onNavigateToSearch,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                val favorites = homeData?.favoriteMerchants ?: emptyList()
                if (favorites.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Yêu thích", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Xem tất cả",
                                color = Color(0xFF157FEC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.singleClick { onNavigateToFavorites() }
                            )
                        }
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(favorites, key = { it.merchantId }) { merchant ->
                                MerchantCard(
                                    merchant = merchant,
                                    onFavoriteClick = { viewModel.toggleFavorite(merchant) },
                                    onClick = { onNavigateToMerchantDetail(merchant.merchantId) }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(top = if (favorites.isNotEmpty()) 16.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top Merchant", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Xem tất cả",
                            color = Color(0xFF157FEC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.singleClick { onNavigateToTopMerchants() }
                        )
                    }
                }

                item {
                    val merchants = homeData?.topMerchants ?: emptyList()
                    if (merchants.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(merchants, key = { it.merchantId }) { merchant ->
                                MerchantCard(
                                    merchant = merchant,
                                    onFavoriteClick = { viewModel.toggleFavorite(merchant) },
                                    onClick = { onNavigateToMerchantDetail(merchant.merchantId) }
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có dữ liệu", color = Color.Gray)
                        }
                    }
                }

                val promos = homeData?.promotions ?: emptyList()
                if (promos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ưu đãi nổi bật",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                        )
                    }

                    items(promos, key = { it.promoId }) { promo ->
                        PromotionCard(
                            promo = promo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .singleClick {
                                    if (!promo.tncUrl.isNullOrEmpty()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(promo.tncUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            onNavigateToPromotion(promo.promoId)
                                        }
                                    } else {
                                        onNavigateToPromotion(promo.promoId)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(fullName: String, cardCount: Int, avatarUrl: String?, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "$cardCount thẻ", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun MccSearchBar(onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .singleClick { onSearchClick() }
            .padding(horizontal = 16.dp)
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text("Tra cứu MCC...", color = Color.Gray, fontSize = 14.sp)
    }
}