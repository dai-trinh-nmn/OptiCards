package com.android.opticards.ui.auth.screens

import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.android.opticards.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.opticards.BuildConfig
import com.android.opticards.data.local.TokenManager
import com.android.opticards.ui.auth.viewmodel.LoginState
import com.android.opticards.ui.auth.viewmodel.LoginViewModel
import com.android.opticards.ui.components.CustomPopup

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val tokenManager = remember { TokenManager(context) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginState.Success -> {
                tokenManager.saveAccessToken(state.accessToken)
                tokenManager.saveRefreshToken(state.refreshToken)
                tokenManager.setOnboarded(state.isOnboarded)

                viewModel.resetState()

                onLoginSuccess(state.isOnboarded)
            }
            is LoginState.Error -> {
                delay(3000)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState is LoginState.Banned) {
            CustomPopup(
                title = "Truy cập bị từ chối!",
                message = (uiState as LoginState.Banned).message,
                icon = Icons.Outlined.Block,
                iconTint = Color.Red,
                primaryButtonText = "Đã hiểu",
                onPrimaryClick = { viewModel.resetState() },
                onDismissRequest = { viewModel.resetState() }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Chi tiêu thông minh\nTối ưu tiền hoàn",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 40.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.login_screen_illustration),
                contentDescription = "Login Illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Chào mừng bạn đến với OptiCards\nVui lòng đăng nhập để tiếp tục!",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (uiState !is LoginState.Loading) {
                        coroutineScope.launch {
                            try {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(webClientId)
                                    .setAutoSelectEnabled(true)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context
                                )

                                val credential = result.credential

                                if (credential is androidx.credentials.CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    viewModel.loginWithBackend(idToken)
                                }
                            } catch (e: Exception) {
                                Log.e("Auth", "Người dùng hủy hoặc lỗi popup: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState is LoginState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tiếp tục với Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        AnimatedVisibility(
            visible = uiState is LoginState.Error,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState is LoginState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = (uiState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}