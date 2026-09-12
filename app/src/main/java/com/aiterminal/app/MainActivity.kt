package com.aiterminal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aiterminal.app.ui.navigation.Screen
import com.aiterminal.app.ui.screens.agent.AgentScreen
import com.aiterminal.app.ui.screens.agent.AgentViewModel
import com.aiterminal.app.ui.screens.settings.SettingsScreen
import com.aiterminal.app.ui.screens.settings.SettingsViewModel
import com.aiterminal.app.ui.screens.terminal.TerminalScreen
import com.aiterminal.app.ui.screens.workspace.WorkspaceScreen
import com.aiterminal.app.ui.theme.AiTerminalTheme
import com.aiterminal.app.ui.theme.TerminalBackground
import com.aiterminal.app.ui.theme.TerminalBlue
import com.aiterminal.app.ui.theme.TerminalBorder
import com.aiterminal.app.ui.theme.TerminalSurface
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AiTerminalApp
        val startupError = app.startupError

        if (startupError != null) {
            setContent {
                AiTerminalTheme {
                    StartupErrorScreen(startupError)
                }
            }
            return
        }

        val agentViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AgentViewModel(app.agentEngine, app.terminalManager) as T
            }
        })[AgentViewModel::class.java]

        val settingsViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(app.secureKeyStore, app.modelRouter) as T
            }
        })[SettingsViewModel::class.java]

        setContent {
            AiTerminalTheme {
                MainAppScreen(
                    app = app,
                    agentViewModel = agentViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
private fun StartupErrorScreen(error: Throwable) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TerminalBackground
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
            Text(
                text = "AI Terminal could not start",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error.message ?: error::class.java.simpleName,
                color = TextSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "The full stack trace was written to logcat under AiTerminalApp.",
                color = TextSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun MainAppScreen(
    app: AiTerminalApp,
    agentViewModel: AgentViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Agent) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TerminalBackground,
        bottomBar = {
            NavigationBar(
                containerColor = TerminalSurface,
                tonalElevation = 4.dp
            ) {
                Screen.items.forEach { screen ->
                    val isSelected = selectedScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalBlue,
                            selectedTextColor = TerminalBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = TerminalSurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedScreen) {
            Screen.Agent -> AgentScreen(
                viewModel = agentViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Terminal -> TerminalScreen(
                terminalManager = app.terminalManager,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Workspace -> WorkspaceScreen(
                workspaceManager = app.workspaceManager,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Settings -> SettingsScreen(
                viewModel = settingsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
