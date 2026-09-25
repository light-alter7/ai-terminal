# LoomCode (📱)

> **An AI-native development environment for your phone.**  
> The phone is the control center, frontier LLMs provide the intelligence, and the embedded app provides the tools and execution layer.

---

## Architecture Overview

```
                     📱 AI TERMINAL
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
    AI Interface      Agent Engine      Workspace
                     (Planner + Memory)
         │                │                │
         └────────────────┼────────────────┘
                          │
                     Model Router (Runtime Swappable: OpenAI / Claude / Gemini)
                          │
                     Permission Gate (Safe / Confirm / Destructive)
                          │
                     Tool-Calling Layer
                          │
    ┌─────────────────────┼─────────────────────┐
    ▼                     ▼                     ▼
Terminal               Files                   MCP
(sandboxed PTY shell,  (scoped workspace,      (V0.3: GitHub, DB,
python, build tools)    diff-first, search)     docs, custom)
```

---

## V0.1 Capabilities

1. **Android App Shell**: Built with modern Kotlin and Jetpack Compose (Material 3 dark terminal aesthetic).
2. **Sandboxed Android shell**: PTY shell execution environment scoped strictly to the application's private files directory (`$FILES/workspace`).
3. **Real Terminal UI**: Interactive terminal emulator view with ANSI colors, monospace typography, scrollback, command history, and mobile-friendly accessory keys (`TAB`, `CTRL-C`, `ESC`, `UP`, `DOWN`).
4. **Scoped Workspace / File Browser**: Interactive file tree scoped to `$FILES/workspace` preventing directory traversal outside sandbox.
5. **BYOK Security (Android Keystore)**: Zero hardcoded keys. Keys are encrypted on-device via `EncryptedSharedPreferences` backed by Android Keystore.
6. **Swappable Model Router**: Configured dynamically via `routing_config.json` and switchable at runtime from Settings.
7. **Permission Gate & Diff-First Editing**:
   - **Tier 1 (Safe - Auto-run)**: `read_file`, `list_directory`, `search_files`
   - **Tier 2 (Confirm-Required - User Tap)**: `write_file` (with line-by-line diff preview), `run_command` (with command and working dir inspection)
   - **Tier 3 (Destructive - Typed Confirmation)**: `delete_file`, `delete_directory`, `reset_repo`
8. **Relevance-Based Context Selection**: Only includes files and logs relevant to the task, avoiding full repository dumps.

---

## V0.1 Tool Contract

- `read_file(path)`: Reads content of a workspace file.
- `write_file(path, content)`: Previews unified line-by-line diff (`+`/`-`), requires user tap, then writes.
- `list_directory(path)`: Lists files and subdirectories.
- `search_files(query, scope)`: Recursively matches text lines across workspace files.
- `run_command(command, cwd)`: Displays exact command & target directory, requires user tap, executes in PTY shell.

## Build and runtime notes

- The repository includes both `gradlew` (Unix/macOS/Linux) and `gradlew.bat`
  (Windows), plus the Gradle wrapper JAR. On a machine with Java 17 and the
  Android SDK installed, use `./gradlew assembleDebug`.
- Launcher resources are included under `app/src/main/res`, so manifest
  resource linking does not depend on generated icons.
- The app uses Android's built-in `/system/bin/sh` for its shell. The source
  archive does not contain a complete Termux/Python distribution; the
  `app/src/main/assets/bootstrap` directory contains only a portable
  environment diagnostic. Add a separately licensed and tested userland
  bundle before enabling Python or package-manager features.
- GitHub Actions builds the debug APK on pushes to `main`, pull requests, and
  manual workflow runs. Download it from the workflow run's
  `ai-terminal-debug-apk` artifact.

---

## Definition of Done Verification

- **Acceptance Flow 1**: "List the files in my project and show me main.py"
  - `list_directory(".")` executes automatically.
  - `read_file("main.py")` executes automatically.
  - File contents are rendered in the agent chat.
- **Acceptance Flow 2**: "Create a hello-world Python script and run it"
  - Agent calls `write_file("hello.py", ...)` -> UI displays Diff card -> User taps "Accept Changes".
  - Agent calls `run_command("python hello.py", ".")` -> UI displays Command card -> User taps "Run Command".
  - Sandboxed shell executes script, captures stdout, and agent summarizes completion.
