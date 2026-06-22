package com.android.opticards.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.data.local.SearchHistoryManager
import com.android.opticards.ui.components.MerchantListCard
import com.android.opticards.ui.home.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToContribution: (String, Boolean) -> Unit,
    onNavigateToMerchantDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val historyManager = remember { SearchHistoryManager(context) }
    var query by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf<List<String>>(emptyList()) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        try {
            searchHistory = historyManager.getSearchHistory()
        } catch (e: Exception) {}
    }

    fun performSearch(text: String) {
        if (text.isNotBlank()) {
            historyManager.addSearchQuery(text)
            searchHistory = historyManager.getSearchHistory()
            viewModel.searchMerchants(text)
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isBlank()) viewModel.resetSearch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Tìm kiếm MCC, thương hiệu...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch(query) }),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                viewModel.resetSearch()
                                focusRequester.requestFocus()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF157FEC),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    )
                )
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF157FEC))
                }
            } else if (!hasSearched) {
                if (query.isBlank() && searchHistory.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tìm kiếm gần đây", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = {
                                historyManager.clearHistory()
                                searchHistory = emptyList()
                            }) {
                                Text("Xóa lịch sử", color = Color.Gray)
                            }
                        }
                        LazyColumn {
                            items(searchHistory) { historyItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            query = historyItem
                                            performSearch(historyItem)
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = historyItem, fontSize = 15.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            } else {
                if (searchResults.isNotEmpty()) {
                    PullToRefreshBox(
                        isRefreshing = isSearching,
                        onRefresh = { performSearch(query) }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(searchResults, key = { it.merchantId }) { merchant ->
                                MerchantListCard(
                                    merchant = merchant,
                                    onFavoriteClick = { viewModel.toggleFavorite(merchant.merchantId) },
                                    onClick = { onNavigateToMerchantDetail(merchant.merchantId) }
                                )
                            }

                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Không tìm thấy kết quả mong muốn?", color = Color.Gray, fontSize = 13.sp)
                                    TextButton(onClick = { onNavigateToContribution(query, false) }) {
                                        Text("Đóng góp dữ liệu", color = Color(0xFF157FEC), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không tìm thấy kết quả phù hợp cho '$query'",
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onNavigateToContribution(query, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF157FEC)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Đóng góp dữ liệu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}