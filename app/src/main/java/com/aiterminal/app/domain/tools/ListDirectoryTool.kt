package com.aiterminal.app.domain.tools

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager

class ListDirectoryTool(private val workspaceManager: WorkspaceManager) : ITool {

    override val name: String = "list_directory"

    override val description: String =
        "Lists all files and directories at the specified path within the project workspace (defaults to root '.')."

    override val parametersSchemaJson: String = """
        {
          "type": "object",
          "properties": {
            "path": {
              "type": "string",
              "description": "Relative path to list (e.g. '.' or 'src'). Defaults to '.'",
              "default": "."
            }
          }
        }
    """.trimIndent()

    override suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult {
        val path = arguments["path"] ?: "."
        return when (val result = workspaceManager.listDirectory(path)) {
            is AppResult.Success -> {
                val entries = result.data
                if (entries.isEmpty()) {
                    ToolResult.success(callId, name, "Directory '$path' is empty.")
                } else {
                    val formatted = entries.joinToString("\n") { entry ->
                        val typeIndicator = if (entry.isDirectory) "[DIR] " else "      "
                        val sizeStr = if (entry.isDirectory) "" else " (${entry.sizeBytes} bytes)"
                        "$typeIndicator${entry.relativePath}$sizeStr"
                    }
                    ToolResult.success(callId, name, formatted)
                }
            }
            is AppResult.Error -> ToolResult.failure(callId, name, result.message)
        }
    }
}
