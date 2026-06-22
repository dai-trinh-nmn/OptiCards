package com.android.opticards.ui.onboarding.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.android.opticards.R
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.runtime.rememberCoroutineScope
import com.android.opticards.ui.onboarding.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MembershipSelectionScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val selectedMemberships by viewModel.selectedMemberships.collectAsState()
    val banks = viewModel.getBanksRequiringMembership()

    val isNextEnabled = selectedMemberships.isNotEmpty()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color(0xFF157FEC),
                        modifier = Modifier.size(28.dp).align(Alignment.CenterStart).clickable { onBack() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Hạng hội viên",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Text(
                    text = "Một số đặc quyền như miễn phí thường niên thẻ sẽ được áp dụng tùy vào hạng hội viên của bạn tại ngân hàng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                )
            }

            items(banks) {bank ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                ) {
                    Column (modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = bank.logoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                placeholder = painterResource(R.drawable.bank_sample)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = bank.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            bank.membershipTiers.forEach { tier ->
                                val isSelected = selectedMemberships[bank.id] == tier
                                MembershipChip(
                                    text = tier,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectMembership(bank.id, tier) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onSkip()
                    scope.launch{
                        delay(500)
                        viewModel.clearMembership()
                    }
                }
            ) {
                Icon(Icons.Default.Close, contentDescription = "null", modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bỏ qua", color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNext,
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
                Text("Tiếp tục", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MembershipChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFF0F4FF) else Color(0xFFF5F5F5)
    val contentColor = if (isSelected) Color(0xFF157FEC) else Color.DarkGray
    val borderColor = if (isSelected) Color(0xFF157FEC) else Color.Transparent

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "null",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}