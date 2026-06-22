package com.android.opticards.ui.transaction.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.ui.components.CustomNumpad
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.transaction.viewmodel.TransactionEntryViewModel
import com.android.opticards.utils.CurrencyVisualTransformation
import com.android.opticards.utils.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    merchantId: Int,
    mccCodeParam: String,
    initialAmount: Int,
    initialChannel: String,
    viewModel: TransactionEntryViewModel = viewModel(),
    onNavigateBack: (isSuccess: Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val merchantName by viewModel.merchantName.collectAsState()
    val mccCode by viewModel.mccCode.collectAsState()
    val amountStr by viewModel.amountStr.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val availablePaymentChannels by viewModel.availablePaymentChannels.collectAsState()
    val availableMccsForMerchant by viewModel.availableMccsForMerchant.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()

    val ownedCards by viewModel.ownedCards.collectAsState()
    val allMerchants by viewModel.allMerchants.collectAsState()
    val allMccs by viewModel.allMccs.collectAsState()
    val surcharge by viewModel.calculatedSurcharge.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showNumpad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.initData(merchantId, mccCodeParam, initialAmount, initialChannel) }

    if (submitSuccess) {
        CustomPopup(
            title = "Thành công",
            message = "Giao dịch của bạn đã được ghi nhận",
            icon = Icons.Filled.CheckCircle,
            iconTint = Color(0xFF28A745),
            primaryButtonText = "Đồng ý",
            onPrimaryClick = {
                viewModel.resetSuccess()
                onNavigateBack(true)
            },
            onDismissRequest = {
                viewModel.resetSuccess()
                onNavigateBack(true)
            }
        )
    }

    if (errorMessage != null) {
        CustomPopup(
            title = "Đã xảy ra lỗi",
            message = errorMessage!!,
            primaryButtonText = "Đóng",
            onPrimaryClick = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis() + 86400000L
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghi lại giao dịch", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
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
                    CustomNumpad(
                        onKeyPress = { key ->
                            val newVal = amountStr + key
                            if (newVal.length <= 11) {
                                viewModel.onAmountChanged(newVal)
                            }
                        },
                        onBackspace = {
                            if (amountStr.isNotEmpty()) {
                                viewModel.onAmountChanged(amountStr.dropLast(1))
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
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = { onNavigateBack(false) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                            Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.submitTransaction() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isSubmitting && amountStr.isNotEmpty() && selectedCard != null && merchantName.isNotEmpty() && mccCode.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC))
                        ) {
                            if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Thêm GD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF157FEC))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus(); showNumpad = false }) }
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var mExpanded by remember { mutableStateOf(false) }
                var mSearchText by remember { mutableStateOf("") }

                LaunchedEffect(merchantName) {
                    if (merchantName.isNotEmpty() && mSearchText.isEmpty()) {
                        mSearchText = merchantName
                    }
                }

                ExposedDropdownMenuBox(expanded = mExpanded, onExpandedChange = { mExpanded = !mExpanded; if(mExpanded) showNumpad = false }) {
                    OutlinedTextField(
                        value = mSearchText,
                        onValueChange = {
                            mSearchText = it
                            viewModel.onMerchantNameChanged(it)
                            mExpanded = it.isNotEmpty()
                        },
                        label = { Text("Cửa hàng / Dịch vụ *") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        trailingIcon = {
                            if (mSearchText.isNotEmpty()) {
                                IconButton(onClick = {
                                    mSearchText = ""
                                    viewModel.onMerchantNameChanged("")
                                    mExpanded = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Xóa nội dung")
                                }
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = mExpanded)
                            }
                        }
                    )
                    if (mSearchText.isNotEmpty() && mExpanded) {
                        ExposedDropdownMenu(expanded = mExpanded, onDismissRequest = { mExpanded = false }) {
                            allMerchants.filter { it.name.contains(mSearchText, true) }.take(5).forEach { merch ->
                                DropdownMenuItem(
                                    text = { Text(merch.name) },
                                    onClick = {
                                        mSearchText = merch.name
                                        viewModel.onMerchantSelected(merch)
                                        mExpanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }

                var mccExpanded by remember { mutableStateOf(false) }
                val currentMccDesc = allMccs.find { it.mccCode == mccCode }?.description ?: ""
                val mccDisplay = if (mccCode.isNotEmpty()) "$mccCode - $currentMccDesc" else ""

                ExposedDropdownMenuBox(expanded = mccExpanded, onExpandedChange = { mccExpanded = !mccExpanded; focusManager.clearFocus(); if(mccExpanded) showNumpad = false }) {
                    OutlinedTextField(
                        value = mccDisplay,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Mã ngành (MCC) *") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mccExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    if (availableMccsForMerchant.isNotEmpty() && mccExpanded) {
                        ExposedDropdownMenu(expanded = mccExpanded, onDismissRequest = { mccExpanded = false }) {
                            availableMccsForMerchant.forEach { mccVal ->
                                val desc = allMccs.find { it.mccCode == mccVal }?.description ?: "Không xác định"
                                DropdownMenuItem(
                                    text = { Text("$mccVal - $desc") },
                                    onClick = {
                                        viewModel.onMccSelected(mccVal)
                                        mccExpanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.clickable { showNumpad = true; focusManager.clearFocus() }) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = {},
                        label = { Text("Số tiền thanh toán *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = CurrencyVisualTransformation(),
                        suffix = { Text("đ") },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = if (showNumpad) Color(0xFF157FEC) else Color.Gray,
                            disabledLabelColor = Color.DarkGray,
                            disabledSuffixColor = Color.Black
                        )
                    )
                }

                Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                val options = mutableListOf<Pair<String, String>>()
                if (availablePaymentChannels.contains("ONLINE")) options.add("Online" to "ONLINE")
                if (availablePaymentChannels.contains("CONTACTLESS")) options.add("Chạm" to "CONTACTLESS")
                if (availablePaymentChannels.contains("CHIP")) options.add("Cắm/Quẹt" to "CHIP")

                if (options.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEBEBEB)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { (text, methodValue) ->
                            val isSelected = paymentMethod == methodValue
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp).clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { viewModel.onPaymentMethodChanged(methodValue); showNumpad = false; focusManager.clearFocus() },
                                contentAlignment = Alignment.Center
                            ) { Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color(0xFF157FEC) else Color.Gray) }
                        }
                    }
                }

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true; focusManager.clearFocus(); showNumpad = false }) {
                    OutlinedTextField(
                        value = sdf.format(Date(selectedDate)), onValueChange = {}, readOnly = true,
                        label = { Text("Ngày giao dịch") }, trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.DarkGray)
                    )
                }

                var cardExpanded by remember { mutableStateOf(false) }

                val displayRate = selectedCard?.let {
                    val r = if (it.cashbackRate % 1f == 0f) it.cashbackRate.toInt().toString() else it.cashbackRate.toString()
                    "${it.cardName} (${if (it.cashbackRate > 0) "Hoàn $r%" else "0%"})"
                } ?: if (ownedCards.isEmpty()) "Không có thẻ" else "Đang tải thẻ..."

                ExposedDropdownMenuBox(expanded = cardExpanded, onExpandedChange = { cardExpanded = !cardExpanded; focusManager.clearFocus(); if(cardExpanded) showNumpad = false }) {
                    OutlinedTextField(
                        value = displayRate,
                        onValueChange = { }, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Thẻ sử dụng") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardExpanded) },
                        shape = RoundedCornerShape(12.dp), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = cardExpanded, onDismissRequest = { cardExpanded = false }) {
                        ownedCards.forEach { card ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(card.cardName, fontWeight = FontWeight.Medium)
                                        if (card.cashbackRate > 0) {
                                            val r = if (card.cashbackRate % 1f == 0f) card.cashbackRate.toInt().toString() else card.cashbackRate.toString()
                                            Text("Hoàn $r%", color = Color(0xFF28A745), fontSize = 12.sp)
                                        } else {
                                            Text("Không hoàn tiền", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                },
                                onClick = { viewModel.onCardSelected(card); cardExpanded = false },
                                leadingIcon = { AsyncImage(model = card.imageUrl, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp))) }
                            )
                        }
                    }
                }

                val baseAmount = amountStr.toIntOrNull() ?: 0
                val totalPaymentAmount = baseAmount + surcharge

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Phí dịch vụ", color = Color.DarkGray)
                            Text(
                                text = if (amountStr.isEmpty()) "0đ" else if (surcharge > 0) "+${formatNumber(surcharge.toLong())}đ" else "Miễn phí",
                                fontWeight = FontWeight.Medium,
                                color = if (surcharge > 0 && amountStr.isNotEmpty()) Color(0xFFE65100) else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng thanh toán", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text(text = "${formatNumber(totalPaymentAmount.toLong())}đ", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFD0E3FC), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        val cashbackUnit = selectedCard?.cashbackUnit ?: "đ"

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tiền hoàn dự kiến", color = Color.DarkGray)
                            Text(
                                text = if (amountStr.isEmpty()) "0$cashbackUnit" else "+${formatNumber(selectedCard?.expectedAmount?.toLong() ?: 0)}$cashbackUnit",
                                fontWeight = FontWeight.Bold,
                                color = if (amountStr.isEmpty() || (selectedCard?.expectedAmount ?: 0) == 0) Color.Black else Color(0xFF28A745),
                                fontSize = 16.sp
                            )
                        }

                        if (selectedCard?.note?.isNotEmpty() == true && amountStr.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(selectedCard!!.note!!, fontSize = 12.sp, color = Color(0xFF157FEC))
                        }
                    }
                }
            }
        }
    }
}