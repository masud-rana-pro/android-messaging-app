package com.contactme.app.media

import android.util.Log
import android.media.MediaPlayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicePlayer @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentId: String? = null

    fun play(id: String, url: String, onComplete: () -> Unit) {
        Log.d("VoicePlayer", "play called for ID: $id, URL: $url")
        if (currentId == id && mediaPlayer?.isPlaying == true) {
            Log.d("VoicePlayer", "Pausing current playback")
            mediaPlayer?.pause()
            return
        }

        stop()
        currentId = id
        mediaPlayer = MediaPlayer().apply {
            runCatching {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { 
                    Log.d("VoicePlayer", "MediaPlayer prepared, starting playback")
                    start() 
                }
                setOnCompletionListener { 
                    Log.d("VoicePlayer", "Playback completed")
                    stop()
                    onComplete()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("VoicePlayer", "MediaPlayer error: $what, $extra")
                    stop()
                    onComplete()
                    true
                }
            }.onFailure {
                Log.e("VoicePlayer", "Failed to setup MediaPlayer", it)
                onComplete()
            }
        }
    }

    fun stop() {
        Log.d("VoicePlayer", "stop called")
        mediaPlayer?.release()
        mediaPlayer = null
        currentId = null
    }
}
