package com.example.ringrush.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural synthesizer for arcade sound effects.
 * Requires 0 external audio files, operates completely offline,
 * and produces clean retro-arcade audio tones.
 */
class SoundPlayer {
    private var enabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun isEnabled(): Boolean = enabled

    /**
     * Plays a crisp high-frequency chime when a ring is cleared.
     * Higher streaks pitch up smoothly.
     */
    fun playPassSound(streak: Int) {
        if (!enabled) return
        scope.launch {
            try {
                val baseFreq = 520.0 + ((streak % 12) * 45.0)
                generateTone(
                    startFreq = baseFreq,
                    endFreq = baseFreq * 1.35,
                    durationMs = 90,
                    volume = 0.55f
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays a celebratory dual-tone chord for streak milestones or perfect passes.
     */
    fun playStreakBonusSound() {
        if (!enabled) return
        scope.launch {
            try {
                generateTone(startFreq = 660.0, endFreq = 990.0, durationMs = 120, volume = 0.65f)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays a low impact thud when colliding with ring or closing.
     */
    fun playCollisionSound() {
        if (!enabled) return
        scope.launch {
            try {
                generateTone(startFreq = 160.0, endFreq = 65.0, durationMs = 180, volume = 0.75f)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays a subtle UI tap tone.
     */
    fun playClickSound() {
        if (!enabled) return
        scope.launch {
            try {
                generateTone(startFreq = 800.0, endFreq = 700.0, durationMs = 35, volume = 0.35f)
            } catch (_: Exception) {}
        }
    }

    private fun generateTone(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float
    ) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return

        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples.toDouble()
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val angle = 2.0 * Math.PI * i / (sampleRate / currentFreq)
            // Apply simple linear decay envelope to eliminate audio clicks
            val envelope = (1.0 - progress) * volume
            val sample = (sin(angle) * envelope * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(kotlin.math.max(buffer.size * 2, minBufferSize))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()

        // Release after playback finishes
        scope.launch {
            kotlinx.coroutines.delay(durationMs.toLong() + 50L)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}
