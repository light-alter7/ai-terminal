package com.aiterminal.app.domain.tools

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager

class SearchFilesTool(private val workspaceManager: WorkspaceManager) : ITool {

    override val name: String = "search_files"

    override val description: String =
        "Recursively searches files in the workspace for text matching the given query string."

    override val parametersSchemaJson: String = """
        {
          "type": "object",
          "properties": {
            "query": {
              "type": "string",
              "description": "The text pattern or keyword to search for"
            },
            "scope": {
              "type": "string",
              "description": "Optional subdirectory scope to search within. Defaults to '.'",
              "default": "."
            }
          },
          "required": ["query"]
        }
    """.trimIndent()

    override suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult {
        val query = arguments["query"] ?: return ToolResult.failure(callId, name, "Missing required argument 'query'")
        val scope = arguments["scope"] ?: "."

        return when (val result = workspaceManager.searchFiles(query, scope)) {
            is AppResult.Success -> {
                val matches = result.data
                if (matches.isEmpty()) {
                    ToolResult.success(callId, name, "No files found containing query: '$query'")
                } else {
                    val sb = StringBuilder()
                    matches.forEach { fileResult ->
                        sb.appendLine("${fileResult.relativePath}:")
                        fileResult.matches.take(10).forEach { match ->
                            sb.appendLine("  Line ${match.lineNumber}: ${match.lineContent}")
                        }
                    }
                    ToolResult.success(callId, name, sb.toString().trim())
                }
            }
            is AppResult.Error -> ToolResult.failure(callId, name, result.message)
        }
    }
}
