package com.aiterminal.app.domain.agent

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.MessageRole
import com.aiterminal.app.domain.model.PermissionTier
import com.aiterminal.app.domain.model.ToolCall
import com.aiterminal.app.domain.model.ToolResult
import com.aiterminal.app.domain.tools.ITool
import com.aiterminal.app.domain.tools.ToolRegistry
import com.aiterminal.app.domain.tools.WriteFileTool
import com.aiterminal.app.platform.filesystem.FileDiff
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AgentState {
    object Idle : AgentState()
    object Thinking : AgentState()
    data class ExecutingTool(val toolName: String) : AgentState()
    data class AwaitingConfirmation(val confirmation: PendingConfirmation) : AgentState()
    data class Error(val message: String) : AgentState()
}

sealed class PendingConfirmation {
    abstract val toolCall: ToolCall

    data class WriteFile(
        override val toolCall: ToolCall,
        val path: String,
        val diff: FileDiff
    ) : PendingConfirmation()

    data class RunCommand(
        override val toolCall: ToolCall,
        val command: String,
        val cwd: String
    ) : PendingConfirmation()

    data class Generic(
        override val toolCall: ToolCall,
        val tier: PermissionTier,
        val description: String
    ) : PendingConfirmation()
}

/**
 * AgentEngine executes the autonomous loop:
 * user intent -> planner -> tool calls -> permission gate -> execution -> verification -> result.
 */
class AgentEngine(
    private val modelRouter: ModelRouter,
    private val toolRegistry: ToolRegistry,
    private val permissionGate: PermissionGate,
    private val contextSelector: ContextSelector
) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _state = MutableStateFlow<AgentState>(AgentState.Idle)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private var activePendingConfirmation: PendingConfirmation? = null

    suspend fun submitUserPrompt(prompt: String, recentLogs: String? = null) {
        if (prompt.isBlank() || _state.value is AgentState.Thinking || _state.value is AgentState.AwaitingConfirmation) {
            return
        }

        // 1. Add user message
        val userMsg = ChatMessage(role = MessageRole.USER, content = prompt)
        appendMessage(userMsg)

        // 2. Select targeted context & assemble system prompt
        val context = contextSelector.selectContext(prompt, recentLogs)
        val systemPrompt = contextSelector.buildSystemPrompt(context)

        // Ensure system prompt is first message
        val current = _messages.value.toMutableList()
        if (current.none { it.role == MessageRole.SYSTEM }) {
            current.add(0, ChatMessage(role = MessageRole.SYSTEM, content = systemPrompt))
            _messages.value = current
        }

        runAgentLoop()
    }

    private suspend fun runAgentLoop(maxTurns: Int = 10) {
        var turns = 0

        while (turns < maxTurns) {
            turns++
            _state.value = AgentState.Thinking

            val toolsJson = toolRegistry.toOpenAiToolsJson()
            val responseResult = modelRouter.routeRequest(_messages.value, toolsJson)

            when (responseResult) {
                is AppResult.Error -> {
                    _state.value = AgentState.Error(responseResult.message)
                    appendMessage(
                        ChatMessage(
                            role = MessageRole.ASSISTANT,
                            content = "⚠️ ${responseResult.message}"
                        )
                    )
                    return
                }
                is AppResult.Success -> {
                    val response = responseResult.data
                    val toolCalls = response.toolCalls

                    // Record assistant message
                    val assistantMsg = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = response.content ?: "",
                        toolCalls = toolCalls
                    )
                    appendMessage(assistantMsg)

                    // If no tool calls, model has completed the turn
                    if (toolCalls.isEmpty()) {
                        _state.value = AgentState.Idle
                        return
                    }

                    // Process first tool call (sequential execution)
                    val primaryToolCall = toolCalls.first()
                    val tool = toolRegistry.getTool(primaryToolCall.name)

                    if (tool == null) {
                        handleToolResult(
                            ToolResult.failure(
                                primaryToolCall.id,
                                primaryToolCall.name,
                                "Unknown tool: '${primaryToolCall.name}'"
                            )
                        )
                        continue
                    }

                    val tier = permissionGate.getTierForTool(primaryToolCall.name)

                    if (tier == PermissionTier.SAFE) {
                        // Auto-execute safe tools
                        executeToolDirectly(tool, primaryToolCall)
                    } else {
                        // Mutating or execution tool -> trigger Permission Gate
                        val confirmation = buildPendingConfirmation(tool, primaryToolCall, tier)
                        activePendingConfirmation = confirmation
                        _state.value = AgentState.AwaitingConfirmation(confirmation)
                        // Pause loop until user confirms or denies
                        return
                    }
                }
            }
        }

        _state.value = AgentState.Idle
    }

    private fun buildPendingConfirmation(
        tool: ITool,
        toolCall: ToolCall,
        tier: PermissionTier
    ): PendingConfirmation {
        val args = toolCall.parseArguments()

        return when (tool.name) {
            "write_file" -> {
                val path = args["path"] ?: "unknown"
                val content = args["content"] ?: args["diff_or_content"] ?: ""
                val diff = if (tool is WriteFileTool) {
                    tool.previewDiff(path, content)
                } else {
                    FileDiff(path, isNewFile = true, emptyList(), 0, 0)
                }
                PendingConfirmation.WriteFile(toolCall, path, diff)
            }
            "run_command" -> {
                val command = args["command"] ?: ""
                val cwd = args["cwd"] ?: "."
                PendingConfirmation.RunCommand(toolCall, command, cwd)
            }
            else -> {
                PendingConfirmation.Generic(toolCall, tier, "Execute ${tool.name}")
            }
        }
    }

    suspend fun resolvePendingConfirmation(approved: Boolean) {
        val pending = activePendingConfirmation ?: return
        activePendingConfirmation = null

        if (approved) {
            val tool = toolRegistry.getTool(pending.toolCall.name)
            if (tool != null) {
                executeToolDirectly(tool, pending.toolCall)
                // Resume loop to allow model to react to tool result
                runAgentLoop()
            } else {
                handleToolResult(
                    ToolResult.failure(pending.toolCall.id, pending.toolCall.name, "Tool not found")
                )
                runAgentLoop()
            }
        } else {
            // User rejected the action
            handleToolResult(
                ToolResult.failure(
                    pending.toolCall.id,
                    pending.toolCall.name,
                    "Action canceled by user."
                )
            )
            runAgentLoop()
        }
    }

    private suspend fun executeToolDirectly(tool: ITool, toolCall: ToolCall) {
        _state.value = AgentState.ExecutingTool(tool.name)
        val args = toolCall.parseArguments()
        val result = tool.execute(args, toolCall.id)
        handleToolResult(result)
    }

    private fun handleToolResult(result: ToolResult) {
        val toolMsg = ChatMessage(
            role = MessageRole.TOOL,
            content = if (result.isSuccess) result.content else "Error: ${result.error ?: result.content}",
            toolCallId = result.toolCallId,
            toolName = result.toolName
        )
        appendMessage(toolMsg)
    }

    private fun appendMessage(msg: ChatMessage) {
        val current = _messages.value.toMutableList()
        current.add(msg)
        _messages.value = current
    }

    fun clearHistory() {
        _messages.value = emptyList()
        _state.value = AgentState.Idle
        activePendingConfirmation = null
    }
}
