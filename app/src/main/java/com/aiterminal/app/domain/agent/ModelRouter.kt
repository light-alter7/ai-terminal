package com.aiterminal.app.domain.agent

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.core.security.SecureKeyStore
import com.aiterminal.app.domain.agent.providers.ILlmProvider
import com.aiterminal.app.domain.agent.providers.LlmResponse
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.ModelRoutingConfig
import com.aiterminal.app.domain.model.ProviderConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * ModelRouter dynamically routes requests to the configured provider at runtime.
 * Configuration is loaded from routing_config.json and can be modified or overridden on the fly.
 */
class ModelRouter(
    private val secureKeyStore: SecureKeyStore,
    private val providers: Map<String, ILlmProvider>,
    initialConfig: ModelRoutingConfig
) {

    private val _config = MutableStateFlow(initialConfig)
    val config: StateFlow<ModelRoutingConfig> = _config.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun getActiveProviderId(): String = _config.value.activeProvider

    fun getActiveProviderConfig(): ProviderConfig? {
        val activeId = getActiveProviderId()
        return _config.value.providers[activeId]
    }

    fun setActiveProvider(providerId: String) {
        val current = _config.value
        if (current.providers.containsKey(providerId)) {
            _config.value = current.copy(activeProvider = providerId)
        }
    }

    fun getPreferredProviderForTask(taskType: String): String {
        val matchingRule = _config.value.taskRoutingRules.firstOrNull { it.taskType.equals(taskType, ignoreCase = true) }
        return matchingRule?.preferredProvider ?: getActiveProviderId()
    }

    suspend fun routeRequest(
        messages: List<ChatMessage>,
        toolsJson: String,
        overrideProviderId: String? = null
    ): AppResult<LlmResponse> {
        val targetProviderId = overrideProviderId ?: getActiveProviderId()
        val provider = providers[targetProviderId]
            ?: return AppResult.Error("No provider registered for ID: '$targetProviderId'")

        val providerConfig = _config.value.providers[targetProviderId]
            ?: return AppResult.Error("Configuration not found for provider: '$targetProviderId'")

        val apiKey = secureKeyStore.getApiKey(targetProviderId)
        if (apiKey.isNullOrBlank()) {
            return AppResult.Error(
                "API Key for '${providerConfig.name}' is missing. Please set your key in Settings."
            )
        }

        return provider.generateResponse(
            messages = messages,
            toolsJson = toolsJson,
            apiKey = apiKey,
            config = providerConfig
        )
    }

    companion object {
        fun fromConfigJson(
            configJson: String,
            secureKeyStore: SecureKeyStore,
            providers: Map<String, ILlmProvider>
        ): ModelRouter {
            val parsedConfig = try {
                Json { ignoreUnknownKeys = true }.decodeFromString<ModelRoutingConfig>(configJson)
            } catch (e: Exception) {
                ModelRoutingConfig(activeProvider = "openai")
            }
            return ModelRouter(secureKeyStore, providers, parsedConfig)
        }
    }
}
