package com.android.opticards.ui.contribution.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ContributionViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun submitData(
        context: Context,
        merchantName: String,
        mccCode: String,
        note: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false

            try {
                val nameBody = merchantName.toRequestBody("text/plain".toMediaTypeOrNull())
                val mccBody = mccCode.toRequestBody("text/plain".toMediaTypeOrNull())
                val noteBody = if (note.isNotBlank()) note.toRequestBody("text/plain".toMediaTypeOrNull()) else null

                var imagePart: MultipartBody.Part? = null
                if (imageUri != null) {
                    val file = getFileFromUri(context, imageUri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    }
                }

                val response = ApiClient.apiService.submitContribution(
                    merchantName = nameBody,
                    mccCode = mccBody,
                    note = noteBody,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "Lỗi hệ thống: Không thể ghi nhận đóng góp (Mã: ${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối mạng: Vui lòng kiểm tra lại đường truyền!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun resetSuccessState() { _isSuccess.value = false }
}