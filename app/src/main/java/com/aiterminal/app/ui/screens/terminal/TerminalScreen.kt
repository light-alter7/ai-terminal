package com.aiterminal.app.ui.screens.terminal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.aiterminal.app.platform.terminal.TerminalManager
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    terminalManager: TerminalManager,
    modifier: Modifier = Modifier
) {
    val outputLines by terminalManager.terminalOutput.collectAsState()
    val isExecuting by terminalManager.isExecuting.collectAsState()
    val scope = rememberCoroutineScope()

    ComposeTerminalView(
        outputLines = outputLines,
        isExecuting = isExecuting,
        onExecuteCommand = { cmd ->
            if (cmd == "clear") {
                terminalManager.clearScreen()
            } else {
                scope.launch {
                    terminalManager.executeCommand(cmd)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
