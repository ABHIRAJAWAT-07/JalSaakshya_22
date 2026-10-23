package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoicePromptHelper(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isMuted = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoicePromptHelper", "Failed to init TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Attempt to set Hindi first, otherwise fallback to English
            val hindi = Locale("hi", "IN")
            val result = tts?.setLanguage(hindi)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.setSpeechRate(0.9f) // Slightly slower for clear rural comprehension
        }
    }

    fun speak(textHi: String, textEn: String, isHindi: Boolean = true) {
        if (isMuted || !isInitialized) return
        val textToSpeak = if (isHindi) textHi else textEn
        try {
            if (isHindi) {
                val hindi = Locale("hi", "IN")
                val res = tts?.setLanguage(hindi)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.ENGLISH)
                }
            } else {
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.stop()
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "JalSaakshyaVoicePrompt")
        } catch (e: Exception) {
            Log.e("VoicePromptHelper", "TTS speak failed", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
    }
}
