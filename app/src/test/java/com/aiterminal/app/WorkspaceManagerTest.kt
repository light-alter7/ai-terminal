package com.aiterminal.app

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WorkspaceManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var workspaceRoot: File
    private lateinit var workspaceManager: WorkspaceManager

    @Before
    fun setup() {
        workspaceRoot = tempFolder.newFolder("workspace")
        workspaceManager = WorkspaceManager(workspaceRoot)
    }

    @Test
    fun testWriteAndReadFile() {
        val writeResult = workspaceManager.writeFile("main.py", "print('hello from test')")
        assertTrue(writeResult.isSuccess)

        val readResult = workspaceManager.readFile("main.py")
        assertTrue(readResult.isSuccess)
        assertEquals("print('hello from test')", (readResult as AppResult.Success).data)
    }

    @Test(expected = SecurityException::class)
    fun testDirectoryTraversalBlocked() {
        // Attempting to read outside workspace sandbox must throw SecurityException
        workspaceManager.resolvePath("../outside.secret")
    }

    @Test
    fun testListDirectory() {
        workspaceManager.writeFile("a.txt", "content a")
        workspaceManager.writeFile("sub/b.txt", "content b")

        val listResult = workspaceManager.listDirectory(".")
        assertTrue(listResult.isSuccess)
        val entries = (listResult as AppResult.Success).data

        assertTrue(entries.any { it.name == "a.txt" && !it.isDirectory })
        assertTrue(entries.any { it.name == "sub" && it.isDirectory })
    }

    @Test
    fun testSearchFiles() {
        workspaceManager.writeFile("hello.py", "def say_hello():\n    return 'world'")
        workspaceManager.writeFile("notes.txt", "Remember to test the agent loop")

        val searchResult = workspaceManager.searchFiles("say_hello")
        assertTrue(searchResult.isSuccess)
        val matches = (searchResult as AppResult.Success).data

        assertEquals(1, matches.size)
        assertEquals("hello.py", matches[0].relativePath)
        assertEquals(1, matches[0].matches[0].lineNumber)
    }
}
