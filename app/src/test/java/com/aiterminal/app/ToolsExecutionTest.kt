package com.aiterminal.app

import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.domain.model.PermissionTier
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

class ToolsExecutionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var workspaceRoot: File
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var terminalManager: TerminalManager
    private lateinit var toolRegistry: ToolRegistry

    @Before
    fun setup() {
        workspaceRoot = tempFolder.newFolder("test_workspace")
        workspaceManager = WorkspaceManager(workspaceRoot)
        val runner = PtyProcessRunner()
        terminalManager = TerminalManager(runner, workspaceRoot)

        val gate = PermissionGate()
        toolRegistry = ToolRegistry(gate).apply {
            registerTool(ReadFileTool(workspaceManager), PermissionTier.SAFE)
            registerTool(WriteFileTool(workspaceManager), PermissionTier.CONFIRM_REQUIRED)
            registerTool(ListDirectoryTool(workspaceManager), PermissionTier.SAFE)
            registerTool(SearchFilesTool(workspaceManager), PermissionTier.SAFE)
            registerTool(RunCommandTool(terminalManager, workspaceManager), PermissionTier.CONFIRM_REQUIRED)
        }
    }

    @Test
    fun testWriteAndReadFileTools() = runTest {
        val writeTool = toolRegistry.getTool("write_file")!!
        val writeRes = writeTool.execute(
            mapOf("path" to "test.py", "content" to "print('Tool Test')"),
            "call_1"
        )
        assertTrue(writeRes.isSuccess)

        val readTool = toolRegistry.getTool("read_file")!!
        val readRes = readTool.execute(mapOf("path" to "test.py"), "call_2")
        assertTrue(readRes.isSuccess)
        assertEquals("print('Tool Test')", readRes.content)
    }

    @Test
    fun testListDirectoryTool() = runTest {
        workspaceManager.writeFile("sample.txt", "sample")
        val listTool = toolRegistry.getTool("list_directory")!!
        val listRes = listTool.execute(mapOf("path" to "."), "call_3")
        assertTrue(listRes.isSuccess)
        assertTrue(listRes.content.contains("sample.txt"))
    }

    @Test
    fun testSearchFilesTool() = runTest {
        workspaceManager.writeFile("hello.txt", "find_me_needle")
        val searchTool = toolRegistry.getTool("search_files")!!
        val searchRes = searchTool.execute(mapOf("query" to "needle"), "call_4")
        assertTrue(searchRes.isSuccess)
        assertTrue(searchRes.content.contains("hello.txt"))
        assertTrue(searchRes.content.contains("find_me_needle"))
    }

    @Test
    fun testRunCommandToolEcho() = runTest {
        val runTool = toolRegistry.getTool("run_command")!!
        val runRes = runTool.execute(mapOf("command" to "echo hello_runner"), "call_5")
        assertTrue(runRes.isSuccess)
        assertTrue(runRes.content.contains("hello_runner"))
    }
}
