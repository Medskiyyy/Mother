package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mother.app.R
import com.mother.app.data.local.entity.CategoryEntity

/**
 * Sheet for choosing the category of a Task / Schedule / Habit (categoryId is
 * a required FK). The caller owns the visibility state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    categories: List<CategoryEntity>,
    onSelected: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.category_picker_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.category_picker_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    CategoryChip(category = category, onClick = { onSelected(category) })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CategoryChip(category: CategoryEntity, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(category.name) },
        leadingIcon = {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = parseCategoryColor(category.color)
            ) {}
        }
    )
}

/** Parses a stored category color; falls back to the theme primary on bad input. */
fun parseCategoryColor(color: String): Color = try {
    Color(android.graphics.Color.parseColor(color))
} catch (_: IllegalArgumentException) {
    Color.Unspecified
}
