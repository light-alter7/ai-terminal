package com.aiterminal.app.ui.screens.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.MessageRole
import com.aiterminal.app.ui.theme.CardShape
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.GraphiteHigh
import com.aiterminal.app.ui.theme.SignalEmerald
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary
import com.aiterminal.app.ui.theme.ThreadIndigo
import com.aiterminal.app.ui.theme.ThreadIndigoDim
import com.aiterminal.app.ui.theme.ThreadViolet
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.glassSurface

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    when (message.role) {
        MessageRole.USER -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            Brush.linearGradient(listOf(ThreadIndigoDim, ThreadIndigo.copy(alpha = 0.55f))),
                            CardShape
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = message.content,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        MessageRole.ASSISTANT -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.95f)) {
                    if (message.content.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassSurface(shape = CardShape)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Display tool calls if any
                    for (toolCall in message.toolCalls) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassSurface(shape = CardShapeSmall)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = ThreadViolet,
                                    modifier = Modifier.width(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tool call · ${toolCall.name}",
                                    color = ThreadViolet,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        MessageRole.TOOL -> {
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .glassSurface(shape = CardShapeSmall)
                    .clickable { expanded = !expanded }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SignalEmerald,
                            modifier = Modifier.width(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Output · ${message.toolName ?: "Result"}",
                            color = SignalEmerald,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message.content,
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        MessageRole.SYSTEM -> {
            // Internal system prompt, generally hidden from user view
        }
    }
}
