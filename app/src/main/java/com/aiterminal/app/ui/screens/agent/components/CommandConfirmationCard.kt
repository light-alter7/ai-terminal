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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
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
import com.aiterminal.app.ui.theme.TerminalAmber
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalRed
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@Composable
fun CommandConfirmationCard(
    command: String,
    cwd: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
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
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TerminalAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permission Required: Execute Command",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Working dir: $cwd",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBackground)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "$ $command",
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalRed)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalAmber)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Command", color = TerminalBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
