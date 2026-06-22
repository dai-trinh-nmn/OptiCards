package com.android.opticards.ui.contribution.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.contribution.viewmodel.ContributionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(
    initialQuery: String = "",
    viewModel: ContributionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isMccQuery = initialQuery.length == 4 && initialQuery.all { it.isDigit() }

    var merchantName by remember(initialQuery) { mutableStateOf(if (isMccQuery) "" else initialQuery) }
    var mccCode by remember(initialQuery) { mutableStateOf(if (isMccQuery) initialQuery else "") }
    var note by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isFormValid = merchantName.isNotBlank() && mccCode.isNotBlank() && mccCode.length <= 4

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    if (isSuccess) {
        CustomPopup(
            title = "Gửi thành công!",
            message = "Cảm ơn bạn đã đóng góp.\nThông tin sẽ được kiểm tra và cập nhật sớm nhất!",
            icon = Icons.Filled.CheckCircle,
            iconTint = Color(0xFF4CAF50),
            primaryButtonText = "Quay lại",
            onPrimaryClick = {
                viewModel.resetSuccessState()
                onNavigateBack()
            },
            onDismissRequest = {
                viewModel.resetSuccessState()
                onNavigateBack()
            }
        )
    }

    if (errorMessage != null) {
        CustomPopup(
            title = "Đã xảy ra lỗi",
            message = errorMessage!!,
            primaryButtonText = "Đồng ý",
            onPrimaryClick = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đóng góp dữ liệu", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Giúp cộng đồng cập nhật mã MCC chính xác cho cửa hàng này.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Tên cửa hàng / Thương hiệu *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = mccCode,
                    onValueChange = { if (it.length <= 4) mccCode = it.filter { char -> char.isDigit() } },
                    label = { Text("MCC (4 chữ số) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (Tùy chọn)") },
                    placeholder = { Text("Ví dụ: Thanh toán ApplePay để ra được MCC này") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Hình ảnh xác thực (Tùy chọn)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Ảnh chụp bill thanh toán có hiển thị mã MCC", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(12.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa ảnh", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nhấn để tải ảnh lên", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.submitData(context, merchantName, mccCode, note, imageUri)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Gửi đóng góp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}