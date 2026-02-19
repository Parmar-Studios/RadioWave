package com.parmarstudios.radiowave.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.parmarstudios.radiowave.MainActivity
import com.parmarstudios.radiowave.R
import com.parmarstudios.radiowave.data.preferences.LastPlayingStationPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Foreground service responsible for managing radio playback,
 * notifications, media session, and playback timer.
 */
class RadioPlayerService : Service() {

    companion object {
        // Notification and action constants
        const val CHANNEL_ID = "radio_playback_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val ACTION_PLAYBACK_STOPPED = "com.parmarstudios.radiowave.ACTION_PLAYBACK_STOPPED"
        const val ACTION_PLAYBACK_PAUSED = "com.parmarstudios.radiowave.ACTION_PLAYBACK_PAUSED"
        const val ACTION_PLAYBACK_RESUMED = "com.parmarstudios.radiowave.ACTION_PLAYBACK_RESUMED"
        const val ACTION_PLAYBACK_ERROR = "com.parmarstudios.radiowave.ACTION_PLAYBACK_ERROR"
        const val EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE"
        const val ACTION_PLAYBACK_TIMER = "com.parmarstudios.radiowave.ACTION_PLAYBACK_TIMER"
        const val EXTRA_ELAPSED_SECONDS = "EXTRA_ELAPSED_SECONDS"
    }

    private var isPlaying = true // Indicates if playback is active
    private var stationName: String = "Radio" // Current station name

    private var timerJob: Job? = null // Job for playback timer coroutine
    private var elapsedSeconds = 0 // Elapsed playback time in seconds

    private lateinit var mediaSession: MediaSessionCompat // Media session for system controls

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // Ensure notification channel exists
        initMediaSession() // Initialize media session
    }

    /**
     * Handles incoming intents to control playback (play, pause, resume, stop).
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val url = intent?.getStringExtra(EXTRA_URL)
        stationName = intent?.getStringExtra(EXTRA_NAME) ?: stationName

        when (action) {
            ACTION_PLAY -> {
                if (url != null && stationName.isNotEmpty()) {
                    // Start playback using RadioPlayer
                    RadioPlayer.play(
                        this,
                        url,
                        onReady = {
                            Log.d("RadioPlayerService", "Sending ACTION_PLAYBACK_RESUMED")
                            startTimer() // Start playback timer
                            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PLAYBACK_RESUMED))
                            savePlaybackState(PlaybackState.PLAYING)
                        },
                        onError = {
                            // Notify error via broadcast
                            val errorIntent = Intent(ACTION_PLAYBACK_ERROR)
                            errorIntent.putExtra(EXTRA_ERROR_MESSAGE, "Failed to play station")
                            LocalBroadcastManager.getInstance(this).sendBroadcast(errorIntent)
                            stopSelf()
                        }
                    )
                    isPlaying = true
                    startForeground(NOTIFICATION_ID, buildNotification(stationName, isPlaying))
                }
            }
            ACTION_PAUSE -> {
                RadioPlayer.pause()
                isPlaying = false
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PLAYBACK_PAUSED))
                stopTimer(reset = false)
                updateNotification()
                savePlaybackState(PlaybackState.STOPPED)
            }
            ACTION_RESUME -> {
                RadioPlayer.resume()
                isPlaying = true
                Log.d("RadioPlayerService", "Sending ACTION_PLAYBACK_RESUMED")
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PLAYBACK_RESUMED))
                startTimer()
                updateNotification()
                savePlaybackState(PlaybackState.PLAYING)
            }
            ACTION_STOP -> {
                RadioPlayer.stop()
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_PLAYBACK_STOPPED))
                stopForeground(true)
                stopTimer(reset = true)
                stopSelf()
            }
        }
        return START_STICKY // Service will be restarted if killed
    }

    override fun onDestroy() {
        mediaSession.release() // Release media session resources
        RadioPlayer.stop() // Stop playback
        LastPlayingStationPreference(this).clearStation()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null // Not a bound service

    /**
     * Initializes the media session for system playback controls.
     */
    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "RadioPlayerService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                // Resume playback via service intent
                val intent = Intent(this@RadioPlayerService, RadioPlayerService::class.java)
                intent.action = ACTION_RESUME
                startService(intent)
            }

            override fun onPause() {
                // Pause playback via service intent
                val intent = Intent(this@RadioPlayerService, RadioPlayerService::class.java)
                intent.action = ACTION_PAUSE
                startService(intent)
            }

            override fun onStop() {
                // Stop playback via service intent
                val intent = Intent(this@RadioPlayerService, RadioPlayerService::class.java)
                intent.action = ACTION_STOP
                startService(intent)
            }
        })
        mediaSession.isActive = true
        updateMediaSessionState()
    }

    /**
     * Updates the media session playback state for system controls.
     */
    private fun updateMediaSessionState() {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    /**
     * Builds the playback notification with play/pause/stop actions.
     */
    private fun buildNotification(stationName: String, isPlaying: Boolean): Notification {
        // Intent to stop playback
        val stopIntent = Intent(this, RadioPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to play or pause playback
        val playPauseIntent = Intent(this, RadioPlayerService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_RESUME
        }

        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to open the main app
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing: $stationName")
            .setSmallIcon(R.drawable.ic_placeholder_light)
            .setContentIntent(openAppPendingIntent)
            .setContentText("Elapsed: ${formatElapsedTime(elapsedSeconds)}")
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
            .setOngoing(isPlaying)
            .build()
    }

    /**
     * Updates the playback notification.
     */
    private fun updateNotification() {
        val notification = buildNotification(stationName, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Creates the notification channel for playback notifications (Android O+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    /**
     * Saves the current playback state to shared preferences.
     */
    private fun savePlaybackState(state: PlaybackState) {
        LastPlayingStationPreference(this).updatePlaybackState(state)
    }

    /**
     * Starts the playback timer coroutine, broadcasting elapsed time every second.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && isPlaying) {
                delay(1000)
                elapsedSeconds++
                saveElapsedSeconds(elapsedSeconds)
                val intent = Intent(ACTION_PLAYBACK_TIMER)
                intent.putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
                LocalBroadcastManager.getInstance(this@RadioPlayerService).sendBroadcast(intent)
                withContext(Dispatchers.Main) { updateNotification() }
            }
        }
    }

    /**
     * Stops the playback timer coroutine and optionally resets elapsed time.
     */
    private fun stopTimer(reset: Boolean = false) {
        timerJob?.cancel()
        timerJob = null
        if (reset) {
            elapsedSeconds = 0
            saveElapsedSeconds(0)
        }
    }

    /**
     * Formats elapsed seconds as a time string (hh:mm:ss or mm:ss).
     */
    private fun formatElapsedTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    /**
     * Saves the elapsed playback seconds to shared preferences.
     */
    private fun saveElapsedSeconds(seconds: Int) {
        LastPlayingStationPreference(this).updateElapsedSeconds(seconds)
    }
}