package com.aiterminal.app.domain.tools

import com.aiterminal.app.domain.model.ToolResult

interface ITool {
    val name: String
    val description: String
    val parametersSchemaJson: String

    suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult
}
