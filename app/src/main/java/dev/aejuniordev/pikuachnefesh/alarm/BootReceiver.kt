package dev.aejuniordev.pikuachnefesh.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.aejuniordev.pikuachnefesh.data.TaskRepository

/** O Android apaga os alarmes ao reiniciar; aqui eles são reagendados. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        TaskRepository.tasks.forEach { AlarmScheduler.schedule(context, it) }
    }
}
