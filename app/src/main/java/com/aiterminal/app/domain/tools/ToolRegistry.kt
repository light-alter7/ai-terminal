package com.aiterminal.app.domain.tools

import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.domain.model.PermissionTier

class ToolRegistry(
    private val permissionGate: PermissionGate
) {

    private val toolsMap = mutableMapOf<String, ITool>()

    fun registerTool(tool: ITool, tier: PermissionTier) {
        toolsMap[tool.name] = tool
        permissionGate.registerToolTier(tool.name, tier)
    }

    fun getTool(name: String): ITool? = toolsMap[name]

    fun getAllTools(): List<ITool> = toolsMap.values.toList()

    fun getTierForTool(name: String): PermissionTier = permissionGate.getTierForTool(name)

    /**
     * Serializes registered tools into standard JSON tool definitions for OpenAI/Anthropic APIs.
     */
    fun toOpenAiToolsJson(): String {
        val toolsJsonList = toolsMap.values.map { tool ->
            """
            {
              "type": "function",
              "function": {
                "name": "${tool.name}",
                "description": "${escapeJson(tool.description)}",
                "parameters": ${tool.parametersSchemaJson}
              }
            }
            """.trimIndent()
        }
        return "[${toolsJsonList.joinToString(",")}]"
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
