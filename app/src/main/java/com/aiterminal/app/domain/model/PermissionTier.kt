package com.aiterminal.app.domain.model

enum class PermissionTier {
    /**
     * Tier 1: Safe
     * Auto-run without user intervention.
     * Examples: read_file, list_directory, search_files, git_status
     */
    SAFE,

    /**
     * Tier 2: Confirm-Required
     * Requires an explicit user confirmation tap before executing.
     * For write_file, shows a diff-first view (added/removed lines).
     * Examples: write_file, run_command, install_package, git_commit, git_push
     */
    CONFIRM_REQUIRED,

    /**
     * Tier 3: Destructive
     * Requires typed confirmation (user must type the target name or phrase) before execution.
     * Examples: delete_file, delete_directory, reset_repo
     */
    DESTRUCTIVE
}
