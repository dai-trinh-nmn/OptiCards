package com.android.opticards.ui.promotion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.ui.components.OptiCardsBottomNavigation
import com.android.opticards.ui.components.PromotionCard
import com.android.opticards.ui.promotions.viewmodel.PromotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionScreen(
    viewModel: PromotionViewModel = viewModel(),
    refreshTrigger: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    val promotions by viewModel.promotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()

    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            listState.animateScrollToItem(0)
            viewModel.refreshData()
        }
    }

    val uriHandler = LocalUriHandler.current
    val tabs = listOf("Dành cho bạn", "Tất cả", "Đã kết thúc")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ưu đãi", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            OptiCardsBottomNavigation(currentTab = 3, onTabSelected = onTabSelected)
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF157FEC),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF157FEC)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) Color(0xFF157FEC) else Color.Gray
                            )
                        }
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading && promotions.isNotEmpty(),
                onRefresh = { viewModel.refreshData() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isLoading && promotions.isNotEmpty(),
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = Color(0xFF157FEC)
                    )
                }
            ) {
                if (isLoading && promotions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF157FEC))
                    }
                } else if (promotions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có ưu đãi nào khả dụng.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(promotions) { promo ->
                            PromotionCard(
                                promo = promo,
                                modifier = Modifier.clickable {
                                    promo.tncUrl?.let { url ->
                                        try { uriHandler.openUri(url) } catch (e: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}