package com.harichselvamc.seetime.util

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build

data class MorsePulse(
    val isHigh: Boolean, // true = light/tone ON, false = silence/darkness
    val durationMs: Long,
    val sourceChar: Char? = null,
    val morseSymbol: String? = null // ".", "-", or " "
)

data class MorseCharEntry(
    val char: Char,
    val morse: String,
    val description: String
)

object MorseTimeEngine {

    val ITU_MORSE_MAP = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        ':' to "---...", '/' to "-..-.", '.' to ".-.-.-", '-' to "-....-",
        '?' to "..--..", '!' to "-.-.--", '@' to ".--.-."
    )

    val CHEAT_SHEET_ENTRIES: List<MorseCharEntry> = ITU_MORSE_MAP.map { (char, morse) ->
        MorseCharEntry(char, morse, "Character '$char'")
    }.sortedBy { it.char }

    /**
     * Converts a raw text string into standard Morse notation (e.g. "... --- ...").
     */
    fun textToMorseNotation(text: String): String {
        return text.uppercase().map { char ->
            if (char == ' ') " / "
            else ITU_MORSE_MAP[char] ?: ""
        }.filter { it.isNotEmpty() }.joinToString(" ")
    }

    /**
     * Encodes [text] into a precise sequence of timed high/low pulses based on standard Farnsworth [wpm].
     */
    fun encodeTextToPulses(text: String, wpm: Int = 15): List<MorsePulse> {
        val safeWpm = wpm.coerceIn(5, 45)
        val unitMs = (1200L / safeWpm).coerceIn(25L, 300L) // 1 dit duration

        val pulses = mutableListOf<MorsePulse>()
        val words = text.uppercase().trim().split("\\s+".toRegex())

        words.forEachIndexed { wordIdx, word ->
            word.forEachIndexed { charIdx, char ->
                val morseCode = ITU_MORSE_MAP[char]
                if (morseCode != null) {
                    morseCode.forEachIndexed { elemIdx, elem ->
                        // High pulse: Dit = 1 unit, Dah = 3 units
                        val highDuration = if (elem == '.') unitMs else unitMs * 3L
                        pulses.add(MorsePulse(isHigh = true, durationMs = highDuration, sourceChar = char, morseSymbol = elem.toString()))

                        // Intra-element space = 1 unit (between dits and dahs in same letter)
                        if (elemIdx < morseCode.length - 1) {
                            pulses.add(MorsePulse(isHigh = false, durationMs = unitMs, sourceChar = char, morseSymbol = ""))
                        }
                    }

                    // Inter-character space = 3 units (between letters in same word)
                    if (charIdx < word.length - 1) {
                        pulses.add(MorsePulse(isHigh = false, durationMs = unitMs * 3L, sourceChar = ' ', morseSymbol = " "))
                    }
                }
            }

            // Inter-word space = 7 units (between words)
            if (wordIdx < words.size - 1) {
                pulses.add(MorsePulse(isHigh = false, durationMs = unitMs * 7L, sourceChar = ' ', morseSymbol = " / "))
            }
        }

        return pulses
    }

    /**
     * Calculates total duration of a pulse sequence in milliseconds.
     */
    fun calculateTotalDurationMs(pulses: List<MorsePulse>): Long {
        return pulses.sumOf { it.durationMs }
    }

    /**
     * Safely toggles camera torch mode on supported hardware.
     */
    fun setCameraTorchMode(context: Context, enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
                val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
                cameraManager.setTorchMode(cameraId, enabled)
            } catch (_: Exception) {
                // Ignore emulator / devices without flash hardware
            }
        }
    }
}
