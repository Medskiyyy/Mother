package com.mother.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mother.app.R
import com.mother.app.ui.components.EmptyState

/** Shown for modules that are wired into navigation but not yet built. */
@Composable
fun PlaceholderScreen(title: String) {
    EmptyState(
        icon = Icons.Filled.Construction,
        title = title,
        description = stringResource(R.string.coming_soon_description)
    )
}