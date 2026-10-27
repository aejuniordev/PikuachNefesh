package dev.aejuniordev.pikuachnefesh.data

import java.time.LocalDateTime
import java.util.UUID

enum class TaskType { EVENT, TASK }

/** Categoria (como as "agendas" do Google): identifica a tarefa pela cor e pelo nome. */
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    /** ARGB, ex.: 0xFFD7CCF5 */
    val color: Long,
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    /** Opcional: sem título, a tarefa é exibida com o nome da categoria. */
    val title: String,
    val type: TaskType = TaskType.TASK,
    val start: LocalDateTime,
    /** Só usado em eventos. */
    val end: LocalDateTime? = null,
    val description: String = "",
    val categoryId: String? = null,
    val alarmMinutesBefore: Int = 5,
    val done: Boolean = false,
)
