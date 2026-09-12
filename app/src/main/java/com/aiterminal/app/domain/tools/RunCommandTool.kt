package com.aiterminal.app.domain.tools

import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import com.aiterminal.app.platform.terminal.TerminalManager

class RunCommandTool(
    private val terminalManager: TerminalManager,
    private val workspaceManager: WorkspaceManager
) : ITool {

    override val name: String = "run_command"

    override val description: String =
        "Executes a shell command within the project workspace environment and returns stdout, stderr, and exit code."

    override val parametersSchemaJson: String = """
        {
          "type": "object",
          "properties": {
            "command": {
              "type": "string",
              "description": "The exact shell command to execute (e.g. 'python hello.py' or 'ls -la')"
            },
            "cwd": {
              "type": "string",
              "description": "Optional working directory relative to workspace root. Defaults to '.'",
              "default": "."
            }
          },
          "required": ["command"]
        }
    """.trimIndent()

    override suspend fun execute(arguments: Map<String, String>, callId: String): ToolResult {
        val command = arguments["command"] ?: return ToolResult.failure(callId, name, "Missing required argument 'command'")
        val relCwd = arguments["cwd"] ?: "."

        val targetDir = try {
            workspaceManager.resolvePath(relCwd)
        } catch (e: Exception) {
            return ToolResult.failure(callId, name, "Invalid working directory: ${e.message}")
        }

        val output = terminalManager.executeCommand(command, targetDir)

        val formattedResult = buildString {
            if (output.stdout.isNotBlank()) {
                appendLine(output.stdout.trim())
            }
            if (output.stderr.isNotBlank()) {
                appendLine("[stderr]")
                appendLine(output.stderr.trim())
            }
            appendLine("[exit code: ${output.exitCode}]")
        }.trim()

        return if (output.isSuccess) {
            ToolResult.success(
                toolCallId = callId,
                toolName = name,
                content = formattedResult,
                metadata = mapOf("exitCode" to output.exitCode.toString(), "command" to command)
            )
        } else {
            ToolResult.failure(
                toolCallId = callId,
                toolName = name,
                errorMessage = formattedResult,
                metadata = mapOf("exitCode" to output.exitCode.toString(), "command" to command)
            )
        }
    }
}
