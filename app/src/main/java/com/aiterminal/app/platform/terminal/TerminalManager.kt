package com.aiterminal.app.platform.terminal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class TerminalManager(
    private val processRunner: PtyProcessRunner,
    private val workingDir: File
) {

    private val _terminalOutput = MutableStateFlow<List<String>>(
        listOf("AI Terminal Shell v0.1.0 [Sandboxed Workspace]", "Type 'help' or any command to begin.\n")
    )
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    fun appendOutput(text: String) {
        val current = _terminalOutput.value.toMutableList()
        current.add(text)
        // Keep last 1000 lines
        if (current.size > 1000) {
            current.removeAt(0)
        }
        _terminalOutput.value = current
    }

    suspend fun executeCommand(command: String, cwd: File = workingDir): CommandOutput {
        _isExecuting.value = true
        appendOutput("$ $command")

        val output = processRunner.runCommand(
            command = command,
            workingDir = cwd,
            onOutputChunk = { chunk ->
                appendOutput(chunk)
            }
        )

        if (output.exitCode != 0 && output.stderr.isNotBlank()) {
            appendOutput("[Exit ${output.exitCode}] ${output.stderr}")
        } else if (output.stdout.isBlank() && output.stderr.isBlank()) {
            // Command completed silently (like mkdir or touch)
        }

        _isExecuting.value = false
        return output
    }

    fun clearScreen() {
        _terminalOutput.value = emptyList()
    }
}
