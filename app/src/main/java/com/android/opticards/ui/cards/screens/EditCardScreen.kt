package com.android.opticards.ui.cards.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.ui.cards.viewmodel.EditCardViewModel
import com.android.opticards.ui.cards.viewmodel.SubmitState
import com.android.opticards.ui.components.CustomNumpad
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.utils.CurrencyVisualTransformation
import com.android.opticards.utils.singleClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    userCardId: Int,
    viewModel: EditCardViewModel = viewModel(),
    bankName: String,
    cardName: String,
    logoUrl: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val formState by viewModel.formState.collectAsState()
    val openMonth = formState.openMonth
    val openYear = formState.openYear
    val estimatedSpend = formState.estimatedSpend
    val hasSpendBasedWaiver = formState.hasSpendBasedWaiver

    val submitState by viewModel.submitState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()

    var showCustomNumpad by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val isBottomBarVisible = !showCustomNumpad && !isImeVisible

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val m = openMonth.toIntOrNull() ?: 0
    val y = openYear.toIntOrNull() ?: 0
    val fullYear = 2000 + y

    val isMonthError = hasSpendBasedWaiver && openMonth.isNotEmpty() && (m !in 1..12)
    val isYearError = hasSpendBasedWaiver && openYear.length == 2 && fullYear > currentYear
    val isFutureError = hasSpendBasedWaiver && (openYear.length == 2 && openMonth.isNotEmpty() && !isMonthError && !isYearError) && (if (fullYear == currentYear) m > currentMonth else false)
    val isTimeComplete = (!hasSpendBasedWaiver) || (openMonth.isEmpty() && openYear.isEmpty()) || (openMonth.isNotEmpty() && openYear.length == 2)
    val isTimeProvidedIfSpendEntered = !hasSpendBasedWaiver || estimatedSpend.isEmpty() || (openMonth.isNotEmpty() && openYear.length == 2)
    val isSaveEnabled = hasChanges &&
            isTimeComplete &&
            isTimeProvidedIfSpendEntered &&
            !isMonthError &&
            !isYearError &&
            !isFutureError &&
            submitState != SubmitState.LOADING

    LaunchedEffect(submitState) {
        if (submitState == SubmitState.SUCCESS) {
            viewModel.resetSubmitState()
            onSave()
        }
    }

    if (errorMessage != null) {
        CustomPopup(
            message = errorMessage!!,
            primaryButtonText = "Đóng",
            onPrimaryClick = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chỉnh sửa thẻ", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                if (isBottomBarVisible) {
                    Row(
                        modifier = Modifier.background(Color.White).padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Hủy", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.saveCardDetails(userCardId) },
                            enabled = isSaveEnabled,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF157FEC),
                                disabledContainerColor = Color(0xFFEBEBEB)
                            )
                        ) {
                            if (submitState == SubmitState.LOADING) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Lưu", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8F9FA)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
                    )
                    .imePadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            showCustomNumpad = false
                        })
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = if (showCustomNumpad) 320.dp else 0.dp)
                ) {
                    // --- KHỐI 1: NHẬN DIỆN THẺ (READ-ONLY) ---
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.width(64.dp).height(40.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = bankName, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = cardName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = hasSpendBasedWaiver,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Text(text = "Thời gian mở thẻ", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = openMonth,
                                    onValueChange = { viewModel.updateOpenMonth(it) },
                                    placeholder = { Text("MM") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f).onFocusChanged {
                                        if (it.isFocused) showCustomNumpad = false
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    isError = isMonthError || isFutureError
                                )
                                OutlinedTextField(
                                    value = openYear,
                                    onValueChange = { viewModel.updateOpenYear(it) },
                                    placeholder = { Text("YY") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f).onFocusChanged {
                                        if (it.isFocused) showCustomNumpad = false
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    isError = isYearError || isFutureError
                                )
                            }

                            Text(text = "Chi tiêu trong năm ước tính (VNĐ)", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = estimatedSpend,
                                onValueChange = { },
                                readOnly = true,
                                placeholder = { Text("VD: 50.000.000") },
                                visualTransformation = CurrencyVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp).singleClick {
                                    focusManager.clearFocus()
                                    softwareKeyboardController?.hide()
                                    showCustomNumpad = true
                                    coroutineScope.launch {
                                        delay(150)
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = if (showCustomNumpad) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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

        AnimatedVisibility(
            visible = showCustomNumpad && hasSpendBasedWaiver,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            CustomNumpad(
                onKeyPress = { key ->
                    if (estimatedSpend.isEmpty() && (key == "0" || key == "000")) return@CustomNumpad
                    viewModel.updateEstimatedSpend(estimatedSpend + key)
                },
                onBackspace = {
                    if (estimatedSpend.isNotEmpty()) viewModel.updateEstimatedSpend(estimatedSpend.dropLast(1))
                },
                onDone = { showCustomNumpad = false }
            )
        }
    }
}