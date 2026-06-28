package com.android.opticards.ui.cards.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.data.model.UserCardOverview
import com.android.opticards.ui.components.OptiCardsBottomNavigation
import com.android.opticards.utils.singleClick
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import com.android.opticards.ui.cards.viewmodel.CardsViewModel
import com.android.opticards.ui.components.ProgressRow
import com.android.opticards.utils.formatNumber
import com.android.opticards.utils.getCurrencyUnit
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: CardsViewModel = viewModel(),
    refreshTrigger: Int = 0,
    onAddCardClick: () -> Unit,
    onCardDetailClick: (Int) -> Unit,
    onCategorySetupClick: (Int) -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val allCards by viewModel.cards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val pullRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()

    val activeCards = allCards.filter { it.cardStatus == "ACTIVE" }
    val cancelledCards = allCards.filter { it.cardStatus == "CANCELLED" }

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Đang sử dụng", "Đã hủy")

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            lazyListState.animateScrollToItem(0)
            viewModel.fetchCards()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchCards()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thẻ của bạn", fontWeight = FontWeight.Bold) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(Color(0xFF157FEC), RoundedCornerShape(50))
                            .singleClick { onAddCardClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm thẻ", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            OptiCardsBottomNavigation(currentTab = 1, onTabSelected = onTabSelected)
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

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
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFF157FEC) else Color.Gray
                            )
                        }
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.fetchCards() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize().weight(1f),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = Color(0xFF157FEC)
                    )
                }
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTabIndex == 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0E4FF))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF157FEC))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = "Hãy hoàn tất chi tiêu trước khi kết thúc kỳ hoàn tiền 3-5 ngày để đảm bảo các giao dịch được ghi nhận đầy đủ", color = Color(0xFF157FEC), fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }

                        if (activeCards.isEmpty() && !isLoading) {
                            item {
                                Text(
                                    "Bạn chưa có thẻ nào đang hoạt động.",
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(activeCards) { card ->
                                UserCardItem(
                                    card = card,
                                    onClick = { onCardDetailClick(card.userCardId) },
                                    onCategorySetupClick = { onCategorySetupClick(card.userCardId) }
                                )
                            }
                        }
                    } else {
                        if (cancelledCards.isEmpty() && !isLoading) {
                            item {
                                Text(
                                    "Bạn không có thẻ nào đã hủy.",
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(cancelledCards) { card ->
                                CancelledCardItem(
                                    card = card,
                                    onClick = { onCardDetailClick(card.userCardId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCardItem(card: UserCardOverview, onClick: () -> Unit, onCategorySetupClick: () -> Unit) {
    val showDetailText = buildAnnotatedString {
        append("Xem chi tiết")
        withStyle(style = SpanStyle(baselineShift = BaselineShift(0.2f))) { append(" →") }
    }
    val configNowText = buildAnnotatedString {
        append("Thiết lập ngay")
        withStyle(style = SpanStyle(baselineShift = BaselineShift(0.2f))) { append(" →") }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().singleClick { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = card.cardImageUrl,
                    contentDescription = null,
                    modifier = Modifier.width(80.dp).height(50.dp).clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = card.cardName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = card.cyclePeriod, fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            card.conditions.forEach { condition ->
                val title = if (condition.conditionType == "SPEND") "Doanh số tối thiểu" else "Giao dịch tối thiểu"
                val valueStr = if (condition.conditionType == "SPEND") {
                    "%,dđ / %,dđ".format(condition.current, condition.target)
                } else {
                    "${condition.current} / ${condition.target}"
                }
                ProgressRow(title = title, valueStr = valueStr, progress = (condition.current.toFloat() / condition.target).coerceAtMost(1f), isSuccessColor = condition.isMet)
            }

            if (card.needsCategorySetup == true) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        .background(Color(0xFFFFF4E5), RoundedCornerShape(8.dp))
                        .singleClick { onCategorySetupClick() }
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Danh mục hoàn tiền chưa được thiết lập", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(configNowText, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (card.cashbackProgresses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                card.cashbackProgresses.forEach { progress ->
                    val ratio = if (progress.maxCap > 0) {
                        (progress.currentAmount.toFloat() / progress.maxCap).coerceAtMost(1f)
                    } else 0f

                    val dynamicUnit = when {
                        progress.ruleName.contains("xu", ignoreCase = true) -> "Xu"
                        progress.ruleName.contains("điểm", ignoreCase = true) -> "Điểm"
                        else -> "đ"
                    }

                    ProgressRow(
                        title = progress.ruleName,
                        valueStr = "${formatNumber(progress.currentAmount.toLong())} $dynamicUnit / ${formatNumber(progress.maxCap.toLong())} $dynamicUnit",
                        progress = ratio,
                        isSuccessColor = progress.currentAmount >= progress.maxCap,
                        color = Color(0xFF157FEC)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = showDetailText,
                color = Color(0xFF157FEC),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CancelledCardItem(card: UserCardOverview, onClick: () -> Unit) {
    val displayDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        card.closeDate?.let { parser.parse(it)?.let { date -> formatter.format(date) } } ?: "Không xác định"
    } catch (e: Exception) { "Không xác định" }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = card.cardImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(80.dp).height(50.dp).clip(RoundedCornerShape(8.dp)),
                alpha = 0.6f
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = card.cardName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC3545), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Đã hủy ngày $displayDate", fontSize = 12.sp, color = Color(0xFFDC3545))
                }
            }
        }
    }
}