package com.android.opticards.ui.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.data.model.TransactionHistoryItem
import com.android.opticards.ui.components.OptiCardsBottomNavigation
import com.android.opticards.ui.transaction.viewmodel.TransactionHistoryViewModel
import com.android.opticards.utils.formatNumber
import com.android.opticards.utils.singleClick

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel = viewModel(),
    refreshTrigger: Int = 0,
    onNavigateToTransactionEntry: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isNextPageLoading by viewModel.isNextPageLoading.collectAsState()
    val searchQuery by viewModel.searchMerchantQuery.collectAsState()
    val filterCards by viewModel.filterCards.collectAsState()

    val cardLabel by viewModel.selectedCardLabel.collectAsState()
    val dateLabel by viewModel.selectedDateLabel.collectAsState()
    val selectedUserCardId by viewModel.selectedCardId.collectAsState()

    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    var showCardFilterSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            listState.animateScrollToItem(0)
            viewModel.loadInitialData()
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsInfo = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsInfo > 0 && lastVisibleItem >= totalItemsInfo - 1
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) viewModel.loadNextPage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử giao dịch", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            OptiCardsBottomNavigation(currentTab = 2, onTabSelected = onTabSelected)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTransactionEntry,
                containerColor = Color(0xFF157FEC),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchMerchantChanged(it) },
                    placeholder = { Text("Tìm kiếm cửa hàng...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF157FEC)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedUserCardId != null,
                        onClick = { showCardFilterSheet = true },
                        label = { Text(cardLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = dateLabel != "Thời gian",
                        onClick = { showDatePicker = true },
                        label = { Text(dateLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading && groupedTransactions.isNotEmpty(),
                onRefresh = { viewModel.loadInitialData() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isLoading && groupedTransactions.isNotEmpty(),
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = Color(0xFF157FEC)
                    )
                }
            ) {
                if (isLoading && groupedTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF157FEC))
                    }
                } else if (groupedTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có giao dịch nào.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        groupedTransactions.forEach { group ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8F9FA))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = group.dateLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            items(group.items) { tx ->
                                TransactionItemRow(tx)
                                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                        if (isNextPageLoading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF157FEC), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCardFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCardFilterSheet = false },
            containerColor = Color.White
        ) {
            LazyColumn(modifier = Modifier.padding(bottom = 32.dp)) {
                item {
                    Text(
                        text = "Lọc theo thẻ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    ListItem(
                        headlineContent = { Text("Tất cả thẻ", fontWeight = FontWeight.Medium) },
                        modifier = Modifier.clickable {
                            viewModel.applyCardFilter(null, "Chọn thẻ")
                            showCardFilterSheet = false
                        }
                    )
                    HorizontalDivider()
                }
                items(filterCards) { card ->
                    ListItem(
                        headlineContent = { Text(card.cardName, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.clickable {
                            viewModel.applyCardFilter(card.userCardId, card.cardName)
                            showCardFilterSheet = false
                        },
                        leadingContent = {
                            AsyncImage(
                                model = card.cardImageUrl,
                                contentDescription = null,
                                modifier = Modifier.width(50.dp).height(32.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.applyDateFilter(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    showDatePicker = false
                }) { Text("Chọn", color = Color(0xFF157FEC), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.applyDateFilter(null, null)
                    showDatePicker = false
                }) { Text("Bỏ lọc", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                headline = { Text("Chọn khoảng thời gian", modifier = Modifier.padding(horizontal = 16.dp)) }
            )
        }
    }
}

@Composable
fun TransactionItemRow(tx: TransactionHistoryItem) {
    val isRefund = tx.status == "REFUND"
    val isGrayedOut = tx.hasBeenRefunded && !isRefund
    val methodLabel = when (tx.paymentChannel) {
        "ONLINE" -> "Online"
        "CONTACTLESS" -> "Chạm"
        "CHIP" -> "Quẹt thẻ"
        else -> tx.paymentChannel
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(if (isGrayedOut) 0.4f else 1.0f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = tx.merchantLogoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = tx.merchantName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${tx.cardName} • $methodLabel",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            val amountPrefix = if (isRefund) "+" else "-"
            val amountColor = if (isRefund) Color(0xFF28A745) else Color.Black
            Text(text = "$amountPrefix ${formatNumber(tx.amount.toLong())}đ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = amountColor)

            Spacer(modifier = Modifier.height(2.dp))
            val cbPrefix = if (isRefund) "-" else "+"
            val cbColor = if (isRefund) Color(0xFFDC3545) else if (tx.cashback > 0) Color(0xFF28A745) else Color.Gray
            Text(
                text = "$cbPrefix ${formatNumber(tx.cashback.toLong())} ${tx.cashbackUnit}",
                fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cbColor
            )
        }
    }
}