package com.android.opticards.ui.onboarding.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.android.opticards.data.model.BankResponse
import com.android.opticards.data.model.CardTemplate
import com.android.opticards.ui.onboarding.viewmodel.OnboardingViewModel
import com.android.opticards.ui.onboarding.UserCardDraft
import com.android.opticards.ui.theme.shimmerEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectionScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onNext: () -> Unit,
    onGoToFinish: () -> Unit
) {
    val selectedCards by viewModel.selectedCards.collectAsState()
    val bankList by viewModel.bankList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSubmitSuccess by viewModel.isSubmitSuccess.collectAsState()

    val isNextEnabled = selectedCards.isNotEmpty()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    LaunchedEffect(isSubmitSuccess) {
        if (isSubmitSuccess) {
            viewModel.resetSubmitStatus()
            onGoToFinish()
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(vertical = 18.dp))
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onNext,
                    enabled = isNextEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF157FEC),
                        contentColor = Color.White
                    )
                ) {
                    Text("Tiếp tục", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        viewModel.clearAllDrafts()
                        viewModel.submitOnboarding()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Thiết lập sau", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    color = Color(0xFF157FEC)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text(
                        text = "Bạn đang sở hữu những thẻ nào?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Text (
                        text = "OptiCards cần biết bạn đang sở hữu những dòng thẻ nào để có thể đưa ra đề xuất tốt nhất!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                    )
                }

                if (bankList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().fillParentMaxHeight(0.8f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(color = Color(0xFF157FEC))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Đang tải dữ liệu...", color = Color.Gray, fontSize = 15.sp)
                            } else {
                                Text(text = "Không có dữ liệu", color = Color.Gray, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.refreshData() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tải lại danh sách", color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    items(bankList) { bank ->
                        BankExpandableItem(
                            bank = bank,
                            selectedCards = selectedCards,
                            onCardToggle = { card, isChecked ->
                                viewModel.toggleCardSelection(bank, card, isChecked)
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun BankExpandableItem(
    bank: BankResponse,
    selectedCards: List<UserCardDraft>,
    onCardToggle: (CardTemplate, Boolean) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = bank.logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) },
                    error = { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEBEBEB))) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                bank.cards.forEach { cardTemplate ->
                    CardRow(
                        card = cardTemplate,
                        isSelected = selectedCards.any { it.cardId == cardTemplate.id },
                        onCheckedChange = { isChecked ->
                            onCardToggle(cardTemplate, isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardRow(
    card: CardTemplate,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.padding(end = 8.dp),
            checked = isSelected,
            onCheckedChange = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        SubcomposeAsyncImage(
            model = card.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(60.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) },
            error = { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEBEBEB))) }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = card.name, style = MaterialTheme.typography.bodyMedium)
    }
}