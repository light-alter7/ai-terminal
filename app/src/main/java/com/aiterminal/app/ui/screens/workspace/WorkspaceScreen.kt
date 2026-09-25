package com.aiterminal.app.ui.screens.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.platform.filesystem.WorkspaceEntry
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import com.aiterminal.app.ui.screens.workspace.components.FilePreviewDialog
import com.aiterminal.app.ui.screens.workspace.components.FileTreeItem
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.GradientText
import com.aiterminal.app.ui.theme.CardShapeSmall
import androidx.compose.material3.MaterialTheme
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@Composable
fun WorkspaceScreen(
    workspaceManager: WorkspaceManager,
    modifier: Modifier = Modifier
) {
    var entries by remember { mutableStateOf<List<WorkspaceEntry>>(emptyList()) }
    var previewFile by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun refresh() {
        val result = workspaceManager.listDirectory(".")
        if (result is AppResult.Success) {
            entries = result.data
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Void)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                GradientText(
                    text = "Workspace",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${entries.size} items",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(onClick = { refresh() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextSecondary
                )
            }
        }

        // Files List
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Workspace is empty",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ask the AI agent to create files or use the terminal.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                items(entries) { entry ->
                    FileTreeItem(
                        entry = entry,
                        onClick = {
                            if (!entry.isDirectory) {
                                val contentResult = workspaceManager.readFile(entry.relativePath)
                                if (contentResult is AppResult.Success) {
                                    previewFile = Pair(entry.name, contentResult.data)
                                }
                            }
                        }
                    )
                }
            }
        }

        // File Content Dialog
        previewFile?.let { (name, content) ->
            FilePreviewDialog(
                fileName = name,
                fileContent = content,
                onDismiss = { previewFile = null }
            )
        }
    }
}
