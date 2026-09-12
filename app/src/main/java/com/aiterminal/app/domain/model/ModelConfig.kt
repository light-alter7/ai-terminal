package com.aiterminal.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderConfig(
    val name: String,
    val model: String,
    @SerialName("base_url") val baseUrl: String,
    @SerialName("supports_tools") val supportsTools: Boolean = true,
    @SerialName("max_tokens") val maxTokens: Int = 4096
)

@Serializable
data class TaskRoutingRule(
    @SerialName("task_type") val taskType: String,
    @SerialName("preferred_provider") val preferredProvider: String
)

@Serializable
data class ModelRoutingConfig(
    @SerialName("active_provider") val activeProvider: String = "openai",
    val providers: Map<String, ProviderConfig> = emptyMap(),
    @SerialName("task_routing_rules") val taskRoutingRules: List<TaskRoutingRule> = emptyList()
)
