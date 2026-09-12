package com.aiterminal.app.domain.agent.providers

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.ProviderConfig
import com.aiterminal.app.domain.model.ToolCall

data class LlmResponse(
    val content: String?,
    val toolCalls: List<ToolCall> = emptyList(),
    val finishReason: String? = null
)

interface ILlmProvider {
    val providerId: String

    suspend fun generateResponse(
        messages: List<ChatMessage>,
        toolsJson: String,
        apiKey: String,
        config: ProviderConfig
    ): AppResult<LlmResponse>
}
