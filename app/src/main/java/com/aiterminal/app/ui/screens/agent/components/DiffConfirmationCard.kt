package com.aiterminal.app.ui.screens.agent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.platform.filesystem.DiffLineType
import com.aiterminal.app.platform.filesystem.FileDiff
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalGreen
import com.aiterminal.app.ui.theme.TerminalGreenBg
import com.aiterminal.app.ui.theme.TerminalRed
import com.aiterminal.app.ui.theme.TerminalRedBg
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TerminalSurfaceVariant
import com.aiterminal.app.ui.theme.TextMuted
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@Composable
fun DiffConfirmationCard(
    path: String,
    diff: FileDiff,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = TerminalGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (diff.isNewFile) "Create: $path" else "Modify: $path",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "+${diff.addedCount} / -${diff.removedCount} lines",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diff Scrollable Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBackground)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            ) {
                LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                    items(diff.lines) { line ->
                        val (bgColor, textColor, prefix) = when (line.type) {
                            DiffLineType.ADDED -> Triple(TerminalGreenBg, TerminalGreen, "+")
                            DiffLineType.REMOVED -> Triple(TerminalRedBg, TerminalRed, "-")
                            DiffLineType.UNCHANGED -> Triple(TerminalBackground, TextSecondary, " ")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${line.newLineNumber ?: line.oldLineNumber ?: ""}".padStart(3, ' '),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = prefix,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(14.dp)
                            )
                            Text(
                                text = line.text,
                                color = textColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalRed)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept Changes", color = TerminalBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
