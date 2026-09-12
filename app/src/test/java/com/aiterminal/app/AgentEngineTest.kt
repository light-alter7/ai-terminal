package com.aiterminal.app

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.core.security.SecureKeyStore
import com.aiterminal.app.domain.agent.AgentEngine
import com.aiterminal.app.domain.agent.AgentState
import com.aiterminal.app.domain.agent.ContextSelector
import com.aiterminal.app.domain.agent.ModelRouter
import com.aiterminal.app.domain.agent.PendingConfirmation
import com.aiterminal.app.domain.agent.providers.ILlmProvider
import com.aiterminal.app.domain.agent.providers.LlmResponse
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.ModelRoutingConfig
import com.aiterminal.app.domain.model.PermissionTier
import com.aiterminal.app.domain.model.ProviderConfig
import com.aiterminal.app.domain.model.ToolCall
import com.aiterminal.app.domain.tools.ListDirectoryTool
import com.aiterminal.app.domain.tools.ReadFileTool
import com.aiterminal.app.domain.tools.RunCommandTool
import com.aiterminal.app.domain.tools.SearchFilesTool
import com.aiterminal.app.domain.tools.ToolRegistry
import com.aiterminal.app.domain.tools.WriteFileTool
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import com.aiterminal.app.platform.terminal.PtyProcessRunner
import com.aiterminal.app.platform.terminal.TerminalManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AgentEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var workspaceRoot: File
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var terminalManager: TerminalManager
    private lateinit var toolRegistry: ToolRegistry
    private lateinit var permissionGate: PermissionGate
    private lateinit var contextSelector: ContextSelector

    // Mock LLM Provider
    class MockLlmProvider(
        private val responses: MutableList<LlmResponse>
    ) : ILlmProvider {
        override val providerId: String = "openai"

        override suspend fun generateResponse(
            messages: List<ChatMessage>,
            toolsJson: String,
            apiKey: String,
            config: ProviderConfig
        ): AppResult<LlmResponse> {
            if (responses.isEmpty()) {
                return AppResult.Success(LlmResponse(content = "Task completed successfully."))
            }
            return AppResult.Success(responses.removeAt(0))
        }
    }

    @Before
    fun setup() {
        workspaceRoot = tempFolder.newFolder("agent_workspace")
        workspaceManager = WorkspaceManager(workspaceRoot)
        val runner = PtyProcessRunner()
        terminalManager = TerminalManager(runner, workspaceRoot)
        permissionGate = PermissionGate()

        toolRegistry = ToolRegistry(permissionGate).apply {
            registerTool(ReadFileTool(workspaceManager), PermissionTier.SAFE)
            registerTool(ListDirectoryTool(workspaceManager), PermissionTier.SAFE)
            registerTool(SearchFilesTool(workspaceManager), PermissionTier.SAFE)
            registerTool(WriteFileTool(workspaceManager), PermissionTier.CONFIRM_REQUIRED)
            registerTool(RunCommandTool(terminalManager, workspaceManager), PermissionTier.CONFIRM_REQUIRED)
        }

        contextSelector = ContextSelector(workspaceManager)
    }

    @Test
    fun testAgentLoopWithPermissionGateConfirmation() = runTest {
        // Prepare mock sequence:
        // 1. Model asks to write "hello.py"
        // 2. Model asks to run "echo Hello_From_Python"
        // 3. Model gives final answer
        val mockResponses = mutableListOf(
            LlmResponse(
                content = "I will create hello.py now.",
                toolCalls = listOf(
                    ToolCall(
                        id = "tc_1",
                        name = "write_file",
                        argumentsJson = """{"path":"hello.py","content":"print('Hello_From_Python')"}"""
                    )
                )
            ),
            LlmResponse(
                content = "Now I will execute hello.py.",
                toolCalls = listOf(
                    ToolCall(
                        id = "tc_2",
                        name = "run_command",
                        argumentsJson = """{"command":"echo Hello_From_Python","cwd":"."}"""
                    )
                )
            ),
            LlmResponse(
                content = "Execution finished with output: Hello_From_Python"
            )
        )

        val mockProvider = MockLlmProvider(mockResponses)
        val testConfig = ModelRoutingConfig(
            activeProvider = "openai",
            providers = mapOf(
                "openai" to ProviderConfig(
                    name = "OpenAI",
                    model = "gpt-4o-mini",
                    baseUrl = "https://api.openai.com/v1"
                )
            )
        )

        val fakePrefs = FakeSharedPreferences()
        val secureKeyStore = SecureKeyStore(context = null, customPrefs = fakePrefs)
        secureKeyStore.saveApiKey("openai", "mock-test-key")

        val router = ModelRouter(
            secureKeyStore = secureKeyStore,
            providers = mapOf("openai" to mockProvider),
            initialConfig = testConfig
        )

        val agentEngine = AgentEngine(
            modelRouter = router,
            toolRegistry = toolRegistry,
            permissionGate = permissionGate,
            contextSelector = contextSelector
        )

        // Step 1: User submits prompt
        agentEngine.submitUserPrompt("Create a hello-world Python script and run it")

        // Agent should be paused awaiting confirmation for write_file
        assertTrue(agentEngine.state.value is AgentState.AwaitingConfirmation)
        val pending1 = (agentEngine.state.value as AgentState.AwaitingConfirmation).confirmation
        assertTrue(pending1 is PendingConfirmation.WriteFile)
        assertEquals("hello.py", (pending1 as PendingConfirmation.WriteFile).path)

        // Step 2: User taps "Accept" for write_file
        agentEngine.resolvePendingConfirmation(approved = true)

        // Verify file was written
        assertTrue(workspaceManager.fileExists("hello.py"))

        // Agent should now be paused awaiting confirmation for run_command
        assertTrue(agentEngine.state.value is AgentState.AwaitingConfirmation)
        val pending2 = (agentEngine.state.value as AgentState.AwaitingConfirmation).confirmation
        assertTrue(pending2 is PendingConfirmation.RunCommand)
        assertEquals("echo Hello_From_Python", (pending2 as PendingConfirmation.RunCommand).command)

        // Step 3: User taps "Run Command"
        agentEngine.resolvePendingConfirmation(approved = true)

        // Agent finishes loop and returns to Idle with final assistant message
        assertTrue(agentEngine.state.value is AgentState.Idle)
        val lastMessage = agentEngine.messages.value.last()
        assertEquals("Execution finished with output: Hello_From_Python", lastMessage.content)
    }

    @Test
    fun testUserRejectsPendingWriteFile() = runTest {
        val mockResponses = mutableListOf(
            LlmResponse(
                content = "I will write hello.py.",
                toolCalls = listOf(
                    ToolCall(
                        id = "tc_reject",
                        name = "write_file",
                        argumentsJson = """{"path":"hello.py","content":"secret"}"""
                    )
                )
            ),
            LlmResponse(
                content = "Understood, I will not modify the file."
            )
        )

        val mockProvider = MockLlmProvider(mockResponses)
        val testConfig = ModelRoutingConfig(
            activeProvider = "openai",
            providers = mapOf(
                "openai" to ProviderConfig(
                    name = "OpenAI",
                    model = "gpt-4o-mini",
                    baseUrl = "https://api.openai.com/v1"
                )
            )
        )

        val fakePrefs = FakeSharedPreferences()
        val secureKeyStore = SecureKeyStore(context = null, customPrefs = fakePrefs)
        secureKeyStore.saveApiKey("openai", "mock-test-key")

        val router = ModelRouter(
            secureKeyStore = secureKeyStore,
            providers = mapOf("openai" to mockProvider),
            initialConfig = testConfig
        )

        val agentEngine = AgentEngine(
            modelRouter = router,
            toolRegistry = toolRegistry,
            permissionGate = permissionGate,
            contextSelector = contextSelector
        )

        agentEngine.submitUserPrompt("Write a secret to hello.py")
        assertTrue(agentEngine.state.value is AgentState.AwaitingConfirmation)

        // User taps "Reject"
        agentEngine.resolvePendingConfirmation(approved = false)

        // File must NOT exist
        assertTrue(!workspaceManager.fileExists("hello.py"))

        // Agent should be back to Idle with cancellation noted
        assertTrue(agentEngine.state.value is AgentState.Idle)
        assertEquals("Understood, I will not modify the file.", agentEngine.messages.value.last().content)
    }
}
