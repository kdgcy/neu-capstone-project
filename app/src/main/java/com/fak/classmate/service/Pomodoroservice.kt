package com.fak.classmate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroService : Service() {

    private val binder = PomodoroServiceBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    // Timer state
    private val _timeLeftMillis = MutableStateFlow(25 * 60 * 1000L)
    val timeLeftMillis: StateFlow<Long> = _timeLeftMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _completedSessions = MutableStateFlow(0)
    val completedSessions: StateFlow<Int> = _completedSessions.asStateFlow()

    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "pomodoro_timer_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_STOP = "ACTION_STOP"
    }

    inner class PomodoroServiceBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startTimerLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> resetTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows Pomodoro timer countdown"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                if (_isRunning.value && _timeLeftMillis.value > 0) {
                    delay(1000L)
                    _timeLeftMillis.value -= 1000L
                    updateNotification()

                    if (_timeLeftMillis.value <= 0) {
                        onTimerComplete()
                    }
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun onTimerComplete() {
        _isRunning.value = false
        _completedSessions.value++

        // Play alarm sound
        playAlarmSound()

        // Show completion notification
        showCompletionNotification()

        // Reset to 25 minutes for next session
        _timeLeftMillis.value = 25 * 60 * 1000L
        updateNotification()
    }

    fun startTimer() {
        _isRunning.value = true
        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun pauseTimer() {
        _isRunning.value = false
        updateNotification()
    }

    fun resetTimer() {
        _isRunning.value = false
        _timeLeftMillis.value = 25 * 60 * 1000L
        updateNotification()
    }

    private fun stopTimer() {
        _isRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): android.app.Notification {
        val minutes = (_timeLeftMillis.value / 1000) / 60
        val seconds = (_timeLeftMillis.value / 1000) % 60
        val timeDisplay = String.format("%02d:%02d", minutes, seconds)

        val statusText = if (_isRunning.value) "Running" else "Paused"

        // Action buttons
        val pauseIntent = Intent(this, PomodoroService::class.java).apply {
            action = if (_isRunning.value) ACTION_PAUSE else ACTION_START
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val resetIntent = Intent(this, PomodoroService::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = PendingIntent.getService(
            this, 1, resetIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, PomodoroService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
            .setContentText("$timeDisplay - $statusText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (_isRunning.value) "Pause" else "Start",
                pausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Reset",
                resetPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun showCompletionNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Completed! 🎉")
            .setContentText("Great work! Time for a break!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(999, notification)
    }

    private fun playAlarmSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }
}