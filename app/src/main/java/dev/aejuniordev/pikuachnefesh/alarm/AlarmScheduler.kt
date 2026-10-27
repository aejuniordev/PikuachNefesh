package dev.aejuniordev.pikuachnefesh.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.aejuniordev.pikuachnefesh.data.Task
import java.time.ZoneId

object AlarmScheduler {
    const val EXTRA_TASK_ID = "task_id"

    fun schedule(context: Context, task: Task) {
        val triggerAt = task.start.minusMinutes(task.alarmMinutesBefore.toLong())
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (task.done || triggerAt < System.currentTimeMillis()) {
            cancel(context, task)
            return
        }
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, task)
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context, task: Task) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, task))
    }

    private fun pendingIntent(context: Context, task: Task): PendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.hashCode(),
        Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_TASK_ID, task.id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
