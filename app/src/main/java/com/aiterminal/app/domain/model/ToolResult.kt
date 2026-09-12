package com.aiterminal.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ToolResult(
    val toolCallId: String,
    val toolName: String,
    val isSuccess: Boolean,
    val content: String,
    val error: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun success(
            toolCallId: String,
            toolName: String,
            content: String,
            metadata: Map<String, String> = emptyMap()
        ): ToolResult {
            return ToolResult(
                toolCallId = toolCallId,
                toolName = toolName,
                isSuccess = true,
                content = content,
                metadata = metadata
            )
        }

        fun failure(
            toolCallId: String,
            toolName: String,
            errorMessage: String,
            metadata: Map<String, String> = emptyMap()
        ): ToolResult {
            return ToolResult(
                toolCallId = toolCallId,
                toolName = toolName,
                isSuccess = false,
                content = "Error: $errorMessage",
                error = errorMessage,
                metadata = metadata
            )
        }
    }
}
