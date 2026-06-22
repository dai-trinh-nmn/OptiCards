package com.android.opticards.ui.cards.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.opticards.data.model.RecentTransactionItem
import com.android.opticards.data.model.TransactionHistoryItem
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.ui.cards.viewmodel.CardDetailViewModel
import com.android.opticards.ui.cards.viewmodel.SubmitState
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.components.ProgressRow
import com.android.opticards.utils.formatNumber
import com.android.opticards.utils.getCurrencyUnit
import com.android.opticards.utils.singleClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    userCardId: Int,
    viewModel: CardDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onEditTokenClick: (String, String, String) -> Unit,
    onChangeCategoryClick: () -> Unit,
) {
    val card by viewModel.cardDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAllTransactionsSheet by remember { mutableStateOf(false) }
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val isTransactionsLoading by viewModel.isTransactionsLoading.collectAsState()

    val uriHandler = LocalUriHandler.current
    var showCancelPopup by remember { mutableStateOf(false) }

    LaunchedEffect(userCardId) { viewModel.loadCardDetail(userCardId) }

    LaunchedEffect(cancelState) {
        if (cancelState == SubmitState.SUCCESS) {
            showCancelPopup = false
            viewModel.resetCancelState()
            onNavigateBack()
        }
    }

    if (errorMessage != null) {
        CustomPopup(message = errorMessage!!, primaryButtonText = "Đóng", onPrimaryClick = { viewModel.clearError() }, onDismissRequest = { viewModel.clearError() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.cardName ?: "Chi tiết thẻ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF157FEC))
            }
        } else card?.let { activeCard ->
            val unit = getCurrencyUnit(activeCard.rewardCurrency)
            val isCancelled = activeCard.cardStatus == "CANCELLED"

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.586f), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = activeCard.cardImageUrl, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            alpha = if (isCancelled) 0.5f else 1.0f,
                            colorFilter = if (isCancelled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
                        )
                        if (isCancelled) {
                            Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("THẺ ĐÃ HỦY", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            }
                        }
                    }
                }

                if (!isCancelled) {
                    activeCard.specialWarning?.let { warning ->
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)), shape = RoundedCornerShape(12.dp)) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = warning, color = Color(0xFFE65100), fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ActionButton(icon = Icons.Default.Edit, label = "Chỉnh sửa\nthông tin", onClick = { onEditTokenClick(activeCard.bankName, activeCard.cardName, activeCard.cardImageUrl) })
                            if (activeCard.hasFlexCategories) { ActionButton(icon = Icons.Default.Category, label = "Danh mục", onClick = onChangeCategoryClick) }
                            ActionButton(icon = Icons.Default.Block, label = "Hủy thẻ", color = Color(0xFFDC3545), onClick = { showCancelPopup = true })
                        }
                    }

                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = activeCard.cyclePeriod, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(14.dp))

                                val feeColor = if (activeCard.annualFeeStatus == "WAIVED") Color(0xFF28A745) else Color.DarkGray
                                ProgressRow(title = "Miễn/Hoàn phí thường niên", valueStr = activeCard.annualFeeNotice, progress = activeCard.annualFeeProgress, isSuccessColor = activeCard.annualFeeStatus == "WAIVED", color = feeColor)

                                val cashbackRatio = if (activeCard.totalCashbackMax > 0) (activeCard.totalCashbackCurrent.toFloat() / activeCard.totalCashbackMax).coerceAtMost(1f) else 0f
                                ProgressRow(title = "Tổng hạn mức hoàn kỳ này", valueStr = "${formatNumber(activeCard.totalCashbackCurrent)}$unit / ${formatNumber(activeCard.totalCashbackMax)}$unit", progress = cashbackRatio, isSuccessColor = activeCard.totalCashbackCurrent >= activeCard.totalCashbackMax, color = Color(0xFF157FEC))

                                if (activeCard.cashbackBreakdowns.isNotEmpty()) {
                                    Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp)).padding(12.dp)) {
                                        activeCard.cashbackBreakdowns.forEach { sub ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "• ${sub.categoryName}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                                Text(text = "${formatNumber(sub.currentAmount)}$unit / ${formatNumber(sub.maxCap)}$unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                activeCard.conditions.forEach { condition ->
                                    val title = if (condition.conditionType == "SPEND") "Doanh số tối thiểu kỳ" else "Giao dịch tối thiểu kỳ"
                                    val valueStr = if (condition.conditionType == "SPEND") "%,dđ / %,dđ".format(condition.current, condition.target) else "${condition.current} / ${condition.target}"
                                    val progressRatio = if (condition.target > 0) (condition.current.toFloat() / condition.target).coerceAtMost(1f) else 0f
                                    ProgressRow(title = title, valueStr = valueStr, progress = progressRatio, isSuccessColor = condition.isMet)
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                            Text(text = "Thể lệ hoàn tiền tóm tắt", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = activeCard.cashbackRuleSummary, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                            activeCard.cashbackTncUrl?.let { url ->
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                                TextButton(onClick = { try { uriHandler.openUri(url) } catch (e: Exception) {} }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentPadding = PaddingValues(0.dp)) {
                                    Text("Thể lệ chi tiết", color = Color(0xFF157FEC), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Giao dịch gần đây", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Xem tất cả",
                                color = Color(0xFF157FEC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    viewModel.loadAllCardTransactions(userCardId)
                                    showAllTransactionsSheet = true
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeCard.recentTransactions.isEmpty()) {
                            Text(text = if (isCancelled) "Không có giao dịch nào được lưu trữ" else "Chưa phát sinh giao dịch trong kỳ", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                        } else {
                            activeCard.recentTransactions.forEach { tx ->
                                TransactionRow(tx)
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                            }
                        }
                    }
                }
            }

            if (showCancelPopup) {
                CustomPopup(
                    message = "Chỉ thực hiện thao tác này khi bạn đã thực sự hủy thẻ, thao tác này không thể hoàn tác!",
                    primaryButtonText = if (cancelState == SubmitState.LOADING) "Đang xử lý..." else "Xác nhận",
                    onPrimaryClick = { if (cancelState != SubmitState.LOADING) viewModel.cancelCard(userCardId) },
                    secondaryButtonText = "Đóng",
                    onSecondaryClick = { showCancelPopup = false },
                    onDismissRequest = { showCancelPopup = false }
                )
            }

            if (showAllTransactionsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAllTransactionsSheet = false },
                    containerColor = Color.White,
                ) {
                    Column(modifier = Modifier.fillMaxHeight(0.92f).padding(horizontal = 16.dp)) {
                        Text("Tất cả lịch sử thẻ", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

                        if (isTransactionsLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF157FEC))
                            }
                        } else if (groupedTransactions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Không có lịch sử giao dịch nào.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                groupedTransactions.forEach { cycleGroup ->
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F6FE), RoundedCornerShape(8.dp)).padding(12.dp)
                                        ) {
                                            Text(cycleGroup.cycleLabel, fontWeight = FontWeight.Bold, color = Color(0xFF157FEC))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    cycleGroup.dayGroups.forEach { dayGroup ->
                                        item {
                                            Text(
                                                text = dayGroup.dateLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.DarkGray,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        items(dayGroup.items) { tx ->
                                            TransactionRowFull(
                                                tx = tx,
                                                onClick = {
                                                    showAllTransactionsSheet = false
                                                }
                                            )
                                            HorizontalDivider(color = Color(0xFFF3F4F6))
                                        }
                                    }
                                    item { Spacer(modifier = Modifier.height(16.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, color: Color = Color(0xFF157FEC), onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(75.dp).clickable { onClick() }) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.DarkGray, maxLines = 2, lineHeight = 14.sp)
    }
}

@Composable
fun TransactionRow(tx: RecentTransactionItem) {
    val isRefund = tx.status == "REFUND"
    val isGrayedOut = tx.hasBeenRefunded && !isRefund
    val backendFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = try {
        val date = backendFormat.parse(tx.transactionDate)
        if (date != null) displayFormat.format(date) else tx.transactionDate
    } catch (e: Exception) { tx.transactionDate }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .alpha(if (isGrayedOut) 0.4f else 1.0f),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = tx.merchantName ?: "Cửa hàng bán lẻ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formattedDate, fontSize = 12.sp, color = Color.Gray)
        }
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

@Composable
fun TransactionRowFull(tx: TransactionHistoryItem, onClick: () -> Unit) {
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
            .singleClick { onClick() }
            .padding(vertical = 12.dp)
            .alpha(if (isGrayedOut) 0.4f else 1.0f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = tx.merchantLogoUrl,
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFF3F4F6)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tx.merchantName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "${tx.cardName} • $methodLabel", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
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