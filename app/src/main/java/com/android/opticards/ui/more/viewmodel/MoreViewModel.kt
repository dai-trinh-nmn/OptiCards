package com.android.opticards.ui.more.viewmodel

import SettleStatementRequest
import StatementDueItem
import UserProfileResponse
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.opticards.data.local.ReminderPrefs
import com.android.opticards.data.model.MembershipPayload
import com.android.opticards.data.network.ApiClient
import com.android.opticards.utils.ReminderManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MoreViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderPrefs = ReminderPrefs(application)
    private val reminderManager = ReminderManager(application)

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile.asStateFlow()

    private val _dueStatements = MutableStateFlow<List<StatementDueItem>>(emptyList())
    val dueStatements: StateFlow<List<StatementDueItem>> = _dueStatements.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _isReminderEnabled = MutableStateFlow(reminderPrefs.isReminderEnabled)
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _daysBefore = MutableStateFlow(reminderPrefs.daysBefore)
    val daysBefore: StateFlow<Int> = _daysBefore.asStateFlow()

    private val _reminderHour = MutableStateFlow(reminderPrefs.reminderHour)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(reminderPrefs.reminderMinute)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profileDeferred = async { ApiClient.apiService.getUserProfile() }
                val statementsDeferred = async { ApiClient.apiService.getDueStatements() }

                val profileRes = profileDeferred.await()
                val statementsRes = statementsDeferred.await()

                if (profileRes.isSuccessful) _userProfile.value = profileRes.body()
                if (statementsRes.isSuccessful) {
                    val items = statementsRes.body() ?: emptyList()
                    _dueStatements.value = items
                    syncAlarms(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun settleStatement(userCardId: Int, statementMonth: String) {
        viewModelScope.launch {
            try {
                val req = SettleStatementRequest(statementMonth)
                val res = ApiClient.apiService.settleStatement(userCardId, req)
                if (res.isSuccessful) {
                    val newList = _dueStatements.value.filterNot { it.userCardId == userCardId && it.statementMonth == statementMonth }
                    _dueStatements.value = newList
                    reminderManager.cancelReminder(userCardId)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateMembership(bankId: Int, tierName: String) {
        viewModelScope.launch {
            try {
                val req = MembershipPayload(bankId, tierName)
                val res = ApiClient.apiService.updateMembership(req)
                if (res.isSuccessful) {
                    val profileRes = ApiClient.apiService.getUserProfile()
                    if (profileRes.isSuccessful) {
                        _userProfile.value = profileRes.body()
                        _toastMessage.emit("Cập nhật hạng thành viên thành công")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateReminderSettings(enabled: Boolean, days: Int, hour: Int, minute: Int) {
        reminderPrefs.isReminderEnabled = enabled
        reminderPrefs.daysBefore = days
        reminderPrefs.reminderHour = hour
        reminderPrefs.reminderMinute = minute

        _isReminderEnabled.value = enabled
        _daysBefore.value = days
        _reminderHour.value = hour
        _reminderMinute.value = minute

        syncAlarms(_dueStatements.value)
    }

    private fun syncAlarms(statements: List<StatementDueItem>) {
        if (!_isReminderEnabled.value) {
            statements.forEach { reminderManager.cancelReminder(it.userCardId) }
            return
        }

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()

        statements.forEach { item ->
            try {
                val date = format.parse(item.dueDate)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date

                    cal.add(Calendar.DAY_OF_YEAR, -_daysBefore.value)
                    cal.set(Calendar.HOUR_OF_DAY, _reminderHour.value)
                    cal.set(Calendar.MINUTE, _reminderMinute.value)
                    cal.set(Calendar.SECOND, 0)

                    val triggerTime = cal.timeInMillis

                    if (triggerTime > now) {
                        reminderManager.scheduleReminder(item.userCardId, item.cardName, triggerTime)
                    } else {
                        reminderManager.cancelReminder(item.userCardId)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    fun clearAllAlarmsAndPreferences() {
        _dueStatements.value.forEach { item ->
            reminderManager.cancelReminder(item.userCardId)
        }

        reminderPrefs.isReminderEnabled = false
        reminderPrefs.daysBefore = 1
        reminderPrefs.reminderHour = 8
        reminderPrefs.reminderMinute = 0

        _isReminderEnabled.value = false
        _daysBefore.value = 1
        _reminderHour.value = 8
        _reminderMinute.value = 0
    }
}