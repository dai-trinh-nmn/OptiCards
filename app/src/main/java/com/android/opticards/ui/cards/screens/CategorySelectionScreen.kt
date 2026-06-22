package com.android.opticards.ui.cards.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.data.model.CategoryOption
import com.android.opticards.ui.cards.viewmodel.CategorySelectionViewModel
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.utils.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    userCardId: Int,
    viewModel: CategorySelectionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val info by viewModel.info.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showConfirmImmediatePopup by remember { mutableStateOf(false) }

    LaunchedEffect(userCardId) {
        viewModel.fetchCategoryInfo(userCardId)
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSuccessState()
            onNavigateBack()
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

    if (showConfirmImmediatePopup) {
        CustomPopup(
            title = "Xác nhận đổi danh mục",
            message = "Các giao dịch chưa được bút toán sau khi đổi danh mục sẽ được xét hoàn tiền theo danh mục mới. Bạn có đồng ý tiếp tục?",
            primaryButtonText = "Xác nhận",
            onPrimaryClick = {
                showConfirmImmediatePopup = false
                viewModel.saveCategories(userCardId)
            },
            secondaryButtonText = "Đóng",
            onSecondaryClick = { showConfirmImmediatePopup = false },
            onDismissRequest = { showConfirmImmediatePopup = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn danh mục hoàn tiền", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (!isLoading && info != null) {
                val hasChanges = selectedIds != (info?.currentSelectedIds?.toSet() ?: emptySet<Int>())
                val isNextEnabled = hasChanges && !isSaving && (info?.remainingChanges ?: 0) > 0

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(24.dp)) {
                    Button(
                        onClick = {
                            val isFirstTimeSetup = info?.currentSelectedIds?.isEmpty() == true
                            if (info?.effectTiming == "IMMEDIATE" && !isFirstTimeSetup) {
                                showConfirmImmediatePopup = true
                            } else {
                                viewModel.saveCategories(userCardId)
                            }
                        },
                        enabled = isNextEnabled,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF157FEC),
                            disabledContainerColor = Color(0xFFEBEBEB)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Lưu thiết lập", fontWeight = FontWeight.Bold)
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
        } else info?.let { flexInfo ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    val remaining = flexInfo.remainingChanges

                    val effectText = when (flexInfo.effectTiming) {
                        "NEXT_STATEMENT" -> "sẽ được áp dụng từ kỳ tiếp theo!"
                        "IMMEDIATE" -> "sẽ được áp dụng ngay lập tức!"
                        else -> "sẽ được áp dụng theo quy định!"
                    }
                    val noticeMessage = if (remaining == 0) {
                        "Đã hết lượt thay đổi danh mục trong kỳ.\nLưu ý: Thiết lập mới $effectText"
                    } else {
                        "Lưu ý: Thiết lập mới $effectText"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = noticeMessage,
                                color = Color(0xFFE65100), fontSize = 13.sp, lineHeight = 18.sp
                            )
                        }
                    }

                    Text(
                        text = "Vui lòng chọn tối đa ${flexInfo.maxCategories} danh mục:",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(flexInfo.availableCategories, key = { it.categoryId }) { category ->
                    val isChecked = selectedIds.contains(category.categoryId)
                    val isDisabled = !isChecked && selectedIds.size >= flexInfo.maxCategories

                    CategorySelectionItem(
                        category = category,
                        isChecked = isChecked,
                        isDisabled = isDisabled,
                        onCheckedChange = { viewModel.toggleCategory(category.categoryId) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelectionItem(
    category: CategoryOption,
    isChecked: Boolean,
    isDisabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val pct = if (category.cashbackPercentage % 1f == 0f) category.cashbackPercentage.toInt() else category.cashbackPercentage
    val capStr = if (category.maxCap != null) "Tối đa ${formatNumber(category.maxCap)}đ/kỳ" else "Không giới hạn"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDisabled && !isChecked) Color(0xFFF3F4F6) else Color.White),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .clickable(enabled = !isDisabled) { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDisabled && !isChecked) Color.Gray else Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Hoàn $pct% - $capStr", fontSize = 13.sp, color = if (isDisabled && !isChecked) Color.LightGray else Color(0xFF157FEC))

                category.notice?.let { noticeText ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = noticeText, fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                }
            }
            Checkbox(checked = isChecked, onCheckedChange = null, enabled = !isDisabled)
        }
    }
}