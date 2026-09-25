package com.aiterminal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.aiterminal.app.ui.theme.CardShapeSmall
import com.aiterminal.app.ui.theme.LoomGradients
import com.aiterminal.app.ui.theme.PillShape
import com.aiterminal.app.ui.theme.SignalRose
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary
import com.aiterminal.app.ui.theme.ThreadIndigo
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.glassSurfaceElevated

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
        containerColor = Void
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SignalRose)
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "LoomCode couldn't start",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
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
        containerColor = Void,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            LoomNavBar(selectedScreen = selectedScreen, onSelect = { selectedScreen = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedScreen) {
                Screen.Agent -> AgentScreen(
                    viewModel = agentViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                Screen.Terminal -> TerminalScreen(
                    terminalManager = app.terminalManager,
                    modifier = Modifier.fillMaxSize()
                )
                Screen.Workspace -> WorkspaceScreen(
                    workspaceManager = app.workspaceManager,
                    modifier = Modifier.fillMaxSize()
                )
                Screen.Settings -> SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * A floating glass pill navigation bar — the LoomCode signature chrome.
 * Sits above the content with breathing room on all sides rather than
 * spanning edge-to-edge like a stock Material bottom bar.
 */
@Composable
private fun LoomNavBar(
    selectedScreen: Screen,
    onSelect: (Screen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(64.dp)
            .glassSurfaceElevated(shape = PillShape),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Screen.items.forEach { screen ->
            if (screen == null) return@forEach
            val isSelected = selectedScreen == screen
            LoomNavItem(
                screen = screen,
                isSelected = isSelected,
                onClick = { onSelect(screen) }
            )
        }
    }
}

@Composable
private fun LoomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CardShapeSmall)
            .background(if (isSelected) ThreadIndigo.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = if (isSelected) ThreadIndigo else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = screen.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}
