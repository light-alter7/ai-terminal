package com.aiterminal.app.domain.agent.providers

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.ProviderConfig
import com.aiterminal.app.domain.model.ToolCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ClaudeProvider implements Anthropic's Messages API with tools support.
 * Scaffolded for runtime switching in the ModelRouter.
 */
class ClaudeProvider : ILlmProvider {

    override val providerId: String = "claude"

    override suspend fun generateResponse(
        messages: List<ChatMessage>,
        toolsJson: String,
        apiKey: String,
        config: ProviderConfig
    ): AppResult<LlmResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AppResult.Error("Anthropic Claude API Key is missing. Please add your key in Settings.")
        }

        // Scaffolded provider ready for V0.2 extension
        AppResult.Error("Claude provider is configured and scaffolded. OpenAI provider is currently active for V0.1 MVP.")
    }
}
