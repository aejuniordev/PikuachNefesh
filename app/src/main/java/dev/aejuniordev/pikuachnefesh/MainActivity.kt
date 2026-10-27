package dev.aejuniordev.pikuachnefesh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.aejuniordev.pikuachnefesh.alarm.AlarmScheduler
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.ui.DayScreen
import dev.aejuniordev.pikuachnefesh.ui.TaskFormScreen
import dev.aejuniordev.pikuachnefesh.ui.WeekScreen
import dev.aejuniordev.pikuachnefesh.ui.theme.PikuachNefeshTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PikuachNefeshTheme {
                PikuachNefeshApp()
            }
        }
    }
}

private sealed interface Screen {
    data object Week : Screen
    data class Day(val date: LocalDate) : Screen
    data class Form(val date: LocalDate, val taskId: String? = null) : Screen
}

@Composable
private fun PikuachNefeshApp() {
    val context = LocalContext.current
    val backStack = remember { mutableStateListOf<Screen>(Screen.Week) }
    fun push(screen: Screen) {
        backStack.add(screen)
    }
    fun pop() {
        backStack.removeAt(backStack.lastIndex)
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = backStack.size > 1) { pop() }

    when (val screen = backStack.last()) {
        Screen.Week -> WeekScreen(
            onDayClick = { push(Screen.Day(it)) },
            onAdd = { push(Screen.Form(LocalDate.now())) },
        )

        is Screen.Day -> DayScreen(
            date = screen.date,
            onBack = ::pop,
            onAdd = { push(Screen.Form(screen.date)) },
            onEdit = { push(Screen.Form(screen.date, it.id)) },
            onToggleDone = { task ->
                val updated = task.copy(done = !task.done)
                TaskRepository.upsert(updated)
                AlarmScheduler.schedule(context, updated)
            },
        )

        is Screen.Form -> TaskFormScreen(
            existing = screen.taskId?.let(TaskRepository::find),
            initialDate = screen.date,
            onClose = ::pop,
            onSave = { task ->
                TaskRepository.upsert(task)
                AlarmScheduler.schedule(context, task)
                pop()
            },
            onDelete = { task ->
                AlarmScheduler.cancel(context, task)
                TaskRepository.delete(task.id)
                pop()
            },
        )
    }
}
