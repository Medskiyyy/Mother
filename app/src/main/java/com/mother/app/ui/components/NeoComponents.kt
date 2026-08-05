package com.mother.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neobrutalism building blocks (Docs/design_system.md): flat surfaces, firm
 * borders, hard offset shadows, high contrast. Press feedback shifts the
 * content into its shadow within the 300 ms motion budget (§Animation).
 */

private val NEO_BORDER_WIDTH = 2.dp
private val NEO_SHADOW_OFFSET = 4.dp

/**
 * Hard offset shadow drawn behind a component (Neobrutalism signature).
 * Use with padding so the shadow stays within the layout bounds:
 * `Modifier.neoShadow().padding(end = 4.dp, bottom = 4.dp)` (or spacing).
 */
fun Modifier.neoShadow(
    color: Color = Color(0xB31B1B1B),
    offsetX: Dp = NEO_SHADOW_OFFSET,
    offsetY: Dp = NEO_SHADOW_OFFSET
): Modifier = drawBehind {
    drawRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(offsetX.toPx(), offsetY.toPx()),
        size = size
    )
}

/**
 * Card with a firm border, flat surface, and a hard offset shadow
 * (Neobrutalism signature). The built-in pressed state gives quick tonal
 * feedback within the motion budget.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.neoShadow().padding(end = NEO_SHADOW_OFFSET, bottom = NEO_SHADOW_OFFSET)
    ) {
        Surface(
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(NEO_BORDER_WIDTH, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
            onClick = onClick ?: {},
            enabled = onClick != null
        ) {
            content()
        }
    }
}

/** Primary filled button with a firm border (hard shadow look, no elevation). */
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(NEO_BORDER_WIDTH, MaterialTheme.colorScheme.outline),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text)
    }
}

/** Secondary outlined button keeping the neobrutalist border treatment. */
@Composable
fun NeoOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(NEO_BORDER_WIDTH, MaterialTheme.colorScheme.outline),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text)
    }
}
