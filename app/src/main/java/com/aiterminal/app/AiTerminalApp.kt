package com.aiterminal.app

import android.app.Application
import android.util.Log
import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.core.security.SecureKeyStore
import com.aiterminal.app.domain.agent.AgentEngine
import com.aiterminal.app.domain.agent.ContextSelector
import com.aiterminal.app.domain.agent.ModelRouter
import com.aiterminal.app.domain.agent.providers.ClaudeProvider
import com.aiterminal.app.domain.agent.providers.OpenAiProvider
import com.aiterminal.app.domain.model.PermissionTier
import com.aiterminal.app.domain.tools.ListDirectoryTool
import com.aiterminal.app.domain.tools.ReadFileTool
import com.aiterminal.app.domain.tools.RunCommandTool
import com.aiterminal.app.domain.tools.SearchFilesTool
import com.aiterminal.app.domain.tools.ToolRegistry
import com.aiterminal.app.domain.tools.WriteFileTool
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import com.aiterminal.app.platform.terminal.EnvironmentBootstrapper
import com.aiterminal.app.platform.terminal.PtyProcessRunner
import com.aiterminal.app.platform.terminal.TerminalManager

class AiTerminalApp : Application() {

    var startupError: Throwable? = null
        private set

    lateinit var secureKeyStore: SecureKeyStore
        private set
    lateinit var workspaceManager: WorkspaceManager
        private set
    lateinit var terminalManager: TerminalManager
        private set
    lateinit var permissionGate: PermissionGate
        private set
    lateinit var toolRegistry: ToolRegistry
        private set
    lateinit var modelRouter: ModelRouter
        private set
    lateinit var contextSelector: ContextSelector
        private set
    lateinit var agentEngine: AgentEngine
        private set

    override fun onCreate() {
        super.onCreate()

        try {
            // 1. Initialize bootstrapper & environment
            val bootstrapper = EnvironmentBootstrapper(this)
            val env = bootstrapper.initializeEnvironment()

            // 2. Initialize workspace manager & terminal manager
            workspaceManager = WorkspaceManager(bootstrapper.workspaceDir)
            val processRunner = PtyProcessRunner(env)
            terminalManager = TerminalManager(processRunner, bootstrapper.workspaceDir)

            // 3. Security & Permission gate
            secureKeyStore = SecureKeyStore(this)
            permissionGate = PermissionGate()

            // 4. Register V0.1 tools with strict permission tiers
            toolRegistry = ToolRegistry(permissionGate).apply {
                registerTool(ReadFileTool(workspaceManager), PermissionTier.SAFE)
                registerTool(ListDirectoryTool(workspaceManager), PermissionTier.SAFE)
                registerTool(SearchFilesTool(workspaceManager), PermissionTier.SAFE)
                registerTool(WriteFileTool(workspaceManager), PermissionTier.CONFIRM_REQUIRED)
                registerTool(RunCommandTool(terminalManager, workspaceManager), PermissionTier.CONFIRM_REQUIRED)
            }

            // 5. Load model router from routing_config.json
            val configJson = readAssetJson("routing_config.json")
            val providers = mapOf(
                "openai" to OpenAiProvider(),
                "claude" to ClaudeProvider()
            )
            modelRouter = ModelRouter.fromConfigJson(configJson, secureKeyStore, providers)

            // 6. Context selector and autonomous agent engine
            contextSelector = ContextSelector(workspaceManager)
            agentEngine = AgentEngine(
                modelRouter = modelRouter,
                toolRegistry = toolRegistry,
                permissionGate = permissionGate,
                contextSelector = contextSelector
            )
        } catch (error: Throwable) {
            startupError = error
            Log.e(TAG, "AI Terminal failed during application startup", error)
        }
    }

    private fun readAssetJson(fileName: String): String {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "{}"
        }
    }

    companion object {
        private const val TAG = "AiTerminalApp"
    }
}
