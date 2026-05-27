package com.listeningstats.app.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listeningstats.app.data.local.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val lastFmUser: String = "",
    val lastFmApiKey: String = "",
    val statsFmUser: String = "",
    val saved: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsManager.lastFmUser,
                settingsManager.lastFmApiKey,
                settingsManager.statsFmUser,
            ) { u, k, s ->
                _state.update { it.copy(lastFmUser = u, lastFmApiKey = k, statsFmUser = s) }
            }.collect()
        }
    }

    fun setLastFmUser(user: String) {
        viewModelScope.launch { settingsManager.setLastFmUser(user) }
    }

    fun setLastFmApiKey(apiKey: String) {
        viewModelScope.launch { settingsManager.setLastFmApiKey(apiKey) }
    }

    fun setStatsFmUser(user: String) {
        viewModelScope.launch { settingsManager.setStatsFmUser(user) }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(saved = true) }
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(saved = false) }
        }
    }
}
