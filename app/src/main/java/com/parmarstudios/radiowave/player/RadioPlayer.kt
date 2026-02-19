package com.parmarstudios.radiowave.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.PriorityTaskManager

/**
 * Singleton object that manages radio playback using ExoPlayer.
 * Provides methods to play, pause, resume, stop, and check playback state.
 */
object RadioPlayer {
    // Holds the current ExoPlayer instance, or null if not initialized
    private var exoPlayer: ExoPlayer? = null
    private var retryCount = 0
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS: Long = 3000

    private fun createLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16 * 1024))
            .setBufferDurationsMs(
                15_000, // minBufferMs: 15 seconds
                30_000, // maxBufferMs: 30 seconds
                1_000,  // bufferForPlaybackMs: 1 second
                3_000   // bufferForPlaybackAfterRebufferMs: 3 seconds
            )
            .build()
    }

    /**
     * Starts playback of the given stream URL.
     *
     * @param context The application context.
     * @param url The stream URL to play.
     * @param onReady Callback invoked when the player is ready.
     * @param onError Callback invoked if playback fails.
     */
    fun play(
        context: Context,
        url: String,
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        stop() // Release any existing player before starting new playback
        retryCount = 0
        exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(createLoadControl())
            .setPriorityTaskManager(PriorityTaskManager())
            .build().apply {
            setMediaItem(MediaItem.fromUri(url)) // Set the stream URL
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        retryCount = 0
                        onReady() // Notify when player is ready
                    }
                    if (state == Player.STATE_ENDED) {
                        stop() // Stop playback when stream ends
                    }
                }
                override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                    stop() // Release player on error
                    if (retryCount < MAX_RETRIES) {
                        retryCount++
                        Handler(Looper.getMainLooper()).postDelayed({
                            retryPlayback(context, url, onReady, onError)
                        }, RETRY_DELAY_MS)
                    } else {
                        onError("Playback failed after $MAX_RETRIES retries.")
                    }
                }
            })
            prepare() // Prepare the player asynchronously
            playWhenReady = true // Start playback when ready
        }
    }


    private fun retryPlayback(
        context: Context,
        url: String,
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        play(context, url, onReady, onError)
    }

    /**
     * Pauses playback if the player is active.
     */
    fun pause() {
        exoPlayer?.playWhenReady = false // Pause playback
    }

    /**
     * Resumes playback if the player is paused.
     */
    fun resume() {
        exoPlayer?.playWhenReady = true // Resume playback
    }

    /**
     * Stops playback and releases the player resources.
     */
    fun stop() {
        exoPlayer?.release() // Release player resources
        exoPlayer = null // Clear reference
    }

    /**
     * Checks if the player is currently playing audio.
     *
     * @return True if playing, false otherwise.
     */
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true // Return playback state
    }
}