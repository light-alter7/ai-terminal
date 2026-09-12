package com.aiterminal.app

import com.aiterminal.app.domain.agent.ContextSelector
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ContextSelectorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var workspaceRoot: File
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var contextSelector: ContextSelector

    @Before
    fun setup() {
        workspaceRoot = tempFolder.newFolder("context_workspace")
        workspaceManager = WorkspaceManager(workspaceRoot)
        contextSelector = ContextSelector(workspaceManager)
    }

    @Test
    fun testContextSelectionExtractsMentionedFile() {
        workspaceManager.writeFile("main.py", "def main():\n    print('Inside main')")
        workspaceManager.writeFile("other.py", "secret_unrelated = 42")

        val context = contextSelector.selectContext(
            userPrompt = "list the files in my project and show me main.py",
            recentLogs = null
        )

        // It should include snippet for main.py because it was mentioned
        assertTrue(context.relevantFileSnippets.containsKey("main.py"))
        assertTrue(context.relevantFileSnippets["main.py"]!!.contains("Inside main"))

        // It should NOT include unrelated other.py
        assertFalse(context.relevantFileSnippets.containsKey("other.py"))

        val systemPrompt = contextSelector.buildSystemPrompt(context)
        assertTrue(systemPrompt.contains("main.py"))
        assertTrue(systemPrompt.contains("AI Terminal"))
    }
}
