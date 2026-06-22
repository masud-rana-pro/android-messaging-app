package com.contactme.app.media

import android.media.MediaPlayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicePlayer @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentId: String? = null

    fun play(id: String, url: String, onComplete: () -> Unit) {
        if (currentId == id && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            return
        }

        stop()
        currentId = id
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { start() }
            setOnCompletionListener { 
                stop()
                onComplete()
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentId = null
    }
}
