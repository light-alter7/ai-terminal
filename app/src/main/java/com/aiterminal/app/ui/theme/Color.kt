package com.aiterminal.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// LoomCode design tokens
// A deep, near-black "void" base with a woven indigo -> violet -> cyan
// accent thread. Named after the product: threads of code, loomed together.
// ---------------------------------------------------------------------------

// Base surfaces (void / graphite scale)
val Void = Color(0xFF07070B)
val VoidElevated = Color(0xFF0C0D14)
val Graphite = Color(0xFF12131C)
val GraphiteHigh = Color(0xFF191B27)
val HairlineLow = Color(0x14FFFFFF)   // 8% white — subtle glass border
val HairlineHigh = Color(0x26FFFFFF)  // 15% white — focused glass border
val GlassFillLow = Color(0x08FFFFFF)  // 3% white glass fill
val GlassFillHigh = Color(0x12FFFFFF) // 7% white glass fill

// Accent thread — the LoomCode signature gradient
val ThreadIndigo = Color(0xFF6366F1)
val ThreadViolet = Color(0xFF8B5CF6)
val ThreadCyan = Color(0xFF22D3EE)
val ThreadIndigoDim = Color(0xFF3730A3)

// Semantic accents
val SignalEmerald = Color(0xFF34D399)
val SignalEmeraldBg = Color(0x1A34D399)
val SignalAmber = Color(0xFFFBBF24)
val SignalAmberBg = Color(0x1AFBBF24)
val SignalRose = Color(0xFFFB7185)
val SignalRoseBg = Color(0x1AFB7185)

// Text
val TextPrimary = Color(0xFFF4F5F7)
val TextSecondary = Color(0xFFA1A3B0)
val TextMuted = Color(0xFF5C5E6C)

// ---- Legacy aliases kept so existing references keep compiling ----
// (Screens are being migrated to the tokens above; these map the old
// "Terminal*" names onto the new LoomCode palette.)
val TerminalBackground = Void
val TerminalSurface = Graphite
val TerminalSurfaceVariant = GraphiteHigh
val TerminalBorder = HairlineLow

val TerminalGreen = SignalEmerald
val TerminalGreenBg = SignalEmeraldBg

val TerminalRed = SignalRose
val TerminalRedBg = SignalRoseBg

val TerminalAmber = SignalAmber
val TerminalBlue = ThreadIndigo
val TerminalPurple = ThreadViolet
