package com.android.opticards.data.local

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val HISTORY_KEY = "recent_searches_list"

    fun getSearchHistory(): List<String> {
        val historyString = prefs.getString(HISTORY_KEY, "") ?: ""
        if (historyString.isBlank()) return emptyList()
        return historyString.split("|;;|")
    }

    fun addSearchQuery(query: String) {
        val currentList = getSearchHistory().toMutableList()
        currentList.remove(query)
        currentList.add(0, query)

        val limitedList = currentList.take(10)

        val newHistoryString = limitedList.joinToString("|;;|")
        prefs.edit().putString(HISTORY_KEY, newHistoryString).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }
}
