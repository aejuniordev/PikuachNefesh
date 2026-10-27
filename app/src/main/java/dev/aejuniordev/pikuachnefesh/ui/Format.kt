package dev.aejuniordev.pikuachnefesh.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import dev.aejuniordev.pikuachnefesh.data.Task
import dev.aejuniordev.pikuachnefesh.ui.theme.Butter
import dev.aejuniordev.pikuachnefesh.ui.theme.Mint
import dev.aejuniordev.pikuachnefesh.ui.theme.Peach
import dev.aejuniordev.pikuachnefesh.ui.theme.Rose
import dev.aejuniordev.pikuachnefesh.ui.theme.Sky
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val PtBr: Locale = Locale.forLanguageTag("pt-BR")
val DayTitleFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PtBr)
val ShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", PtBr)
val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun String.capitalized() = replaceFirstChar { it.titlecase(PtBr) }

// Horas até a tarefa -> cor. Entre os pontos a cor é interpolada.
private val proximityStops = listOf(0f to Peach, 24f to Butter, 72f to Mint, 168f to Sky)

/**
 * Cor pastel conforme a proximidade: pêssego (agora) → manteiga (hoje/amanhã) → menta (próximos dias)
 * → azul-céu (uma semana ou mais). Atrasadas e não concluídas ficam rosadas.
 */
fun proximityColor(task: Task, now: LocalDateTime = LocalDateTime.now()): Color {
    val hours = Duration.between(now, task.start).toMinutes() / 60f
    if (hours < 0) return Rose
    val upper = proximityStops.indexOfFirst { hours <= it.first }
    if (upper == -1) return proximityStops.last().second
    if (upper == 0) return proximityStops.first().second
    val (h0, c0) = proximityStops[upper - 1]
    val (h1, c1) = proximityStops[upper]
    return lerp(c0, c1, (hours - h0) / (h1 - h0))
}

/** "atrasada", "agora", "em 40 min", "em 3 h", "amanhã", "em 4 dias". */
fun proximityLabel(task: Task, now: LocalDateTime = LocalDateTime.now()): String {
    val minutes = Duration.between(now, task.start).toMinutes()
    val days = Duration.between(now.toLocalDate().atStartOfDay(), task.start.toLocalDate().atStartOfDay()).toDays()
    return when {
        minutes < 0 -> "atrasada"
        minutes < 1 -> "agora"
        minutes < 60 -> "em $minutes min"
        days == 0L -> "em ${minutes / 60} h"
        days == 1L -> "amanhã"
        else -> "em $days dias"
    }
}

fun alarmLabel(minutes: Int) = when {
    minutes == 0 -> "Na hora"
    minutes < 60 -> "$minutes minutos antes"
    minutes == 60 -> "1 hora antes"
    else -> "${minutes / 60} horas antes"
}
