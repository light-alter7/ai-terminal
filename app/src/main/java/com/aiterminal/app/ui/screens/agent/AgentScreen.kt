package com.aiterminal.app.ui.screens.agent

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.aiterminal.app.domain.agent.AgentState
import com.aiterminal.app.domain.agent.PendingConfirmation
import com.aiterminal.app.ui.screens.agent.components.CommandConfirmationCard
import com.aiterminal.app.ui.screens.agent.components.DiffConfirmationCard
import com.aiterminal.app.ui.screens.agent.components.MessageBubble
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.GlassFillLow
import com.aiterminal.app.ui.theme.GraphiteHigh
import com.aiterminal.app.ui.theme.GradientText
import com.aiterminal.app.ui.theme.HairlineLow
import com.aiterminal.app.ui.theme.LoomGradients
import com.aiterminal.app.ui.theme.PillShape
import com.aiterminal.app.ui.theme.SignalAmber
import com.aiterminal.app.ui.theme.SignalEmerald
import com.aiterminal.app.ui.theme.SignalRose
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary
import com.aiterminal.app.ui.theme.ThreadCyan
import com.aiterminal.app.ui.theme.ThreadIndigo
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.glassPadding
import com.aiterminal.app.ui.theme.glassSurface

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
            .background(Brush.verticalGradient(listOf(Void, Void)))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Void)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(
                            when (agentState) {
                                is AgentState.Idle -> SignalEmerald
                                is AgentState.Thinking -> ThreadIndigo
                                is AgentState.ExecutingTool -> ThreadCyan
                                is AgentState.AwaitingConfirmation -> SignalAmber
                                is AgentState.Error -> SignalRose
                            }
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                GradientText(
                    text = "LoomCode",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .size(38.dp)
                    .glassSurface(shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LoomGradients.Thread)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        GradientText(
                            text = "LoomCode",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your AI-native development environment",
                            style = MaterialTheme.typography.bodyMedium,
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
                            .glassSurface(shape = PillShape)
                            .glassPadding(com.aiterminal.app.ui.theme.GlassPadding(horizontal = 14.dp, vertical = 10.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = ThreadIndigo,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val statusText = when (val state = agentState) {
                            is AgentState.ExecutingTool -> "Executing tool: ${state.toolName}..."
                            else -> "Reasoning & planning next steps..."
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { viewModel.onInputChanged("List the files in my project and show me main.py") },
                label = { Text("List files & show main.py", style = MaterialTheme.typography.labelLarge) },
                shape = PillShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = GraphiteHigh,
                    labelColor = TextPrimary
                )
            )
            FilterChip(
                selected = false,
                onClick = { viewModel.onInputChanged("Create a hello-world Python script and run it") },
                label = { Text("Create hello-world Python & run", style = MaterialTheme.typography.labelLarge) },
                shape = PillShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = GraphiteHigh,
                    labelColor = TextPrimary
                )
            )
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { viewModel.onInputChanged(it) },
                placeholder = { Text("Ask LoomCode...", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassFillLow,
                    unfocusedContainerColor = GlassFillLow,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ThreadIndigo,
                    focusedIndicatorColor = ThreadIndigo,
                    unfocusedIndicatorColor = HairlineLow
                ),
                shape = PillShape,
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (promptInput.isNotBlank()) LoomGradients.Thread
                        else Brush.linearGradient(listOf(GraphiteHigh, GraphiteHigh))
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { viewModel.submitPrompt() },
                    enabled = promptInput.isNotBlank() && agentState !is AgentState.Thinking
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (promptInput.isNotBlank()) TextPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
