package com.aiterminal.app.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.domain.agent.AgentState
import com.aiterminal.app.domain.agent.PendingConfirmation
import com.aiterminal.app.ui.screens.agent.components.CommandConfirmationCard
import com.aiterminal.app.ui.screens.agent.components.DiffConfirmationCard
import com.aiterminal.app.ui.screens.agent.components.MessageBubble
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBlue
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalGreen
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TerminalSurfaceVariant
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.userVisibleMessages.collectAsState()
    val agentState by viewModel.agentState.collectAsState()
    val promptInput by viewModel.promptInput.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, agentState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .border(width = 1.dp, color = TerminalBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (agentState) {
                                is AgentState.Idle -> TerminalGreen
                                is AgentState.Thinking -> TerminalBlue
                                is AgentState.ExecutingTool -> TerminalBlue
                                is AgentState.AwaitingConfirmation -> Color(0xFFD29922)
                                is AgentState.Error -> Color(0xFFF85149)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Terminal Agent",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextSecondary
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AI Terminal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mobile AI-native development environment",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }

            // Pending Confirmation Card (Permission Gate)
            if (agentState is AgentState.AwaitingConfirmation) {
                val pending = (agentState as AgentState.AwaitingConfirmation).confirmation
                item {
                    when (pending) {
                        is PendingConfirmation.WriteFile -> {
                            DiffConfirmationCard(
                                path = pending.path,
                                diff = pending.diff,
                                onAccept = { viewModel.resolveConfirmation(true) },
                                onReject = { viewModel.resolveConfirmation(false) }
                            )
                        }
                        is PendingConfirmation.RunCommand -> {
                            CommandConfirmationCard(
                                command = pending.command,
                                cwd = pending.cwd,
                                onConfirm = { viewModel.resolveConfirmation(true) },
                                onCancel = { viewModel.resolveConfirmation(false) }
                            )
                        }
                        is PendingConfirmation.Generic -> {
                            CommandConfirmationCard(
                                command = pending.description,
                                cwd = ".",
                                onConfirm = { viewModel.resolveConfirmation(true) },
                                onCancel = { viewModel.resolveConfirmation(false) }
                            )
                        }
                    }
                }
            }

            // Thinking or Tool Execution Progress
            if (agentState is AgentState.Thinking || agentState is AgentState.ExecutingTool) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TerminalBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val statusText = when (val state = agentState) {
                            is AgentState.ExecutingTool -> "Executing tool: ${state.toolName}..."
                            else -> "Reasoning & planning next steps..."
                        }
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips (V0.1 Acceptance test scenarios)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { viewModel.onInputChanged("List the files in my project and show me main.py") },
                label = { Text("List files & show main.py", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TerminalSurfaceVariant,
                    labelColor = TextPrimary
                )
            )
            FilterChip(
                selected = false,
                onClick = { viewModel.onInputChanged("Create a hello-world Python script and run it") },
                label = { Text("Create hello-world Python & run", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TerminalSurfaceVariant,
                    labelColor = TextPrimary
                )
            )
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .border(1.dp, TerminalBorder)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { viewModel.onInputChanged(it) },
                placeholder = { Text("Ask the agent...", fontSize = 14.sp, color = TextSecondary) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TerminalBackground,
                    unfocusedContainerColor = TerminalBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = TerminalBlue,
                    focusedIndicatorColor = TerminalBlue,
                    unfocusedIndicatorColor = TerminalBorder
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { viewModel.submitPrompt() },
                enabled = promptInput.isNotBlank() && agentState !is AgentState.Thinking
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (promptInput.isNotBlank()) TerminalBlue else TextSecondary
                )
            }
        }
    }
}
