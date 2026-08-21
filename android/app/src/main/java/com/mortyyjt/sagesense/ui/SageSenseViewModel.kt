package com.mortyyjt.sagesense.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mortyyjt.sagesense.AppContainer
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.data.WatchlistEntity
import com.mortyyjt.sagesense.network.AgentAnswer
import com.mortyyjt.sagesense.network.agentFailureMessage
import com.mortyyjt.sagesense.network.mapAgentFailure
import com.mortyyjt.sagesense.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SageSenseUiState(
    val events: List<RiskEventEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
    val locale: String = "en-AU",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val onboardingComplete: Boolean = false,
    val permissionSetupPromptSeen: Boolean = false,
    val ready: Boolean = false,
)

sealed interface AgentUiState {
    data object Idle : AgentUiState
    data object Loading : AgentUiState
    data class Success(val answer: AgentAnswer) : AgentUiState
    data class Error(val message: String) : AgentUiState
}

sealed interface ManualCheckUiState {
    data object Idle : ManualCheckUiState
    data object Checking : ManualCheckUiState
    data class Success(val eventId: String) : ManualCheckUiState
    data class Error(val message: String) : ManualCheckUiState
}

class SageSenseViewModel(private val container: AppContainer) : ViewModel() {
    private val storedPreferences = combine(
        container.preferences.language,
        container.preferences.onboardingComplete,
        container.preferences.permissionSetupPromptSeen,
        container.preferences.themeMode,
    ) { locale, onboarding, permissionPromptSeen, themeMode ->
        StoredPreferences(locale, onboarding, permissionPromptSeen, themeMode)
    }

    val uiState: StateFlow<SageSenseUiState> = combine(
        container.riskRepository.events,
        container.riskRepository.watchlist,
    ) { events, watchlist -> events to watchlist }
        .combine(storedPreferences) { (events, watchlist), preferences ->
        SageSenseUiState(
            events = events,
            watchlist = watchlist,
            locale = preferences.locale,
            themeMode = ThemeMode.fromStorage(preferences.themeMode),
            onboardingComplete = preferences.onboardingComplete,
            permissionSetupPromptSeen = preferences.permissionSetupPromptSeen,
            ready = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SageSenseUiState())

    private val _agentState = MutableStateFlow<AgentUiState>(AgentUiState.Idle)
    val agentState: StateFlow<AgentUiState> = _agentState

    private val _manualCheckState = MutableStateFlow<ManualCheckUiState>(ManualCheckUiState.Idle)
    val manualCheckState: StateFlow<ManualCheckUiState> = _manualCheckState

    fun setLanguage(locale: String) {
        viewModelScope.launch { container.preferences.setLanguage(locale) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { container.preferences.setThemeMode(themeMode.storageValue) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { container.preferences.completeOnboarding() }
    }

    fun markPermissionSetupPromptSeen() {
        viewModelScope.launch { container.preferences.markPermissionSetupPromptSeen() }
    }

    fun clearHistory() {
        viewModelScope.launch { container.riskRepository.clearHistory() }
    }

    fun resetAgent() {
        _agentState.value = AgentUiState.Idle
    }

    fun checkManually(sender: String, content: String) {
        if (sender.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            _manualCheckState.value = ManualCheckUiState.Checking
            _manualCheckState.value = runCatching {
                container.riskRepository.analyseAndStore(
                    sourceType = "manual",
                    sender = sender.trim().takeIf(String::isNotEmpty),
                    text = content.trim().ifBlank { sender.trim() },
                )
            }.fold(
                onSuccess = { ManualCheckUiState.Success(it.id) },
                onFailure = {
                    ManualCheckUiState.Error(
                        if (uiState.value.locale == "zh-CN") {
                            "本机检测暂时失败，请重试。"
                        } else {
                            "The local check could not finish. Please try again."
                        },
                    )
                },
            )
        }
    }

    fun resetManualCheck() {
        _manualCheckState.value = ManualCheckUiState.Idle
    }

    fun askAgent(message: String, activeEventId: String?) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _agentState.value = AgentUiState.Loading
            val state = uiState.value
            val active = state.events.firstOrNull { it.id == activeEventId }
            val result = container.agentClient.ask(
                locale = state.locale,
                message = message.trim(),
                activeEvent = active,
                recentEvents = state.events.filterNot { it.id == activeEventId }.take(10),
                watchlist = state.watchlist,
            )
            result.exceptionOrNull()?.let { error ->
                // Exception messages may contain provider response fragments.
                // Keep logcat useful without writing potentially sensitive text.
                Log.w("SageSenseAgent", "Agent request failed: ${error.javaClass.simpleName}")
            }
            _agentState.value = result.fold(
                onSuccess = { AgentUiState.Success(it) },
                onFailure = { AgentUiState.Error(agentFailureMessage(mapAgentFailure(it), state.locale)) },
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SageSenseViewModel(container) as T
        }
    }
}

private data class StoredPreferences(
    val locale: String,
    val onboardingComplete: Boolean,
    val permissionSetupPromptSeen: Boolean,
    val themeMode: String,
)
