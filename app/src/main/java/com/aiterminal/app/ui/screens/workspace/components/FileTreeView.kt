package com.aiterminal.app.ui.screens.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.platform.filesystem.WorkspaceEntry
import com.aiterminal.app.ui.theme.SignalAmber
import com.aiterminal.app.ui.theme.ThreadCyan
import com.aiterminal.app.ui.theme.TextMuted
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.glassSurface

@Composable
fun FileTreeItem(
    entry: WorkspaceEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .glassSurface(shape = CardShapeSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.Description,
            contentDescription = null,
            tint = if (entry.isDirectory) SignalAmber else ThreadCyan,
            modifier = Modifier.width(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = entry.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )

        if (!entry.isDirectory) {
            Text(
                text = "${entry.sizeBytes} B",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
