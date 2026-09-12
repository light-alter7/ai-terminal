package com.aiterminal.app.platform.terminal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class CommandOutput(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    val isSuccess: Boolean get() = exitCode == 0
    val combinedOutput: String get() = if (stderr.isBlank()) stdout else "$stdout\n$stderr".trim()
}

class PtyProcessRunner(
    private val defaultEnvironment: Map<String, String> = emptyMap()
) {

    suspend fun runCommand(
        command: String,
        workingDir: File,
        timeoutSeconds: Long = 60L,
        onOutputChunk: ((String) -> Unit)? = null
    ): CommandOutput = withContext(Dispatchers.IO) {
        val isWindows = System.getProperty("os.name")?.lowercase()?.contains("win") == true
        val isAndroid = File("/system/bin/sh").exists()

        val processBuilder = when {
            isAndroid -> {
                ProcessBuilder("/system/bin/sh", "-c", command)
            }
            isWindows -> {
                ProcessBuilder("cmd.exe", "/c", command)
            }
            else -> {
                ProcessBuilder("/bin/sh", "-c", command)
            }
        }

        processBuilder.directory(workingDir)
        
        // Merge environment
        val env = processBuilder.environment()
        defaultEnvironment.forEach { (k, v) -> env[k] = v }

        val stdoutBuffer = StringBuilder()
        val stderrBuffer = StringBuilder()

        try {
            val process = processBuilder.start()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            // Read stdout
            val stdoutThread = Thread {
                try {
                    val buffer = CharArray(1024)
                    var read: Int
                    while (stdoutReader.read(buffer).also { read = it } != -1) {
                        val text = String(buffer, 0, read)
                        stdoutBuffer.append(text)
                        onOutputChunk?.invoke(text)
                    }
                } catch (_: Exception) {}
            }

            // Read stderr
            val stderrThread = Thread {
                try {
                    val buffer = CharArray(1024)
                    var read: Int
                    while (stderrReader.read(buffer).also { read = it } != -1) {
                        val text = String(buffer, 0, read)
                        stderrBuffer.append(text)
                        onOutputChunk?.invoke(text)
                    }
                } catch (_: Exception) {}
            }

            stdoutThread.start()
            stderrThread.start()

            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return@withContext CommandOutput(
                    stdout = stdoutBuffer.toString(),
                    stderr = "Command timed out after $timeoutSeconds seconds",
                    exitCode = -1
                )
            }

            stdoutThread.join(2000)
            stderrThread.join(2000)

            CommandOutput(
                stdout = stdoutBuffer.toString(),
                stderr = stderrBuffer.toString(),
                exitCode = process.exitValue()
            )
        } catch (e: Exception) {
            CommandOutput(
                stdout = "",
                stderr = e.message ?: "Failed to execute command",
                exitCode = 1
            )
        }
    }
}
