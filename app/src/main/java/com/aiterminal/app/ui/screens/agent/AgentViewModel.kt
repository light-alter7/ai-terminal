package com.aiterminal.app.ui.screens.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiterminal.app.domain.agent.AgentEngine
import com.aiterminal.app.domain.agent.AgentState
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.MessageRole
import com.aiterminal.app.platform.terminal.TerminalManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(
    private val agentEngine: AgentEngine,
    private val terminalManager: TerminalManager
) : ViewModel() {

    // Filter out internal system messages from user UI
    val userVisibleMessages: StateFlow<List<ChatMessage>> = agentEngine.messages
        .map { list -> list.filter { it.role != MessageRole.SYSTEM } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agentState: StateFlow<AgentState> = agentEngine.state

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    fun onInputChanged(text: String) {
        _promptInput.value = text
    }

    fun submitPrompt() {
        val prompt = _promptInput.value.trim()
        if (prompt.isBlank()) return

        _promptInput.value = ""
        viewModelScope.launch {
            // Include recent terminal log context if available
            val recentLogs = terminalManager.terminalOutput.value.takeLast(30).joinToString("\n")
            agentEngine.submitUserPrompt(prompt, recentLogs)
        }
    }

    fun resolveConfirmation(approved: Boolean) {
        viewModelScope.launch {
            agentEngine.resolvePendingConfirmation(approved)
        }
    }

    fun clearChat() {
        agentEngine.clearHistory()
    }
}
