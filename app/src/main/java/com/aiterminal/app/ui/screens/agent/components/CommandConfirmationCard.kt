package com.aiterminal.app.ui.screens.agent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.ui.theme.CardShape
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.SignalAmber
import com.aiterminal.app.ui.theme.SignalRose
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.glassSurface

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
            .glassSurface(shape = CardShape, borderColor = SignalAmber.copy(alpha = 0.35f))
            .padding(16.dp)
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
                    tint = SignalAmber
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permission required · Execute command",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Working dir: $cwd",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Command Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Void, CardShapeSmall)
                    .padding(14.dp)
            ) {
                Text(
                    text = "$ $command",
                    color = SignalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalRose.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRose)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = SignalAmber)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Command", color = Void, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
