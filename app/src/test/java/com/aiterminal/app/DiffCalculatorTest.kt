package com.aiterminal.app

import com.aiterminal.app.platform.filesystem.DiffCalculator
import com.aiterminal.app.platform.filesystem.DiffLineType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffCalculatorTest {

    @Test
    fun testNewFileDiff() {
        val content = "print('Hello, world!')\nprint('Line 2')"
        val diff = DiffCalculator.computeDiff("hello.py", null, content)

        assertTrue(diff.isNewFile)
        assertEquals(2, diff.addedCount)
        assertEquals(0, diff.removedCount)
        assertEquals(2, diff.lines.size)
        assertTrue(diff.lines.all { it.type == DiffLineType.ADDED })
    }

    @Test
    fun testModifiedFileDiff() {
        val oldContent = "line 1\nline 2\nline 3"
        val newContent = "line 1\nmodified line 2\nline 3\nline 4"
        val diff = DiffCalculator.computeDiff("sample.txt", oldContent, newContent)

        assertEquals("sample.txt", diff.path)
        assertEquals(false, diff.isNewFile)
        assertTrue(diff.addedCount >= 2) // "modified line 2" and "line 4"
        assertTrue(diff.removedCount >= 1) // "line 2"
    }

    @Test
    fun testUnchangedFileDiff() {
        val content = "constant content\nsecond line"
        val diff = DiffCalculator.computeDiff("const.txt", content, content)

        assertEquals(0, diff.addedCount)
        assertEquals(0, diff.removedCount)
        assertTrue(diff.lines.all { it.type == DiffLineType.UNCHANGED })
    }
}
