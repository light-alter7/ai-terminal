package com.aiterminal.app.ui.screens.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.MessageRole
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBlue
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalGreen
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TerminalSurfaceVariant
import com.aiterminal.app.ui.theme.TextMuted
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

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
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalSurfaceVariant)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = TextPrimary,
                        fontSize = 14.sp
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Display tool calls if any
                    for (toolCall in message.toolCalls) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalBackground)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = TerminalBlue,
                                    modifier = Modifier.width(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tool Call: ${toolCall.name}",
                                    color = TerminalBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBackground)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TerminalGreen,
                            modifier = Modifier.width(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tool Output: ${message.toolName ?: "Result"}",
                            color = TerminalGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.content,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
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
