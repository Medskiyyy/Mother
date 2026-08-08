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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.category_picker_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
            androidx.compose.material3.TextButton(onClick = { showCreateDialog = true }) {
                Text("+ Kategori Baru", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
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
        Spacer(Modifier.height(24.dp))
    }

    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = { newCategory ->
                showCreateDialog = false
                onSelected(newCategory)
            }
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreated: (CategoryEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val container = (context.applicationContext as com.mother.app.MotherApplication).container
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF9F43") }
    val colors = listOf("#FF6B6B", "#FF9F43", "#FECA57", "#1DD1A1", "#54A0FF", "#5f27cd", "#FF9FF3")

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Custom", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Pilih Warna", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { hex ->
                        val color = parseCategoryColor(hex)
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { selectedColor = hex },
                            shape = CircleShape,
                            color = color,
                            border = if (selectedColor == hex) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.outline) else null
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            com.mother.app.ui.components.NeoButton(
                text = "Simpan",
                onClick = {
                    if (name.isNotBlank()) {
                        val newCat = CategoryEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            icon = "tag",
                            color = selectedColor,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        coroutineScope.launch {
                            container.categoryRepository.upsert(newCat)
                            onCreated(newCat)
                        }
                    }
                },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
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
