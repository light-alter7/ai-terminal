package com.aiterminal.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.aiterminal.app.core.security.SecureKeyStore
import com.aiterminal.app.domain.agent.ModelRouter
import com.aiterminal.app.domain.model.ModelRoutingConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val secureKeyStore: SecureKeyStore,
    private val modelRouter: ModelRouter
) : ViewModel() {

    val routingConfig: StateFlow<ModelRoutingConfig> = modelRouter.config

    private val _openAiKey = MutableStateFlow(secureKeyStore.getApiKey("openai") ?: "")
    val openAiKey: StateFlow<String> = _openAiKey.asStateFlow()

    private val _claudeKey = MutableStateFlow(secureKeyStore.getApiKey("claude") ?: "")
    val claudeKey: StateFlow<String> = _claudeKey.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    fun onOpenAiKeyChanged(key: String) {
        _openAiKey.value = key
    }

    fun onClaudeKeyChanged(key: String) {
        _claudeKey.value = key
    }

    fun saveKeys() {
        if (_openAiKey.value.isNotBlank()) {
            secureKeyStore.saveApiKey("openai", _openAiKey.value.trim())
        }
        if (_claudeKey.value.isNotBlank()) {
            secureKeyStore.saveApiKey("claude", _claudeKey.value.trim())
        }
        _savedMessage.value = "API Keys securely saved in Android Keystore."
    }

    fun selectProvider(providerId: String) {
        modelRouter.setActiveProvider(providerId)
    }

    fun clearSavedMessage() {
        _savedMessage.value = null
    }
}
