package com.aiterminal.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LoomCode's "liquid glass" surface: a translucent fill over the void
 * background, a hairline border with a slight sheen, and a soft rounded
 * corner. Used for every panel, card and bar in the app so the whole UI
 * shares one consistent material language.
 */
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(18.dp),
    borderColor: Color = HairlineLow,
    fill: Color = GlassFillLow
): Modifier = this
    .clip(shape)
    .background(fill)
    .border(1.dp, borderColor, shape)

fun Modifier.glassSurfaceElevated(
    shape: Shape = RoundedCornerShape(18.dp)
): Modifier = this
    .clip(shape)
    .background(GraphiteHigh.copy(alpha = 0.92f))
    .border(1.dp, HairlineHigh, shape)

/** A thin glowing hairline used to cap headers / bars with the brand thread. */
fun Modifier.threadUnderline(): Modifier = this
    .background(LoomGradients.ThreadSubtle)

/** Rounded pill shape shared by chips, buttons and the nav bar. */
val PillShape = RoundedCornerShape(28.dp)
val CardShape = RoundedCornerShape(18.dp)
val CardShapeSmall = RoundedCornerShape(12.dp)

@Composable
fun GradientText(
    text: String,
    style: TextStyle,
    brush: Brush = LoomGradients.Thread,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style.copy(brush = brush),
        modifier = modifier
    )
}

data class GlassPadding(val horizontal: Dp = 16.dp, val vertical: Dp = 14.dp)

fun Modifier.glassPadding(p: GlassPadding = GlassPadding()): Modifier =
    this.padding(horizontal = p.horizontal, vertical = p.vertical)
