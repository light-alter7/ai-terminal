package com.aiterminal.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}

@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall> = emptyList(),
    val toolCallId: String? = null,
    val toolName: String? = null
)
