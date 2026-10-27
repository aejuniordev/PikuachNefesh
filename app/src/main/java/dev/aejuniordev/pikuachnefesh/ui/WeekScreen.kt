package dev.aejuniordev.pikuachnefesh.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aejuniordev.pikuachnefesh.data.Task
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.data.displayTitle
import dev.aejuniordev.pikuachnefesh.ui.theme.Ink
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val WeekdayFormat = DateTimeFormatter.ofPattern("EEE", PtBr)

/** Mapa de tarefas ao longo da semana. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(onDayClick: (LocalDate) -> Unit, onAdd: () -> Unit) {
    val today = LocalDate.now()
    var weekStart by rememberSaveable { mutableStateOf(today.with(DayOfWeek.MONDAY)) }
    var showCategories by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha semana") },
                actions = {
                    IconButton(onClick = { showCategories = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Categorias")
                    }
                    FilledTonalIconButton(onClick = onAdd, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { CategoryFilter() }
            val hidden = TaskRepository.hiddenCount(weekStart, weekStart.plusDays(6))
            if (hidden > 0) {
                item {
                    Text(
                        if (hidden == 1) "1 tarefa oculta pelo filtro de categorias"
                        else "$hidden tarefas ocultas pelo filtro de categorias",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                WeekHeader(
                    weekStart = weekStart,
                    isCurrentWeek = weekStart == today.with(DayOfWeek.MONDAY),
                    onPrevious = { weekStart = weekStart.minusWeeks(1) },
                    onNext = { weekStart = weekStart.plusWeeks(1) },
                    onToday = { weekStart = today.with(DayOfWeek.MONDAY) },
                )
            }
            items((0L..6L).map(weekStart::plusDays), key = { it.toString() }) { day ->
                DayCard(day, isToday = day == today, tasks = TaskRepository.tasksOn(day)) { onDayClick(day) }
            }
        }
    }

    if (showCategories) CategoriesDialog(onDismiss = { showCategories = false })
}

/** Liga/desliga a exibição de cada categoria, como as agendas no Google. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TaskRepository.categories, key = { it.id }) { category ->
            val visible = category.id !in TaskRepository.hiddenCategoryIds
            FilterChip(
                selected = visible,
                onClick = { TaskRepository.toggleCategoryVisibility(category.id) },
                label = {
                    Text(category.name, textDecoration = if (visible) null else TextDecoration.LineThrough)
                },
                leadingIcon = {
                    if (visible) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Ink)
                    } else {
                        Box(Modifier.size(12.dp).border(2.dp, category.uiColor, CircleShape))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.uiColor,
                    selectedLabelColor = Ink,
                ),
            )
        }
    }
}

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    isCurrentWeek: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior")
        }
        Text(
            "${weekStart.format(ShortDateFormat)} – ${weekStart.plusDays(6).format(ShortDateFormat)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (!isCurrentWeek) TextButton(onClick = onToday) { Text("Hoje") }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próxima semana")
        }
    }
}

@Composable
private fun DayCard(day: LocalDate, isToday: Boolean, tasks: List<Task>, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.format(WeekdayFormat).trimEnd('.').uppercase(PtBr),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (tasks.isEmpty()) {
                    Text("Livre", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                }
                tasks.take(3).forEach { TaskLine(it) }
                if (tasks.size > 3) {
                    Text("+${tasks.size - 3} mais", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** Como na visão de mês do Google: ponto da categoria, horário e título. */
@Composable
private fun TaskLine(task: Task) {
    val category = TaskRepository.category(task.categoryId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        ColorDot(category?.uiColor ?: MaterialTheme.colorScheme.outline, 10.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            task.start.format(TimeFormat),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            task.displayTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (task.done) TextDecoration.LineThrough else null,
        )
    }
}
