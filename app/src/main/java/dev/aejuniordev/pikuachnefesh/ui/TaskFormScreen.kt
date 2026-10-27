package dev.aejuniordev.pikuachnefesh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.aejuniordev.pikuachnefesh.data.Category
import dev.aejuniordev.pikuachnefesh.data.Task
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.data.TaskType
import dev.aejuniordev.pikuachnefesh.ui.theme.Ink
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

private val AlarmOptions = listOf(0, 5, 10, 15, 30, 60, 120)

/** Criar/editar evento ou tarefa, inspirado no formulário do Google Agenda. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    existing: Task?,
    initialDate: LocalDate,
    onClose: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit,
) {
    val defaultStart = remember { LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: TaskType.TASK) }
    var date by remember { mutableStateOf(existing?.start?.toLocalDate() ?: initialDate) }
    var startTime by remember { mutableStateOf(existing?.start?.toLocalTime() ?: defaultStart) }
    var endTime by remember { mutableStateOf(existing?.end?.toLocalTime() ?: defaultStart.plusHours(1)) }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    // Tarefas cuja categoria foi excluída recebem a primeira categoria ao serem editadas.
    var categoryId by remember {
        mutableStateOf(TaskRepository.category(existing?.categoryId)?.id ?: TaskRepository.categories.firstOrNull()?.id)
    }
    var alarmMinutes by remember { mutableIntStateOf(existing?.alarmMinutesBefore ?: 5) }

    var showDatePicker by remember { mutableStateOf(false) }
    var editingTime by remember { mutableStateOf<TimeField?>(null) }
    var showNewCategory by remember { mutableStateOf(false) }

    val category = TaskRepository.category(categoryId)
    // O título é opcional quando há categoria: ela passa a ser o nome exibido.
    val canSave = title.isNotBlank() || category != null

    fun save() {
        val start = date.atTime(startTime)
        val end = if (type == TaskType.EVENT) {
            date.atTime(endTime).let { if (it.isBefore(start)) it.plusDays(1) else it }
        } else null
        onSave(
            Task(
                id = existing?.id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                type = type,
                start = start,
                end = end,
                description = description.trim(),
                categoryId = categoryId,
                alarmMinutesBefore = alarmMinutes,
                done = existing?.done ?: false,
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Fechar") }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { onDelete(existing) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                    Button(onClick = ::save, enabled = canSave, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Salvar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        if (category != null) "Adicionar título (opcional)" else "Adicionar título",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 40.dp),
            )

            Row(Modifier.padding(start = 56.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == TaskType.EVENT, onClick = { type = TaskType.EVENT }, label = { Text("Evento") })
                FilterChip(selected = type == TaskType.TASK, onClick = { type = TaskType.TASK }, label = { Text("Tarefa") })
            }

            FormRow(Icons.AutoMirrored.Filled.List) {
                CategorySelector(
                    selected = category,
                    onSelect = { categoryId = it.id },
                    onCreateNew = { showNewCategory = true },
                )
            }

            FormRow(Icons.Default.DateRange) {
                Column {
                    TextButton(onClick = { showDatePicker = true }) { Text(date.format(DayTitleFormat).capitalized()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { editingTime = TimeField.START }) { Text(startTime.format(TimeFormat)) }
                        if (type == TaskType.EVENT) {
                            Text("–")
                            TextButton(onClick = { editingTime = TimeField.END }) { Text(endTime.format(TimeFormat)) }
                        }
                    }
                }
            }

            FormRow(Icons.Default.Edit) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Adicionar descrição (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormRow(Icons.Default.Notifications) {
                AlarmSelector(alarmMinutes) { alarmMinutes = it }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // O DatePicker trabalha com meia-noite em UTC
                    state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state) }
    }

    editingTime?.let { field ->
        val initial = if (field == TimeField.START) startTime else endTime
        val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { editingTime = null },
            confirmButton = {
                TextButton(onClick = {
                    val picked = LocalTime.of(state.hour, state.minute)
                    if (field == TimeField.START) {
                        // Mantém a duração do evento ao mover o início
                        endTime = picked.plusMinutes(Duration.between(startTime, endTime).toMinutes())
                        startTime = picked
                    } else {
                        endTime = picked
                    }
                    editingTime = null
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { editingTime = null }) { Text("Cancelar") } },
            text = { TimePicker(state) },
        )
    }

    if (showNewCategory) {
        CategoryEditorDialog(
            initial = null,
            onDismiss = { showNewCategory = false },
            onSave = {
                TaskRepository.saveCategory(it)
                categoryId = it.id
                showNewCategory = false
            },
        )
    }
}

private enum class TimeField { START, END }

@Composable
private fun FormRow(icon: ImageVector, content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        content()
    }
}

/**
 * Botão no estilo do seletor de agenda do Google, preenchido com a cor da categoria.
 * Não há "sem categoria": como no Google, toda tarefa pertence a uma categoria, que é o que a identifica.
 */
@Composable
private fun CategorySelector(selected: Category?, onSelect: (Category) -> Unit, onCreateNew: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = selected?.uiColor ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (selected != null) Ink else MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(selected?.name ?: "Escolher categoria")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskRepository.categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = { ColorDot(category.uiColor, 16.dp) },
                    onClick = { onSelect(category); expanded = false },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Nova categoria…") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { expanded = false; onCreateNew() },
            )
        }
    }
}

@Composable
private fun AlarmSelector(minutes: Int, onChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(onClick = { expanded = true }, label = { Text("Alarme: " + alarmLabel(minutes).lowercase(PtBr)) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AlarmOptions.forEach { option ->
                DropdownMenuItem(text = { Text(alarmLabel(option)) }, onClick = { onChange(option); expanded = false })
            }
        }
    }
}
