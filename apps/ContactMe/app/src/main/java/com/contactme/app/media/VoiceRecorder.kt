package com.contactme.app.media

import android.util.Log
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class VoiceRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(outputFile: File) {
        Log.d("VoiceRecorder", "Starting recording to: ${outputFile.absolutePath}")
        currentFile = outputFile
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stop(): File? {
        Log.d("VoiceRecorder", "Stopping recording")
        recorder?.apply {
            runCatching {
                stop()
                Log.d("VoiceRecorder", "Recording stopped successfully")
            }.onFailure {
                Log.e("VoiceRecorder", "Failed to stop recorder", it)
            }
            release()
        }
        recorder = null
        return currentFile
    }

    fun cancel() {
        Log.d("VoiceRecorder", "Cancelling recording")
        recorder?.apply {
            runCatching { stop() }
            release()
        }
        recorder = null
        currentFile?.delete()
        currentFile = null
    }
}
