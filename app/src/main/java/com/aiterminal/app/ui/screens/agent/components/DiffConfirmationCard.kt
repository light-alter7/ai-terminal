package com.aiterminal.app.ui.screens.agent.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.platform.filesystem.DiffLineType
import com.aiterminal.app.platform.filesystem.FileDiff
import com.aiterminal.app.ui.theme.CardShape
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.SignalEmerald
import com.aiterminal.app.ui.theme.SignalEmeraldBg
import com.aiterminal.app.ui.theme.SignalRose
import com.aiterminal.app.ui.theme.SignalRoseBg
import com.aiterminal.app.ui.theme.TextMuted
import com.aiterminal.app.ui.theme.TextSecondary
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.glassSurface

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
            .glassSurface(shape = CardShape, borderColor = SignalEmerald.copy(alpha = 0.3f))
            .padding(16.dp)
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
                    tint = SignalEmerald
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (diff.isNewFile) "Create · $path" else "Modify · $path",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "+${diff.addedCount} / -${diff.removedCount} lines",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diff Scrollable Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .background(Void, CardShapeSmall)
            ) {
                LazyColumn(modifier = Modifier.padding(vertical = 6.dp)) {
                    items(diff.lines) { line ->
                        val (bgColor, textColor, prefix) = when (line.type) {
                            DiffLineType.ADDED -> Triple(SignalEmeraldBg, SignalEmerald, "+")
                            DiffLineType.REMOVED -> Triple(SignalRoseBg, SignalRose, "-")
                            DiffLineType.UNCHANGED -> Triple(Void, TextSecondary, " ")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(horizontal = 10.dp, vertical = 2.dp),
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

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    border = BorderStroke(1.dp, SignalRose.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRose)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept Changes", color = Void, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
