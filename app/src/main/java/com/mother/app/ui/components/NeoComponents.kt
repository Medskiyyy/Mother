package com.mother.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mother.app.data.model.Priority
import com.mother.app.ui.theme.PriorityAman
import com.mother.app.ui.theme.PriorityMepet
import com.mother.app.ui.theme.PriorityUrgent
import com.mother.app.ui.theme.PriorityWaspada

import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

private val NEO_BORDER_WIDTH = 3.5.dp
private val NEO_SHADOW_OFFSET = 4.dp
val NeoCornerRadius = 14.dp

/**
 * Hard offset shadow drawn behind a component (Neobrutalism signature).
 */
fun Modifier.neoShadow(
    color: Color = Color(0xFF121212),
    offsetX: Dp = NEO_SHADOW_OFFSET,
    offsetY: Dp = NEO_SHADOW_OFFSET,
    shape: Shape = RoundedCornerShape(NeoCornerRadius)
): Modifier = drawBehind {
    val shadowOutline = shape.createOutline(size, layoutDirection, this)
    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(offsetX.toPx(), offsetY.toPx())
        canvas.drawOutline(
            outline = shadowOutline,
            paint = androidx.compose.ui.graphics.Paint().apply {
                this.color = color
            }
        )
        canvas.restore()
    }
}

/**
 * Card with a firm 3.5dp border, flat surface, and a hard offset shadow (Neobrutalism signature).
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shape: Shape = RoundedCornerShape(NeoCornerRadius),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .padding(end = NEO_SHADOW_OFFSET, bottom = NEO_SHADOW_OFFSET)
            .neoShadow(color = borderColor, shape = shape)
    ) {
        Surface(
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(NEO_BORDER_WIDTH, borderColor),
            shadowElevation = 0.dp,
            onClick = onClick ?: {},
            enabled = onClick != null
        ) {
            content()
        }
    }
}



/** Primary filled button with a firm border (hard shadow look, no elevation). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = ButtonDefaults.ContentPadding,
    fullWidth: Boolean = true
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Box(
            modifier = modifier
                .padding(end = 3.dp, bottom = 3.dp)
                .neoShadow(color = borderColor, offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(10.dp))
        ) {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor,
                    disabledContentColor = contentColor
                ),
                border = BorderStroke(2.5.dp, borderColor),
                contentPadding = contentPadding,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Secondary outlined button keeping the neobrutalist border treatment. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = ButtonDefaults.ContentPadding,
    fullWidth: Boolean = true
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Box(
            modifier = modifier
                .padding(end = 3.dp, bottom = 3.dp)
                .neoShadow(color = borderColor, offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(10.dp))
        ) {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor,
                    disabledContentColor = contentColor
                ),
                border = BorderStroke(2.5.dp, borderColor),
                contentPadding = contentPadding,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Capsule Priority Badge (Neobrutalist style: solid bg, black border, bold uppercase text).
 */
@Composable
fun NeoPriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority) {
        Priority.URGENT -> PriorityUrgent to Color(0xFF121212)
        Priority.MEPET -> PriorityMepet to Color(0xFF121212)
        Priority.WASPADA -> PriorityWaspada to Color(0xFF121212)
        Priority.AMAN -> PriorityAman to Color(0xFF121212)
    }

    val label = priority.name

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, Color(0xFF121212), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

