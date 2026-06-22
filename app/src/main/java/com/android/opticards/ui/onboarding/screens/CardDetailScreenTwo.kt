package com.android.opticards.ui.onboarding.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.components.CustomNumpad
import com.android.opticards.ui.onboarding.CardSpendInput
import com.android.opticards.ui.onboarding.viewmodel.OnboardingViewModel
import com.android.opticards.utils.CurrencyVisualTransformation
import com.android.opticards.utils.singleClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity

@Composable
fun CardDetailScreenTwo(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val spendInputs by viewModel.cardSpendInputs.collectAsState()
    val bankList by viewModel.bankList.collectAsState()
    val isSubmitSuccess by viewModel.isSubmitSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val cardsToTrack = remember(spendInputs, bankList) { viewModel.getCardsForSpendTracking() }
    val bankMap = remember(bankList) { bankList.associateBy { it.id } }

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showCustomNumpad by remember { mutableStateOf(false) }
    var activeCardId by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val isBottomBarVisible = !showCustomNumpad && !isImeVisible

    val isNextEnabled = spendInputs.values.any { input ->
        val m = input.openMonth.toIntOrNull() ?: 0
        val y = input.openYear.toIntOrNull() ?: 0
        val fullYear = 2000 + y

        val isMonthValid = m in 1..12
        val isYearValid = input.openYear.length == 2 && fullYear <= currentYear
        val isNotFuture = if (y == currentYear) m <= currentMonth else y < currentYear

        isMonthValid && isYearValid && isNotFuture
    }

    if (errorMessage != null) {
        CustomPopup(
            message = errorMessage!!,
            primaryButtonText = "Đóng",
            onPrimaryClick = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }

    LaunchedEffect(isSubmitSuccess) {
        if (isSubmitSuccess) {
            viewModel.resetSubmitStatus()
            onFinish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                        .statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color(0xFF157FEC),
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterStart)
                            .clickable { onBack() }
                    )
                    Text(
                        text = "Thông tin thẻ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            bottomBar = {
                if (isBottomBarVisible) {
                    Row(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearCardSpend()
                                viewModel.submitOnboarding()
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Bỏ qua", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.submitOnboarding() },
                            enabled = isNextEnabled,
                            modifier = Modifier.height(48.dp).width(140.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF157FEC),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFEBEBEB),
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            Text(text = "Hoàn tất", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
                    )
                    .imePadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            showCustomNumpad = false
                            activeCardId = null
                        })
                    }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = if (showCustomNumpad) 320.dp else 30.dp)
                ) {
                    item {
                        Text(
                            text = "Thông tin này giúp OptiCards tính toán điều kiện miễn/hoàn phí thường niên của thẻ (không bắt buộc)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }

                    items(cardsToTrack, key = { it.cardId }) { draft ->
                        val inputData = spendInputs[draft.cardId] ?: CardSpendInput()

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = bankMap[draft.bankId]?.logoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = draft.bankName, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(text = draft.cardName, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text(text = "Thời gian mở thẻ", fontSize = 12.sp, color = Color.DarkGray)

                                val m = inputData.openMonth.toIntOrNull() ?: 0
                                val y = inputData.openYear.toIntOrNull() ?: 0
                                val fullYear = 2000 + y

                                val isMonthError = inputData.openMonth.isNotEmpty() && (m !in 1..12)
                                val isYearError = inputData.openYear.length == 2 && fullYear > currentYear
                                val isFutureError = (inputData.openYear.length == 2 && inputData.openMonth.isNotEmpty() && !isMonthError && !isYearError) && (if (fullYear == currentYear) m > currentMonth else false)

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = inputData.openMonth,
                                        onValueChange = {
                                            if (it.length <= 2 && it.all { char -> char.isDigit()}) {
                                                viewModel.updateOpenDate(draft.cardId, it, inputData.openYear)
                                            }
                                        },
                                        placeholder = { Text("MM") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    showCustomNumpad = false
                                                    activeCardId = null
                                                }
                                            },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        isError = isMonthError || isFutureError
                                    )
                                    OutlinedTextField(
                                        value = inputData.openYear,
                                        onValueChange = {
                                            if (it.length <= 2 && it.all {char -> char.isDigit()}) {
                                                viewModel.updateOpenDate(draft.cardId, inputData.openMonth, it)
                                            }
                                        },
                                        placeholder = { Text("YY") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    showCustomNumpad = false
                                                    activeCardId = null
                                                }
                                            },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        isError = isYearError || isFutureError
                                    )
                                }

                                if (isFutureError) {
                                    Text(text = "Thời gian mở thẻ không thể ở tương lai!", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                                } else if (isMonthError) {
                                    Text(text = "Tháng không hợp lệ", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                                } else if (isYearError) {
                                    Text(text = "Năm không hợp lệ", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                                }

                                Text(text = "Chi tiêu trong năm ước tính (VNĐ)", fontSize = 12.sp, color = Color.DarkGray)

                                val isFocused = activeCardId == draft.cardId && showCustomNumpad
                                OutlinedTextField(
                                    value = inputData.estimatedSpend,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("VD: 50.000.000") },
                                    visualTransformation = CurrencyVisualTransformation(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .singleClick {
                                            focusManager.clearFocus()
                                            softwareKeyboardController?.hide()
                                            activeCardId = draft.cardId
                                            showCustomNumpad = true
                                            val index = cardsToTrack.indexOf(draft)
                                            if (index != -1) {
                                                coroutineScope.launch {
                                                    delay(150)
                                                    listState.animateScrollToItem(index + 1)
                                                }
                                            }
                                        },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = Color.Black,
                                        disabledBorderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        disabledPlaceholderColor = Color.LightGray,
                                        disabledContainerColor = Color.White
                                    ),
                                    enabled = false
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showCustomNumpad,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            CustomNumpad(
                onKeyPress = { key ->
                    activeCardId?.let { cardId ->
                        val currentVal = spendInputs[cardId]?.estimatedSpend ?: ""
                        if (currentVal.isEmpty() && (key == "0" || key == "000")) { }
                        else { viewModel.updateEstimatedSpend(cardId, currentVal + key) }
                    }
                },
                onBackspace = {
                    activeCardId?.let { cardId ->
                        val currentVal = spendInputs[cardId]?.estimatedSpend ?: ""
                        if (currentVal.isNotEmpty()) viewModel.updateEstimatedSpend(cardId, currentVal.dropLast(1))
                    }
                },
                onDone = {
                    showCustomNumpad = false
                    activeCardId = null
                }
            )
        }
    }
}