package com.aiterminal.app.platform.terminal

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class EnvironmentBootstrapper(private val context: Context) {

    val filesDir: File = context.filesDir
    val workspaceDir: File = File(filesDir, "workspace")
    val usrDir: File = File(filesDir, "usr")
    val binDir: File = File(usrDir, "bin")
    val tmpDir: File = File(filesDir, "tmp")

    fun initializeEnvironment(): Map<String, String> {
        if (!workspaceDir.exists()) workspaceDir.mkdirs()
        if (!binDir.exists()) binDir.mkdirs()
        if (!tmpDir.exists()) tmpDir.mkdirs()

        // Unpack bundled scripts/assets if present
        copyAssetsToBin()

        return getEnvironmentVariables()
    }

    private fun copyAssetsToBin() {
        try {
            val assetManager = context.assets
            val bootstrapFiles = assetManager.list("bootstrap") ?: return
            for (fileName in bootstrapFiles) {
                val destFile = File(binDir, fileName)
                val assetPath = "bootstrap/$fileName"

                // Assets are versioned with the app. Re-copy when the bundled
                // file changes instead of leaving an older helper in place.
                val shouldCopy = !destFile.exists() ||
                    destFile.length() != assetManager.open(assetPath).use { it.available().toLong() }

                if (shouldCopy) {
                    assetManager.open(assetPath).use { input ->
                        FileOutputStream(destFile).use { output -> input.copyTo(output) }
                    }
                }

                destFile.setExecutable(true, false)
                destFile.setReadable(true, false)
            }
        } catch (_: Exception) {
            // A shell-only Android install remains usable when optional
            // bootstrap helpers are unavailable.
        }
    }

    fun getEnvironmentVariables(): Map<String, String> {
        val path = "${binDir.absolutePath}:/system/bin:/system/xbin"
        val environment = mutableMapOf(
            "HOME" to workspaceDir.absolutePath,
            "PATH" to path,
            "TMPDIR" to tmpDir.absolutePath,
            "PREFIX" to usrDir.absolutePath,
            "TERM" to "xterm-256color"
        )

        // Do not point an Android shell at a nonexistent Python installation.
        // A future userland bundle can opt in simply by shipping python in usr/bin.
        val pythonBinary = File(binDir, "python")
        val python3Binary = File(binDir, "python3")
        if (pythonBinary.exists() || python3Binary.exists()) {
            environment["PYTHONHOME"] = usrDir.absolutePath
            environment["PYTHONPATH"] =
                "${usrDir.absolutePath}/lib/python3.11:${usrDir.absolutePath}/lib"
        }

        return environment
    }
}
