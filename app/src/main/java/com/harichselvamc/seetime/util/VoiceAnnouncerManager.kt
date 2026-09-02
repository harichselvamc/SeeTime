package com.harichselvamc.seetime.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SupportedVoiceLanguage(
    val code: String,
    val locale: Locale,
    val displayName: String,
    val flagEmoji: String
) {
    ENGLISH("en", Locale.US, "English", "🇺🇸"),
    JAPANESE("ja", Locale.JAPAN, "日本語", "🇯🇵"),
    FRENCH("fr", Locale.FRENCH, "Français", "🇫🇷"),
    SPANISH("es", Locale("es", "ES"), "Español", "🇪🇸"),
    GERMAN("de", Locale.GERMAN, "Deutsch", "🇩🇪")
}

class VoiceAnnouncerManager private constructor() {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeakingState = MutableStateFlow(false)
    val isSpeakingState: StateFlow<Boolean> = _isSpeakingState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: VoiceAnnouncerManager? = null

        fun getInstance(): VoiceAnnouncerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceAnnouncerManager().also { INSTANCE = it }
            }
        }

        /**
         * Generates localized natural spoken speech script for a given city and timezone.
         */
        fun generateAnnouncementScript(
            cityName: String,
            targetZoneId: String,
            language: SupportedVoiceLanguage = SupportedVoiceLanguage.ENGLISH,
            localZoneId: String = ZoneId.systemDefault().id,
            referenceTime: ZonedDateTime = ZonedDateTime.now()
        ): String {
            val targetZone = try { ZoneId.of(targetZoneId) } catch (_: Exception) { ZoneId.systemDefault() }
            val localZone = try { ZoneId.of(localZoneId) } catch (_: Exception) { ZoneId.systemDefault() }

            val targetTime = referenceTime.withZoneSameInstant(targetZone)
            val localTime = referenceTime.withZoneSameInstant(localZone)

            val h24 = targetTime.hour
            val m = targetTime.minute

            // Time difference calculation based on timezone UTC offsets
            val diffSeconds = (targetTime.offset.totalSeconds - localTime.offset.totalSeconds).toLong()
            val diffMinutesTotal = (diffSeconds / 60).toInt()
            val diffHours = Math.abs(diffMinutesTotal) / 60
            val diffMinutes = Math.abs(diffMinutesTotal) % 60
            val isAhead = diffMinutesTotal > 0
            val isSame = diffMinutesTotal == 0

            return when (language) {
                SupportedVoiceLanguage.ENGLISH -> {
                    val greeting = when (h24) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        in 17..21 -> "Good evening"
                        else -> "Good night"
                    }
                    val ampm = if (h24 < 12) "AM" else "PM"
                    val h12 = when {
                        h24 == 0 -> 12
                        h24 <= 12 -> h24
                        else -> h24 - 12
                    }
                    val timeStr = if (m == 0) "$h12 $ampm" else "$h12 ${String.format("%02d", m)} $ampm"
                    val dayName = targetTime.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))

                    val diffStr = when {
                        isSame -> "This is the same as your local time."
                        isAhead -> if (diffMinutes == 0) "$cityName is $diffHours hours ahead of your local time."
                                   else "$cityName is $diffHours hours and $diffMinutes minutes ahead of your local time."
                        else -> if (diffMinutes == 0) "$cityName is $diffHours hours behind your local time."
                                else "$cityName is $diffHours hours and $diffMinutes minutes behind your local time."
                    }

                    "$greeting! In $cityName, the time is currently $timeStr on $dayName. $diffStr"
                }
                SupportedVoiceLanguage.JAPANESE -> {
                    val greeting = when (h24) {
                        in 5..10 -> "おはようございます。"
                        in 11..17 -> "こんにちは。"
                        else -> "こんばんは。"
                    }
                    val ampm = if (h24 < 12) "午前" else "午後"
                    val h12 = when {
                        h24 == 0 -> 12
                        h24 <= 12 -> h24
                        else -> h24 - 12
                    }
                    val diffStr = when {
                        isSame -> "現地時間と同じです。"
                        isAhead -> if (diffMinutes == 0) "現地時間より${diffHours}時間進んでいます。"
                                   else "現地時間より${diffHours}時間${diffMinutes}分進んでいます。"
                        else -> if (diffMinutes == 0) "現地時間より${diffHours}時間遅れています。"
                                else "現地時間より${diffHours}時間${diffMinutes}分遅れています。"
                    }
                    "$greeting $cityName の現在時刻は、$ampm $h12 時 $m 分です。$diffStr"
                }
                SupportedVoiceLanguage.FRENCH -> {
                    val greeting = when (h24) {
                        in 5..17 -> "Bonjour!"
                        else -> "Bonsoir!"
                    }
                    val diffStr = when {
                        isSame -> "Il n'y a pas de décalage horaire."
                        isAhead -> if (diffMinutes == 0) "$cityName est en avance de $diffHours heures."
                                   else "$cityName est en avance de $diffHours heures et $diffMinutes minutes."
                        else -> if (diffMinutes == 0) "$cityName est en retard de $diffHours heures."
                                else "$cityName est en retard de $diffHours heures et $diffMinutes minutes."
                    }
                    "$greeting À $cityName, il est actuellement $h24 heures $m. $diffStr"
                }
                SupportedVoiceLanguage.SPANISH -> {
                    val greeting = when (h24) {
                        in 5..12 -> "¡Buenos días!"
                        in 13..19 -> "¡Buenas tardes!"
                        else -> "¡Buenas noches!"
                    }
                    val diffStr = when {
                        isSame -> "Es la misma hora que su zona local."
                        isAhead -> if (diffMinutes == 0) "$cityName tiene $diffHours horas por delante."
                                   else "$cityName tiene $diffHours horas y $diffMinutes minutos por delante."
                        else -> if (diffMinutes == 0) "$cityName tiene $diffHours horas por detrás."
                                else "$cityName tiene $diffHours horas y $diffMinutes minutos por detrás."
                    }
                    "$greeting En $cityName son las $h24 horas y $m minutos. $diffStr"
                }
                SupportedVoiceLanguage.GERMAN -> {
                    val greeting = when (h24) {
                        in 5..10 -> "Guten Morgen!"
                        in 11..17 -> "Guten Tag!"
                        else -> "Guten Abend!"
                    }
                    val diffStr = when {
                        isSame -> "Keine Zeitverschiebung zu Ihrer Ortszeit."
                        isAhead -> if (diffMinutes == 0) "$cityName ist Ihrer Ortszeit $diffHours Stunden voraus."
                                   else "$cityName ist Ihrer Ortszeit $diffHours Stunden und $diffMinutes Minuten voraus."
                        else -> if (diffMinutes == 0) "$cityName ist Ihrer Ortszeit $diffHours Stunden hinterher."
                                else "$cityName ist Ihrer Ortszeit $diffHours Stunden und $diffMinutes Minuten hinterher."
                    }
                    "$greeting In $cityName ist es jetzt $h24 Uhr $m. $diffStr"
                }
            }
        }
    }

    fun initialize(context: Context) {
        if (isInitialized && tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeakingState.value = true
                    }
                    override fun onDone(utteranceId: String?) {
                        _isSpeakingState.value = false
                    }
                    override fun onError(utteranceId: String?) {
                        _isSpeakingState.value = false
                    }
                })
            }
        }
    }

    fun speak(
        text: String,
        language: SupportedVoiceLanguage = SupportedVoiceLanguage.ENGLISH,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ) {
        tts?.let { engine ->
            engine.language = language.locale
            engine.setPitch(pitch)
            engine.setSpeechRate(speechRate)
            val utteranceId = "utterance_${System.currentTimeMillis()}"
            _isSpeakingState.value = true
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeakingState.value = false
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeakingState.value = false
    }
}
