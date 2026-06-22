package com.android.opticards.ui.more.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Warning
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.opticards.ui.components.CustomPopup
import com.android.opticards.ui.components.OptiCardsBottomNavigation
import com.android.opticards.ui.components.StatementCountdownCard
import com.android.opticards.ui.more.viewmodel.MoreViewModel
import com.android.opticards.utils.singleClick
import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.android.opticards.utils.openNotificationSettings
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MoreViewModel = viewModel(),
    refreshTrigger: Int = 0,
    onLogoutClick: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val dueStatements by viewModel.dueStatements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val daysBefore by viewModel.daysBefore.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    var showMembershipSheet by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showLogoutPopup by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.loadData()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mở rộng", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            OptiCardsBottomNavigation(currentTab = 4, onTabSelected = onTabSelected)
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadData() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(padding),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = profile?.avatarUrl ?: "https://i.pravatar.cc/150",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(60.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = profile?.fullName ?: "Đang tải...", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = profile?.email ?: "", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (dueStatements.isNotEmpty()) {
                    Text("Cần thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Text("Thời hạn thanh toán thực tế có thể bị thay đổi\nHãy kiểm tra sao kê để biết ngày thanh toán chính xác!", fontWeight = FontWeight.Light, fontSize = 12.sp, lineHeight = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    dueStatements.forEach { item ->

                        key(item.userCardId, item.statementMonth) {
                            StatementCountdownCard(
                                item = item,
                                onSettleClick = { viewModel.settleStatement(item.userCardId, item.statementMonth) }
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text("Tiện ích", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                ) {
                    if (profile?.hasMembershipWaiverCard == true) {
                        ListItem(
                            headlineContent = { Text("Thay đổi hạng hội viên") },
                            leadingContent = { Icon(Icons.Default.CardMembership, contentDescription = null, tint = Color(0xFF157FEC)) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.singleClick { showMembershipSheet = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                    }

                    if ((profile?.creditCardCount ?: 0) > 0) {
                        ListItem(
                            headlineContent = { Text("Nhắc nhở thanh toán thẻ") },
                            supportingContent = {
                                if (isReminderEnabled) Text("Đang bật (Nhắc trước $daysBefore ngày)", color = Color(0xFF28A745))
                                else Text("Đang tắt", color = Color.Gray)
                            },
                            leadingContent = { Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFF157FEC)) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { showReminderSheet = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { showLogoutPopup = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDC3545)))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showMembershipSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMembershipSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Hạng hội viên của bạn", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 24.dp))

                profile?.membershipOptions?.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        AsyncImage(model = option.logoUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(option.bankName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    var expanded by remember { mutableStateOf(false) }
                    val currentDisplayTier = option.currentTier ?: "Phân hạng khác"

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = currentDisplayTier,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 24.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            option.availableTiers.forEach { tier ->
                                DropdownMenuItem(
                                    text = { Text(tier, fontWeight = if (tier == currentDisplayTier) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        expanded = false
                                        viewModel.updateMembership(option.bankId, tier)
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showReminderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReminderSheet = false },
            containerColor = Color.White
        ) {
            var tempEnabled by remember { mutableStateOf(isReminderEnabled) }
            var tempDays by remember { mutableStateOf(daysBefore.toString()) }
            var tempHour by remember { mutableIntStateOf(reminderHour) }
            var tempMinute by remember { mutableIntStateOf(reminderMinute) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    tempEnabled = true
                } else {
                    tempEnabled = false
                    Toast.makeText(context, "Ứng dụng cần cấp quyền thông báo để nhắc lịch thanh toán thẻ!", Toast.LENGTH_LONG).show()
                }
            }

            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Cài đặt nhắc nhở", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text("Bật thông báo nhắc nợ thẻ", fontSize = 16.sp)
                    Switch(
                        checked = tempEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val isGranted = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (isGranted) {
                                        tempEnabled = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

                                    if (areNotificationsEnabled) {
                                        tempEnabled = true
                                    } else {
                                        tempEnabled = false
                                        Toast.makeText(context, "Thông báo đang bị tắt. Vui lòng bật lại trong Cài đặt hệ thống!", Toast.LENGTH_LONG).show()
                                        openNotificationSettings(context)
                                    }
                                }
                            } else {
                                tempEnabled = false
                            }
                        }
                    )
                }

                if (tempEnabled) {
                    OutlinedTextField(
                        value = tempDays,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) tempDays = input
                        },
                        label = { Text("Nhắc trước hạn mấy ngày?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Thời gian nhận thông báo", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                    val timeString = String.format("%02d:%02d", tempHour, tempMinute)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        tempHour = selectedHour
                                        tempMinute = selectedMinute
                                    },
                                    tempHour,
                                    tempMinute,
                                    true
                                ).show()
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gửi thông báo vào lúc:", fontWeight = FontWeight.Medium)
                            Text(timeString, fontWeight = FontWeight.Bold, color = Color(0xFF157FEC), fontSize = 16.sp)
                        }
                    }
                }

                Button(
                    onClick = {
                        val d = tempDays.toIntOrNull() ?: 1
                        viewModel.updateReminderSettings(tempEnabled, d, tempHour, tempMinute)
                        showReminderSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC))
                ) { Text("Lưu cài đặt", fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showLogoutPopup) {
        CustomPopup(
            title = "Xác nhận đăng xuất",
            message = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?",
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFE65100),
            primaryButtonText = "Đăng xuất",
            secondaryButtonText = "Hủy",
            onPrimaryClick = {
                showLogoutPopup = false
                viewModel.clearAllAlarmsAndPreferences()
                onLogoutClick()
            },
            onSecondaryClick = { showLogoutPopup = false },
            onDismissRequest = { showLogoutPopup = false }
        )
    }
}