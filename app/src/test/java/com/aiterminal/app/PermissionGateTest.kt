package com.aiterminal.app

import com.aiterminal.app.core.security.PermissionGate
import com.aiterminal.app.domain.model.PermissionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionGateTest {

    private val permissionGate = PermissionGate()

    @Test
    fun testSafeTools() {
        assertEquals(PermissionTier.SAFE, permissionGate.getTierForTool("read_file"))
        assertEquals(PermissionTier.SAFE, permissionGate.getTierForTool("list_directory"))
        assertEquals(PermissionTier.SAFE, permissionGate.getTierForTool("search_files"))
        assertTrue(permissionGate.isSafe("read_file"))
    }

    @Test
    fun testConfirmRequiredTools() {
        assertEquals(PermissionTier.CONFIRM_REQUIRED, permissionGate.getTierForTool("write_file"))
        assertEquals(PermissionTier.CONFIRM_REQUIRED, permissionGate.getTierForTool("run_command"))
        assertTrue(permissionGate.requiresConfirmation("write_file"))
        assertTrue(permissionGate.requiresConfirmation("run_command"))
        assertFalse(permissionGate.isSafe("write_file"))
    }

    @Test
    fun testDestructiveTools() {
        assertEquals(PermissionTier.DESTRUCTIVE, permissionGate.getTierForTool("delete_file"))
        assertEquals(PermissionTier.DESTRUCTIVE, permissionGate.getTierForTool("delete_directory"))
        assertTrue(permissionGate.isDestructive("delete_file"))
    }

    @Test
    fun testUnknownToolDefaultsToConfirmRequired() {
        assertEquals(PermissionTier.CONFIRM_REQUIRED, permissionGate.getTierForTool("unknown_risky_tool"))
    }
}
