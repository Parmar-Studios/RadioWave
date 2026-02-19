package com.parmarstudios.radiowave.player

/**
 * Enum representing the playback state of the radio player.
 *
 * - [LOADING]: The player is currently loading a stream.
 * - [PLAYING]: The player is actively playing audio.
 * - [STOPPED]: The player is stopped and not playing audio.
 */
enum class PlaybackState {
    LOADING, // Player is loading the stream
    PLAYING, // Player is playing audio
    STOPPED  // Player is stopped
}