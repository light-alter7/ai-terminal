package com.aiterminal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Agent : Screen("agent", "Agent", Icons.Default.Psychology)
    object Terminal : Screen("terminal", "Terminal", Icons.Default.Code)
    object Workspace : Screen("workspace", "Files", Icons.Default.Folder)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Agent, Terminal, Workspace, Settings)
    }
}
