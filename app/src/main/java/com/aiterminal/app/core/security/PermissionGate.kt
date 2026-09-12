package com.aiterminal.app.core.security

import com.aiterminal.app.domain.model.PermissionTier

/**
 * PermissionGate mediates every tool call emitted by an LLM agent.
 * Mutating or execution calls must be confirmed by the human in the loop.
 */
class PermissionGate(
    private val toolTiers: MutableMap<String, PermissionTier> = defaultToolTiers().toMutableMap()
) {

    /**
     * Look up the tier for a given tool name.
     * If tool is unknown, defaults to CONFIRM_REQUIRED for safety.
     */
    fun getTierForTool(toolName: String): PermissionTier {
        return toolTiers[toolName] ?: PermissionTier.CONFIRM_REQUIRED
    }

    /**
     * Register or update a tool's permission tier.
     * Whenever a new tool is introduced, it must be registered here.
     */
    fun registerToolTier(toolName: String, tier: PermissionTier) {
        toolTiers[toolName] = tier
    }

    fun isSafe(toolName: String): Boolean {
        return getTierForTool(toolName) == PermissionTier.SAFE
    }

    fun requiresConfirmation(toolName: String): Boolean {
        return getTierForTool(toolName) == PermissionTier.CONFIRM_REQUIRED
    }

    fun isDestructive(toolName: String): Boolean {
        return getTierForTool(toolName) == PermissionTier.DESTRUCTIVE
    }

    companion object {
        fun defaultToolTiers(): Map<String, PermissionTier> = mapOf(
            // Safe tier (auto-run)
            "read_file" to PermissionTier.SAFE,
            "list_directory" to PermissionTier.SAFE,
            "search_files" to PermissionTier.SAFE,
            "git_status" to PermissionTier.SAFE,

            // Confirm-required tier (tap confirmation required)
            "write_file" to PermissionTier.CONFIRM_REQUIRED,
            "run_command" to PermissionTier.CONFIRM_REQUIRED,
            "install_package" to PermissionTier.CONFIRM_REQUIRED,
            "git_commit" to PermissionTier.CONFIRM_REQUIRED,
            "git_push" to PermissionTier.CONFIRM_REQUIRED,

            // Destructive tier (typed confirmation required)
            "delete_file" to PermissionTier.DESTRUCTIVE,
            "delete_directory" to PermissionTier.DESTRUCTIVE,
            "reset_repo" to PermissionTier.DESTRUCTIVE
        )
    }
}
