package com.aiterminal.app.domain.agent

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.platform.filesystem.WorkspaceManager

data class SelectedContext(
    val directoryOverview: String,
    val relevantFileSnippets: Map<String, String>,
    val recentLogs: String? = null
)

/**
 * ContextSelector intelligently chooses relevant files and summaries instead of full-repo dumping.
 * Extracts keywords, inspects mentioned file names in errors/prompts, and includes targeted snippets.
 */
class ContextSelector(
    private val workspaceManager: WorkspaceManager
) {

    private val filePattern = Regex("""\b[\w-]+\.(?:py|kt|java|js|ts|json|md|txt|sh|html|css|xml|gradle)\b""", RegexOption.IGNORE_CASE)

    fun selectContext(userPrompt: String, recentLogs: String? = null): SelectedContext {
        // 1. Get top-level directory structure
        val directoryOverview = when (val listing = workspaceManager.listDirectory(".")) {
            is AppResult.Success -> {
                listing.data.take(20).joinToString(", ") { entry ->
                    if (entry.isDirectory) "${entry.name}/" else entry.name
                }
            }
            is AppResult.Error -> "(empty or uninitialized)"
        }

        // 2. Identify candidate file names from user prompt and recent logs
        val candidateFiles = mutableSetOf<String>()
        filePattern.findAll(userPrompt).forEach { match ->
            candidateFiles.add(match.value)
        }
        if (recentLogs != null) {
            filePattern.findAll(recentLogs).forEach { match ->
                candidateFiles.add(match.value)
            }
        }

        // 3. Collect snippets for explicitly mentioned existing files (cap to 5 files, 200 lines each)
        val snippets = mutableMapOf<String, String>()
        for (fileName in candidateFiles) {
            if (workspaceManager.fileExists(fileName)) {
                when (val contentResult = workspaceManager.readFile(fileName)) {
                    is AppResult.Success -> {
                        val lines = contentResult.data.lines()
                        val preview = lines.take(200).joinToString("\n")
                        snippets[fileName] = preview
                    }
                    is AppResult.Error -> {}
                }
            }
        }

        return SelectedContext(
            directoryOverview = directoryOverview,
            relevantFileSnippets = snippets,
            recentLogs = recentLogs?.takeLast(1000)
        )
    }

    fun buildSystemPrompt(context: SelectedContext): String {
        val sb = StringBuilder()
        sb.appendLine("You are AI Terminal, an autonomous AI-native development environment on Android.")
        sb.appendLine("You are operating directly inside a sandboxed workspace on the user's mobile device.")
        sb.appendLine()
        sb.appendLine("Available files in workspace root: [${context.directoryOverview}]")
        
        if (context.relevantFileSnippets.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Relevant workspace files:")
            context.relevantFileSnippets.forEach { (path, content) ->
                sb.appendLine("--- $path ---")
                sb.appendLine(content)
                sb.appendLine("--- end of $path ---")
            }
        }

        if (!context.recentLogs.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("Recent shell output:")
            sb.appendLine(context.recentLogs)
        }

        sb.appendLine()
        sb.appendLine("Guidelines:")
        sb.appendLine("1. You must execute real actions using your available tools: read_file, write_file, list_directory, search_files, run_command.")
        sb.appendLine("2. Never simulate or invent command output. Call run_command to execute commands in the shell.")
        sb.appendLine("3. Mutating tools (write_file, run_command) require human confirmation before execution.")
        sb.appendLine("4. When asked to create or modify code, use write_file. When asked to run, test, or verify, use run_command.")
        sb.appendLine("5. Keep explanations concise and mobile-friendly.")

        return sb.toString().trim()
    }
}
