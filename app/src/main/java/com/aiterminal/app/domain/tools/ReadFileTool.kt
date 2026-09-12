package com.aiterminal.app.domain.tools

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager

class ReadFileTool(private val workspaceManager: WorkspaceManager) : ITool {

    override val name: String = "read_file"

    override val description: String =
        "Reads the contents of a file at the given relative path within the project workspace."

    override val parametersSchemaJson: String = """
        {
          "type": "object",
          "properties": {
            "path": {
              "type": "string",
              "description": "The relative path of the file to read (e.g. 'main.py' or 'src/app.py')"
            }
          },
          "required": ["path"]
        }
    """.trimIndent()

    override suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult {
        val path = arguments["path"] ?: return ToolResult.failure(callId, name, "Missing required argument 'path'")
        return when (val result = workspaceManager.readFile(path)) {
            is AppResult.Success -> ToolResult.success(callId, name, result.data)
            is AppResult.Error -> ToolResult.failure(callId, name, result.message)
        }
    }
}
