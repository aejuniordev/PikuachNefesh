package dev.aejuniordev.pikuachnefesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.aejuniordev.pikuachnefesh.data.Task
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.data.TaskType
import dev.aejuniordev.pikuachnefesh.data.displayTitle
import dev.aejuniordev.pikuachnefesh.ui.theme.Ink
import java.time.LocalDate

/** Lista de tarefas do dia com checkbox. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    date: LocalDate,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Task) -> Unit,
    onToggleDone: (Task) -> Unit,
) {
    val tasks = TaskRepository.tasksOn(date)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(date.format(DayTitleFormat).capitalized()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova tarefa") },
            )
        },
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                val hidden = TaskRepository.hiddenCount(date, date)
                Text(
                    if (hidden > 0) "$hidden oculta(s) pelo filtro de categorias" else "Nada agendado para este dia",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(task, onClick = { onEdit(task) }, onToggleDone = { onToggleDone(task) })
                }
            }
        }
    }
}

/**
 * A categoria identifica a tarefa (faixa lateral + etiqueta com cor e nome);
 * a etiqueta de horário mostra a proximidade em tom pastel.
 */
@Composable
private fun TaskCard(task: Task, onClick: () -> Unit, onToggleDone: () -> Unit) {
    val category = TaskRepository.category(task.categoryId)
    val stripe = category?.uiColor ?: MaterialTheme.colorScheme.outlineVariant
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(8.dp).fillMaxHeight().background(stripe))
            Column(
                Modifier.weight(1f).padding(start = 14.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (category != null) CategoryLabel(category)
                    if (task.type == TaskType.EVENT) {
                        Text("Evento", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    task.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeBadge(task)
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        alarmLabel(task.alarmMinutesBefore),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Checkbox(
                checked = task.done,
                onCheckedChange = { onToggleDone() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(end = 4.dp),
            )
        }
    }
}

@Composable
private fun TimeBadge(task: Task) {
    val time = if (task.type == TaskType.EVENT && task.end != null) {
        "${task.start.format(TimeFormat)} – ${task.end.format(TimeFormat)}"
    } else {
        task.start.format(TimeFormat)
    }
    val label = if (task.done) time else "$time · ${proximityLabel(task)}"
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = Ink,
        modifier = Modifier
            .background(
                if (task.done) MaterialTheme.colorScheme.outlineVariant else proximityColor(task),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
