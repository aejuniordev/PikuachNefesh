package dev.aejuniordev.pikuachnefesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.aejuniordev.pikuachnefesh.data.Category
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.ui.theme.CategoryPalette
import dev.aejuniordev.pikuachnefesh.ui.theme.Ink

val Category.uiColor get() = Color(color)

@Composable
fun ColorDot(color: Color, size: Dp = 12.dp) {
    Box(Modifier.size(size).background(color, CircleShape))
}

/** Etiqueta com a cor e o nome da categoria. */
@Composable
fun CategoryLabel(category: Category, modifier: Modifier = Modifier) {
    Text(
        category.name,
        style = MaterialTheme.typography.labelMedium,
        color = Ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(category.uiColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** Lista as categorias para editar, excluir ou criar novas. */
@Composable
fun CategoriesDialog(onDismiss: () -> Unit) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categorias") },
        text = {
            Column {
                TaskRepository.categories.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(category.uiColor, 20.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(category.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = { editing = category }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar ${category.name}")
                        }
                        IconButton(onClick = { deleting = category }, enabled = TaskRepository.categories.size > 1) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir ${category.name}")
                        }
                    }
                }
                TextButton(onClick = { creating = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nova categoria")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )

    if (creating || editing != null) {
        CategoryEditorDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = {
                TaskRepository.saveCategory(it)
                creating = false
                editing = null
            },
        )
    }

    deleting?.let { category ->
        val others = TaskRepository.categories.filter { it.id != category.id }
        var moveTo by remember(category) { mutableStateOf(others.first()) }
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir \"${category.name}\"?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("As tarefas desta categoria não serão apagadas. Mover para:")
                    others.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().clickable { moveTo = option }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = option == moveTo, onClick = { moveTo = option })
                            ColorDot(option.uiColor, 16.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(option.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    TaskRepository.deleteCategory(category.id, moveTo = moveTo.id)
                    deleting = null
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }
}

/** Cria (initial = null) ou edita uma categoria: nome + cor. */
@Composable
fun CategoryEditorDialog(initial: Category?, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.uiColor ?: CategoryPalette.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nova categoria" else "Editar categoria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    leadingIcon = { ColorDot(color, 16.dp) },
                    modifier = Modifier.fillMaxWidth(),
                )
                CategoryPalette.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { option ->
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .background(option, CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable { color = option },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (option == color) Icon(Icons.Default.Check, contentDescription = "Selecionada", tint = Ink)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val argb = color.toArgb().toLong() and 0xFFFFFFFFL
                    onSave(initial?.copy(name = name.trim(), color = argb) ?: Category(name = name.trim(), color = argb))
                },
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
