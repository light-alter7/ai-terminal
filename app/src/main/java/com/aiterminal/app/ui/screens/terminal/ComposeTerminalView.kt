package com.aiterminal.app.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.ui.theme.TerminalAmber
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalGreen
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@Composable
fun ComposeTerminalView(
    outputLines: List<String>,
    onExecuteCommand: (String) -> Unit,
    isExecuting: Boolean,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    LaunchedEffect(outputLines.size) {
        if (outputLines.isNotEmpty()) {
            listState.animateScrollToItem(outputLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Output Stream Box
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(outputLines) { line ->
                val color = when {
                    line.startsWith("$") -> TerminalAmber
                    line.startsWith("[Exit") || line.contains("Error", ignoreCase = true) -> Color(0xFFF85149)
                    line.startsWith("AI Terminal") -> TerminalGreen
                    else -> TextPrimary
                }
                Text(
                    text = line,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Accessory Keys Bar (Ctrl, Esc, Tab, Up, Down)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("TAB", "CTRL-C", "ESC", "UP", "DOWN", "CLEAR").forEach { key ->
                OutlinedButton(
                    onClick = {
                        when (key) {
                            "TAB" -> commandInput += "    "
                            "CTRL-C" -> commandInput = ""
                            "ESC" -> commandInput = ""
                            "UP" -> {
                                if (history.isNotEmpty() && historyIndex < history.size - 1) {
                                    historyIndex++
                                    commandInput = history[history.size - 1 - historyIndex]
                                }
                            }
                            "DOWN" -> {
                                if (historyIndex > 0) {
                                    historyIndex--
                                    commandInput = history[history.size - 1 - historyIndex]
                                } else if (historyIndex == 0) {
                                    historyIndex = -1
                                    commandInput = ""
                                }
                            }
                            "CLEAR" -> onExecuteCommand("clear")
                        }
                    },
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(key, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Interactive Shell Input Prompt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = TerminalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )

            BasicTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(TerminalGreen),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedButton(
                onClick = {
                    val cmd = commandInput.trim()
                    if (cmd.isNotBlank()) {
                        history.add(cmd)
                        historyIndex = -1
                        commandInput = ""
                        onExecuteCommand(cmd)
                    }
                },
                enabled = commandInput.isNotBlank() && !isExecuting,
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isExecuting) "..." else "Run",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
private fun Color(color: Long) = androidx.compose.ui.graphics.Color(color)
