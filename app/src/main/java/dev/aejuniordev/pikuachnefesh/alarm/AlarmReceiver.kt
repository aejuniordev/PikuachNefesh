package dev.aejuniordev.pikuachnefesh.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dev.aejuniordev.pikuachnefesh.MainActivity
import dev.aejuniordev.pikuachnefesh.R
import dev.aejuniordev.pikuachnefesh.data.TaskRepository
import dev.aejuniordev.pikuachnefesh.data.displayTitle
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID)?.let(TaskRepository::find) ?: return
        if (task.done) return
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.displayTitle)
            .setContentText("Às " + task.start.format(DateTimeFormatter.ofPattern("HH:mm")))
            .setColor(0xFF6D5FA0.toInt())
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(task.id.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "task_alarms"
    }
}
