package com.aiterminal.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

// A near-black neutral used only for text sitting on top of bright
// gradient fills (buttons, badges).
private val OnAccent = androidx.compose.ui.graphics.Color(0xFF0A0A10)

private val LoomDarkColorScheme = darkColorScheme(
    primary = ThreadIndigo,
    onPrimary = OnAccent,
    secondary = ThreadCyan,
    onSecondary = OnAccent,
    tertiary = ThreadViolet,
    background = Void,
    onBackground = TextPrimary,
    surface = Graphite,
    onSurface = TextPrimary,
    surfaceVariant = GraphiteHigh,
    onSurfaceVariant = TextSecondary,
    outline = HairlineLow,
    error = SignalRose,
    onError = TextPrimary
)

@Composable
fun AiTerminalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoomDarkColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * LoomCode signature gradients — reused across headers, buttons, badges
 * and accent glows so the brand reads consistently everywhere.
 */
object LoomGradients {
    val Thread = Brush.linearGradient(
        colors = listOf(ThreadIndigo, ThreadViolet, ThreadCyan)
    )
    val ThreadSubtle = Brush.linearGradient(
        colors = listOf(ThreadIndigo.copy(alpha = 0.35f), ThreadCyan.copy(alpha = 0.35f))
    )
    val ThreadVertical = Brush.verticalGradient(
        colors = listOf(ThreadIndigo, ThreadViolet)
    )
    val VoidDepth = Brush.verticalGradient(
        colors = listOf(VoidElevated, Void)
    )
    val Glass = Brush.linearGradient(
        colors = listOf(GlassFillHigh, GlassFillLow)
    )
}
