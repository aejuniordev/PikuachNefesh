package dev.aejuniordev.pikuachnefesh.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

/** Guarda tarefas e categorias em memória (observável pelo Compose) e persiste em JSON. */
object TaskRepository {
    private lateinit var file: File

    val tasks = mutableStateListOf<Task>()
    val categories = mutableStateListOf<Category>()

    /** Categorias desmarcadas no filtro, como desmarcar uma agenda no Google. */
    val hiddenCategoryIds = mutableStateListOf<String>()

    fun init(context: Context) {
        if (::file.isInitialized) return
        file = File(context.filesDir, "tasks.json")
        if (file.exists()) {
            load()
        } else {
            categories += listOf(
                Category(name = "Pessoal", color = 0xFFD7CCF5),
                Category(name = "Trabalho", color = 0xFFC6E2F5),
                Category(name = "Saúde", color = 0xFFC8EBD9),
            )
            save()
        }
    }

    fun find(id: String) = tasks.firstOrNull { it.id == id }

    fun category(id: String?) = categories.firstOrNull { it.id == id }

    fun isVisible(task: Task) = task.categoryId == null || task.categoryId !in hiddenCategoryIds

    fun tasksOn(date: LocalDate) =
        tasks.filter { it.start.toLocalDate() == date && isVisible(it) }.sortedBy { it.start }

    /** Quantas tarefas do período estão escondidas pelo filtro de categorias. */
    fun hiddenCount(from: LocalDate, to: LocalDate) =
        tasks.count { it.start.toLocalDate() in from..to && !isVisible(it) }

    fun upsert(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) tasks[index] = task else tasks += task
        // Salvar numa categoria oculta voltaria a esconder a tarefa recém-criada; reexibe a categoria.
        task.categoryId?.let { hiddenCategoryIds.remove(it) }
        save()
    }

    fun delete(id: String) {
        tasks.removeAll { it.id == id }
        save()
    }

    fun saveCategory(category: Category) {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index >= 0) categories[index] = category else categories += category
        save()
    }

    /** Remove a categoria e move as tarefas dela para [moveTo]. Sempre sobra ao menos uma categoria. */
    fun deleteCategory(id: String, moveTo: String) {
        if (categories.size <= 1 || id == moveTo) return
        categories.removeAll { it.id == id }
        hiddenCategoryIds.remove(id)
        for (i in tasks.indices) {
            if (tasks[i].categoryId == id) tasks[i] = tasks[i].copy(categoryId = moveTo)
        }
        save()
    }

    fun toggleCategoryVisibility(id: String) {
        if (!hiddenCategoryIds.remove(id)) hiddenCategoryIds += id
        save()
    }

    private fun save() {
        val json = JSONObject()
            .put("categories", JSONArray(categories.map {
                JSONObject().put("id", it.id).put("name", it.name).put("color", it.color)
            }))
            .put("hiddenCategoryIds", JSONArray(hiddenCategoryIds.toList()))
            .put("tasks", JSONArray(tasks.map {
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("type", it.type.name)
                    .put("start", it.start.toString())
                    .put("end", it.end?.toString() ?: JSONObject.NULL)
                    .put("description", it.description)
                    .put("categoryId", it.categoryId ?: JSONObject.NULL)
                    .put("alarmMinutesBefore", it.alarmMinutesBefore)
                    .put("done", it.done)
            }))
        file.writeText(json.toString())
    }

    private fun load() {
        val json = JSONObject(file.readText())
        // "segments"/"segmentId" eram os nomes da primeira versão
        val cats = json.optJSONArray("categories") ?: json.getJSONArray("segments")
        for (i in 0 until cats.length()) {
            val c = cats.getJSONObject(i)
            categories += Category(c.getString("id"), c.getString("name"), c.getLong("color"))
        }
        json.optJSONArray("hiddenCategoryIds")?.let { hidden ->
            for (i in 0 until hidden.length()) hiddenCategoryIds += hidden.getString(i)
        }
        val items = json.getJSONArray("tasks")
        for (i in 0 until items.length()) {
            val t = items.getJSONObject(i)
            tasks += Task(
                id = t.getString("id"),
                title = t.getString("title"),
                type = TaskType.valueOf(t.getString("type")),
                start = LocalDateTime.parse(t.getString("start")),
                end = t.stringOrNull("end")?.let(LocalDateTime::parse),
                description = t.optString("description"),
                categoryId = t.stringOrNull("categoryId") ?: t.stringOrNull("segmentId"),
                alarmMinutesBefore = t.optInt("alarmMinutesBefore", 5),
                done = t.optBoolean("done"),
            )
        }
    }

    private fun JSONObject.stringOrNull(key: String) = if (!has(key) || isNull(key)) null else getString(key)
}

/** Título exibido: o próprio título ou, se vazio, o nome da categoria. */
val Task.displayTitle: String
    get() = title.ifBlank { TaskRepository.category(categoryId)?.name ?: "Sem título" }
