package com.android.opticards.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.network.ApiClient
import com.android.opticards.data.model.GoogleTokenRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val accessToken: String, val refreshToken: String, val isOnboarded: Boolean) : LoginState()
    data class Error(val message: String) : LoginState()
    data class Banned(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun loginWithBackend(idToken: String) {
        _uiState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val request = GoogleTokenRequest(idToken)
                val response = ApiClient.apiService.verifyGoogleToken(request)

                if (response.isBanned == true) {
                    _uiState.value = LoginState.Banned("Tài khoản của bạn đã bị khóa")
                } else if (response.success == true && response.accessToken != null && response.refreshToken != null) {
                    _uiState.value = LoginState.Success(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        isOnboarded = response.isOnboarded ?: false
                    )
                } else {
                    _uiState.value =
                        LoginState.Error(response.message ?: "Đăng nhập thất bại từ máy chủ")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Đã xảy ra lỗi mạng không xác định"
                _uiState.value = LoginState.Error("Lỗi kết nối: $errorMessage")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}