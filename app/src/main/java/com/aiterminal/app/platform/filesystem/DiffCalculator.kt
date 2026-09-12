package com.aiterminal.app.platform.filesystem

enum class DiffLineType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

data class FileDiff(
    val path: String,
    val isNewFile: Boolean,
    val lines: List<DiffLine>,
    val addedCount: Int,
    val removedCount: Int
)

/**
 * Calculates line-by-line diffs between existing file content and new target content.
 * Enables the diff-first preview requirement before any write_file is executed.
 */
object DiffCalculator {

    fun computeDiff(path: String, oldContent: String?, newContent: String): FileDiff {
        if (oldContent == null) {
            val newLines = newContent.lines()
            val diffLines = newLines.mapIndexed { index, line ->
                DiffLine(
                    type = DiffLineType.ADDED,
                    text = line,
                    oldLineNumber = null,
                    newLineNumber = index + 1
                )
            }
            return FileDiff(
                path = path,
                isNewFile = true,
                lines = diffLines,
                addedCount = newLines.size,
                removedCount = 0
            )
        }

        val oldLines = oldContent.lines()
        val newLines = newContent.lines()

        // Compute Longest Common Subsequence (LCS) matrix
        val m = oldLines.size
        val n = newLines.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0 until m) {
            for (j in 0 until n) {
                if (oldLines[i] == newLines[j]) {
                    dp[i + 1][j + 1] = dp[i][j] + 1
                } else {
                    dp[i + 1][j + 1] = maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        // Backtrack to build diff lines
        val result = mutableListOf<DiffLine>()
        var i = m
        var j = n

        val reverseDiff = mutableListOf<DiffLine>()
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1] == newLines[j - 1]) {
                reverseDiff.add(
                    DiffLine(
                        type = DiffLineType.UNCHANGED,
                        text = oldLines[i - 1],
                        oldLineNumber = i,
                        newLineNumber = j
                    )
                )
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                reverseDiff.add(
                    DiffLine(
                        type = DiffLineType.ADDED,
                        text = newLines[j - 1],
                        oldLineNumber = null,
                        newLineNumber = j
                    )
                )
                j--
            } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
                reverseDiff.add(
                    DiffLine(
                        type = DiffLineType.REMOVED,
                        text = oldLines[i - 1],
                        oldLineNumber = i,
                        newLineNumber = null
                    )
                )
                i--
            }
        }

        reverseDiff.reverse()

        val added = reverseDiff.count { it.type == DiffLineType.ADDED }
        val removed = reverseDiff.count { it.type == DiffLineType.REMOVED }

        return FileDiff(
            path = path,
            isNewFile = false,
            lines = reverseDiff,
            addedCount = added,
            removedCount = removed
        )
    }
}
