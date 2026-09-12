package com.aiterminal.app.domain.tools

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.platform.filesystem.FileDiff
import com.aiterminal.app.platform.filesystem.WorkspaceManager

class WriteFileTool(private val workspaceManager: WorkspaceManager) : ITool {

    override val name: String = "write_file"

    override val description: String =
        "Writes content to a file at the given relative path within the workspace. Creates parent directories if needed."

    override val parametersSchemaJson: String = """
        {
          "type": "object",
          "properties": {
            "path": {
              "type": "string",
              "description": "The relative path of the file to write (e.g. 'hello.py')"
            },
            "content": {
              "type": "string",
              "description": "The exact full file content or diff to write"
            }
          },
          "required": ["path", "content"]
        }
    """.trimIndent()

    fun previewDiff(path: String, content: String): FileDiff {
        return workspaceManager.getDiffForWrite(path, content)
    }

    override suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult {
        val path = arguments["path"] ?: return ToolResult.failure(callId, name, "Missing required argument 'path'")
        val content = arguments["content"] ?: arguments["diff_or_content"]
            ?: return ToolResult.failure(callId, name, "Missing required argument 'content'")

        return when (val result = workspaceManager.writeFile(path, content)) {
            is AppResult.Success -> {
                val linesCount = content.lines().size
                ToolResult.success(
                    toolCallId = callId,
                    toolName = name,
                    content = "Successfully wrote $linesCount lines to $path",
                    metadata = mapOf("path" to path, "lines" to linesCount.toString())
                )
            }
            is AppResult.Error -> ToolResult.failure(callId, name, result.message)
        }
    }
}
