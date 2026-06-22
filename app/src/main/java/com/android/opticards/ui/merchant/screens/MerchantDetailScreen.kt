package com.android.opticards.ui.merchant.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.data.model.CardSuggestionItem
import com.android.opticards.ui.components.CustomNumpad
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.merchant.viewmodel.MerchantDetailViewModel
import com.android.opticards.utils.CurrencyVisualTransformation
import com.android.opticards.utils.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchantId: Int,
    viewModel: MerchantDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToContribution: (String) -> Unit = {},
    onNavigateToTransactionEntry: (Int, String, Int, String) -> Unit = { _, _, _, _ -> }
) {
    val merchant by viewModel.merchant.collectAsState()
    val selectedMcc by viewModel.selectedMcc.collectAsState()
    val inputAmount by viewModel.inputAmount.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedSubServiceId by remember { mutableIntStateOf(-1) }
    var showNumpad by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(merchantId) {
        viewModel.loadMerchantDetail(merchantId)
    }

    if (errorMessage != null) {
        CustomPopup(message = errorMessage!!, primaryButtonText = "Đóng", onPrimaryClick = { viewModel.clearError() }, onDismissRequest = { viewModel.clearError() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(merchant?.name ?: "Chi tiết cửa hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    merchant?.let { info ->
                        IconButton(onClick = { viewModel.toggleFavorite(merchantId) }) {
                            Icon(
                                imageVector = if (info.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Yêu thích",
                                tint = if (info.isFavorited) Color.Red else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(Color.White)) {
                AnimatedVisibility(
                    visible = showNumpad,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    val currentMccGroup = merchant?.mccGroups?.find { it.mccCode == selectedMcc }
                    val currentSubService = currentMccGroup?.subServices?.find { it.serviceId == selectedSubServiceId }

                    CustomNumpad(
                        onKeyPress = { key ->
                            val newVal = inputAmount + key
                            if (newVal.length <= 11) {
                                viewModel.onParametersChanged(merchantId, selectedMcc, newVal, currentSubService?.paymentChannel ?: "ANY", currentSubService?.cardBrand)
                            }
                        },
                        onBackspace = {
                            if (inputAmount.isNotEmpty()) {
                                val newVal = inputAmount.dropLast(1)
                                viewModel.onParametersChanged(merchantId, selectedMcc, newVal, currentSubService?.paymentChannel ?: "ANY", currentSubService?.cardBrand)
                            }
                        },
                        onDone = { showNumpad = false },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }

                AnimatedVisibility(
                    visible = !showNumpad,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                merchant?.name?.let { name ->
                                    val encodedName = android.net.Uri.encode(name)
                                    onNavigateToContribution(encodedName)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF157FEC)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF157FEC))
                        ) {
                            Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Báo lỗi", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val amt = inputAmount.toIntOrNull() ?: 0
                                val currentMccGroup = merchant?.mccGroups?.find { it.mccCode == selectedMcc }
                                val currentSubService = currentMccGroup?.subServices?.find { it.serviceId == selectedSubServiceId }
                                val paymentChannel = currentSubService?.paymentChannel ?: "ANY"

                                onNavigateToTransactionEntry(merchantId, selectedMcc, amt, paymentChannel)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ghi chép GD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        if (isLoadingDetail) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF157FEC))
            }
        } else merchant?.let { info ->

            val currentMccGroup = info.mccGroups.find { it.mccCode == selectedMcc }

            LaunchedEffect(selectedMcc) {
                currentMccGroup?.subServices?.firstOrNull()?.let {
                    selectedSubServiceId = it.serviceId
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showNumpad = false })
                    },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = info.logoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = info.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier.background(Color(0xFFE8F4FF), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(text = info.category, color = Color(0xFF157FEC), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Mã danh mục (MCC)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(info.mccGroups) { group ->
                                    FilterChip(
                                        selected = selectedMcc == group.mccCode,
                                        onClick = {
                                            val firstSub = group.subServices.firstOrNull()
                                            viewModel.onParametersChanged(merchantId, group.mccCode, inputAmount, firstSub?.paymentChannel ?: "ANY", firstSub?.cardBrand)
                                            showNumpad = false
                                        },
                                        label = { Text("${group.mccCode} - ${group.mccDescription}") },
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }

                            currentMccGroup?.let { group ->
                                if (group.subServices.size > 1) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(text = "Chọn phương thức/dịch vụ cụ thể:", fontSize = 13.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    group.subServices.forEach { sub ->
                                        val isSelected = selectedSubServiceId == sub.serviceId
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clickable {
                                                    selectedSubServiceId = sub.serviceId
                                                    viewModel.onParametersChanged(merchantId, selectedMcc, inputAmount, sub.paymentChannel, sub.cardBrand)
                                                    showNumpad = false
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF157FEC) else Color(0xFFEBEBEB)),
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0F6FE) else Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = {
                                                            selectedSubServiceId = sub.serviceId
                                                            viewModel.onParametersChanged(merchantId, selectedMcc, inputAmount, sub.paymentChannel, sub.cardBrand)
                                                            showNumpad = false
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(text = sub.serviceName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                }

                                                if (sub.surchargeFixed > 0 || sub.surchargePercent > 0f || !sub.note.isNullOrEmpty()) {
                                                    Column(modifier = Modifier.padding(start = 32.dp, top = 6.dp)) {
                                                        if (sub.surchargeFixed > 0 || sub.surchargePercent > 0f) {
                                                            val pctStr = if (sub.surchargePercent > 0f) "+${sub.surchargePercent}%" else ""
                                                            val fixStr = if (sub.surchargeFixed > 0) "+${formatNumber(sub.surchargeFixed.toLong())}đ" else ""
                                                            Text(text = "⚠️ Phí dịch vụ: $fixStr $pctStr", color = Color(0xFFE65100), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                                        }
                                                        sub.note?.let {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(text = "Chú ý: $it", color = Color.Gray, fontSize = 12.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (group.subServices.size == 1) {
                                    val sub = group.subServices.first()
                                    if (sub.surchargeFixed > 0 || sub.surchargePercent > 0f || !sub.note.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFFF4E5), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            if (sub.surchargeFixed > 0 || sub.surchargePercent > 0f) {
                                                val pctStr = if (sub.surchargePercent > 0f) "+${sub.surchargePercent}%" else ""
                                                val fixStr = if (sub.surchargeFixed > 0) "+${formatNumber(sub.surchargeFixed.toLong())}đ" else ""
                                                Text(text = "⚠️ Phí dịch vụ: $fixStr $pctStr", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            sub.note?.let {
                                                if (sub.surchargeFixed > 0 || sub.surchargePercent > 0f) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Text(text = "Chú ý: $it", color = Color(0xFFE65100), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Thẻ tối ưu cho ${info.name}/MCC $selectedMcc", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(modifier = Modifier.clickable { showNumpad = true }) {
                                OutlinedTextField(
                                    value = inputAmount,
                                    onValueChange = { },
                                    placeholder = { Text("Nhập số tiền thanh toán dự kiến...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    suffix = { Text("đ") },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = Color.Black,
                                        disabledBorderColor = if (showNumpad) Color(0xFF157FEC) else Color(0xFFEBEBEB),
                                        disabledPlaceholderColor = Color.Gray,
                                        disabledTrailingIconColor = Color.Black
                                    ),
                                    visualTransformation = CurrencyVisualTransformation()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoadingSuggestions) {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF157FEC), modifier = Modifier.size(28.dp))
                                }
                            } else {
                                val ownedCards = suggestions?.ownedCards ?: emptyList()
                                if (ownedCards.isEmpty()) {
                                    Text(
                                        text = "Không có thẻ nào phù hợp",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    )
                                } else {
                                    ownedCards.forEach { card ->
                                        OwnedCardSuggestionRow(card = card, hasAmount = inputAmount.isNotEmpty())
                                        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                val discoverCards = suggestions?.discoverCards ?: emptyList()
                if (discoverCards.isNotEmpty() && !isLoadingSuggestions) {
                    item {
                        Text(text = "Khám phá các dòng thẻ khác", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(discoverCards) { card ->
                                DiscoverCardItem(card = card)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnedCardSuggestionRow(card: CardSuggestionItem, hasAmount: Boolean) {
    val ratePct = if (card.cashbackRate % 1f == 0f) card.cashbackRate.toInt() else card.cashbackRate

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).alpha(if (card.isMaxedOut) 0.5f else 1.0f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = card.imageUrl,
            contentDescription = null,
            modifier = Modifier.width(64.dp).height(40.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = card.cardName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (!card.note.isNullOrEmpty()) {
                Text(text = card.note!!, fontSize = 11.sp, color = Color(0xFF157FEC))
            }
            if (card.isMaxedOut) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC3545), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Đã đạt hạn mức hoàn tối đa kì này", fontSize = 11.sp, color = Color(0xFFDC3545), fontWeight = FontWeight.Medium)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "$ratePct%", fontWeight = FontWeight.Bold, color = Color(0xFF28A745), fontSize = 16.sp)
            if (hasAmount && !card.isMaxedOut) {
                Text(text = "+${formatNumber(card.expectedAmount.toLong())}${card.cashbackUnit}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DiscoverCardItem(card: CardSuggestionItem) {
    val ratePct = if (card.cashbackRate % 1f == 0f) card.cashbackRate.toInt() else card.cashbackRate

    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = card.cardName, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Hoàn $ratePct%", color = Color(0xFF28A745), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}