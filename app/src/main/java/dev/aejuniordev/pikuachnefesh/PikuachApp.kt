package dev.aejuniordev.pikuachnefesh

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dev.aejuniordev.pikuachnefesh.alarm.AlarmReceiver
import dev.aejuniordev.pikuachnefesh.data.TaskRepository

class PikuachApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.init(this)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(AlarmReceiver.CHANNEL_ID, "Alarmes de tarefas", NotificationManager.IMPORTANCE_HIGH)
        )
    }
}
