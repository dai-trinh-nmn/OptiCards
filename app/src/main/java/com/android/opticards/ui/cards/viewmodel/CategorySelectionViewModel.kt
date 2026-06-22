package com.android.opticards.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.model.FlexCategoryInfoResponse
import com.android.opticards.data.model.UpdateCategoryRequest
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategorySelectionViewModel : ViewModel() {

    private val _info = MutableStateFlow<FlexCategoryInfoResponse?>(null)
    val info: StateFlow<FlexCategoryInfoResponse?> = _info.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchCategoryInfo(userCardId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.apiService.getFlexCategories(userCardId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _info.value = body
                    _selectedIds.value = body.currentSelectedIds.toSet()
                } else {
                    _errorMessage.value = "Không thể tải cấu hình danh mục"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối máy chủ"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleCategory(categoryId: Int) {
        val currentInfo = _info.value ?: return
        _selectedIds.update { currentSelected ->
            val isChecked = currentSelected.contains(categoryId)
            if (isChecked) {
                currentSelected - categoryId
            } else {
                if (currentSelected.size < currentInfo.maxCategories) {
                    currentSelected + categoryId
                } else currentSelected
            }
        }
    }

    fun saveCategories(userCardId: Int) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                val payload = UpdateCategoryRequest(selectedCategoryIds = _selectedIds.value.toList())
                val response = ApiClient.apiService.updateFlexCategories(userCardId, payload)
                if (response.isSuccessful) {
                    _saveSuccess.value = true
                } else {
                    _errorMessage.value = "Lưu thất bại"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối khi lưu"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSuccessState() { _saveSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}