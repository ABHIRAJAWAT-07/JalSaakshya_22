package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    var isRecording = false
        private set
    var isPlaying = false
        private set

    fun startRecording(): File? {
        stopPlayback()
        return try {
            val audioDir = File(context.filesDir, "evidence_audio").apply { mkdirs() }
            val file = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            file
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            null
        }
    }

    fun stopRecording(): Pair<File?, Int> {
        if (!isRecording) return Pair(null, 0)
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            val file = currentRecordingFile
            val durationSec = if (file != null && file.exists()) {
                // approximate or retrieve length
                getAudioDurationSec(file.absolutePath)
            } else 0
            Pair(file, durationSec)
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            Pair(null, 0)
        }
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    this@AudioRecorderHelper.isPlaying = false
                    onCompletion()
                }
                start()
            }
            this@AudioRecorderHelper.isPlaying = true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to play audio", e)
            this@AudioRecorderHelper.isPlaying = false
            onCompletion()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (this.isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            this@AudioRecorderHelper.isPlaying = false
        }
    }

    private fun getAudioDurationSec(path: String): Int {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(path)
            mp.prepare()
            val durationMs = mp.duration
            mp.release()
            (durationMs / 1000).coerceAtLeast(1)
        } catch (e: Exception) {
            15 // fallback estimate
        }
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
        stopPlayback()
    }
}
