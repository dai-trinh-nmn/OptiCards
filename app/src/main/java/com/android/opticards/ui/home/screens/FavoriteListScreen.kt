package com.android.opticards.ui.home.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.opticards.ui.components.MerchantListCard
import com.android.opticards.ui.home.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteListScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMerchantDetail: (Int) -> Unit
) {
    val homeData by viewModel.homeData.collectAsState()
    val favoriteMerchants = homeData?.favoriteMerchants ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách yêu thích", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        if (favoriteMerchants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Chưa có cửa hàng yêu thích nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteMerchants, key = { it.merchantId }) { merchant ->
                    MerchantListCard(
                        merchant = merchant,
                        onFavoriteClick = { viewModel.toggleFavorite(merchant) },
                        onClick = { onNavigateToMerchantDetail(merchant.merchantId) }
                    )
                }
            }
        }
    }
}