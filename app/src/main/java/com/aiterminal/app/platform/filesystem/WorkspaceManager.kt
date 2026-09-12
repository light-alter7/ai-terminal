package com.aiterminal.app.platform.filesystem

import com.aiterminal.app.core.common.AppResult
import java.io.File

data class WorkspaceEntry(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)

data class SearchMatch(
    val lineNumber: Int,
    val lineContent: String
)

data class SearchResult(
    val relativePath: String,
    val matches: List<SearchMatch>
)

/**
 * WorkspaceManager governs all filesystem operations, restricting them strictly
 * to the app's sandboxed private workspace directory ($FILES/workspace).
 * Path traversal attempts outside this boundary are blocked immediately.
 */
class WorkspaceManager(val workspaceRoot: File) {

    init {
        if (!workspaceRoot.exists()) {
            workspaceRoot.mkdirs()
        }
    }

    /**
     * Resolves and canonicalizes a path relative to workspace root.
     * Throws SecurityException if the resolved file is outside the workspace root.
     */
    fun resolvePath(relativePath: String): File {
        val sanitized = relativePath.trim().removePrefix("/").removePrefix("./")
        val target = if (sanitized.isEmpty() || sanitized == ".") {
            workspaceRoot
        } else {
            File(workspaceRoot, sanitized)
        }

        val canonicalRoot = workspaceRoot.canonicalPath
        val canonicalTarget = target.canonicalPath

        if (!canonicalTarget.startsWith(canonicalRoot)) {
            throw SecurityException("Access denied: Path '$relativePath' traverses outside workspace sandbox '$canonicalRoot'")
        }

        return target
    }

    fun readFile(relativePath: String): AppResult<String> {
        return try {
            val file = resolvePath(relativePath)
            if (!file.exists()) {
                AppResult.Error("File not found: $relativePath")
            } else if (file.isDirectory) {
                AppResult.Error("Cannot read file: '$relativePath' is a directory")
            } else {
                AppResult.Success(file.readText(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to read file", e)
        }
    }

    fun writeFile(relativePath: String, content: String): AppResult<Unit> {
        return try {
            val file = resolvePath(relativePath)
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            file.writeText(content, Charsets.UTF_8)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to write file", e)
        }
    }

    fun listDirectory(relativePath: String = "."): AppResult<List<WorkspaceEntry>> {
        return try {
            val dir = resolvePath(relativePath)
            if (!dir.exists()) {
                return AppResult.Error("Directory not found: $relativePath")
            }
            if (!dir.isDirectory) {
                return AppResult.Error("Path '$relativePath' is not a directory")
            }

            val entries = dir.listFiles()?.map { file ->
                val rel = file.relativeTo(workspaceRoot).path.replace('\\', '/')
                WorkspaceEntry(
                    name = file.name,
                    relativePath = rel,
                    isDirectory = file.isDirectory,
                    sizeBytes = if (file.isFile) file.length() else 0L,
                    lastModified = file.lastModified()
                )
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()

            AppResult.Success(entries)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to list directory", e)
        }
    }

    fun searchFiles(query: String, scope: String = "."): AppResult<List<SearchResult>> {
        return try {
            val targetDir = resolvePath(scope)
            if (!targetDir.exists() || !targetDir.isDirectory) {
                return AppResult.Error("Scope '$scope' is not a valid directory")
            }

            val results = mutableListOf<SearchResult>()
            val queryLower = query.lowercase()

            targetDir.walkTopDown()
                .filter { it.isFile && !it.name.startsWith(".") && it.length() < 1024 * 1024 } // Skip hidden & >1MB files
                .forEach { file ->
                    val relPath = file.relativeTo(workspaceRoot).path.replace('\\', '/')
                    val matches = mutableListOf<SearchMatch>()

                    try {
                        file.useLines { lines ->
                            lines.forEachIndexed { index, line ->
                                if (line.lowercase().contains(queryLower)) {
                                    matches.add(SearchMatch(index + 1, line.trim()))
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore unreadable binary files
                    }

                    if (matches.isNotEmpty()) {
                        results.add(SearchResult(relPath, matches))
                    }
                }

            AppResult.Success(results)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Search failed", e)
        }
    }

    fun getDiffForWrite(relativePath: String, newContent: String): FileDiff {
        val file = resolvePath(relativePath)
        val oldContent = if (file.exists() && file.isFile) file.readText(Charsets.UTF_8) else null
        return DiffCalculator.computeDiff(relativePath, oldContent, newContent)
    }

    fun fileExists(relativePath: String): Boolean {
        return try {
            resolvePath(relativePath).exists()
        } catch (_: Exception) {
            false
        }
    }
}
