package com.example.assignmentsixq3

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * ViewModel responsible for managing the audio recording state and calculating decibel levels.
 * It abstracts the audio recording logic away from the UI (MainActivity).
 */
class SoundViewModel : ViewModel() {
    // Holds the calculated decibel value. It's a mutable state to trigger UI recomposition.
    // Initialized to -120.0, representing silence in dBFS.
    val decibel = mutableStateOf(-120.0)

    // Tracks whether the microphone is currently recording.
    val isRecording = mutableStateOf(false)

    // The core Android class for receiving audio from the microphone.
    private var audioRecord: AudioRecord? = null

    // --- Audio Configuration Parameters ---
    private val sampleRate = 44100 // Standard sample rate for digital audio.
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO // Mono recording is sufficient for level measurement.
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT // Standard 16-bit audio encoding.
    // Calculate the minimum buffer size needed for the audio stream.
    private var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    /**
     * Starts the audio recording process.
     * This function is launched on a background thread to avoid blocking the main UI thread.
     */
    @SuppressLint("MissingPermission")
    fun startRecording(activity: MainActivity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // Initialize AudioRecord with the specified configuration.
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording.value = true
        // Launch a coroutine in the IO dispatcher for background processing.
        viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize) // Buffer to hold audio samples.
            // Loop continuously while recording is active.
            while (isRecording.value) {
                // Read audio data from the microphone into the buffer.
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    // --- Decibel Calculation ---

                    // 1. Calculate the Root Mean Square (RMS) of the audio buffer.
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        // Square the sample value and add it to the sum.
                        sum += buffer[i].toDouble() * buffer[i].toDouble()
                    }
                    val rms = sqrt(sum / readSize) // The square root of the average of the squares.

                    // 2. Define the reference amplitude. For 16-bit PCM, the max value is 32767.
                    // This is our 0 dB reference (Full Scale).
                    val referenceAmplitude = 32767.0

                    // 3. Calculate the decibel value in dBFS (Decibels relative to Full Scale).
                    // The result is negative, where 0 is the maximum possible level.
                    val dbValue = if (rms > 0) {
                        20 * log10(rms / referenceAmplitude)
                    } else {
                        -120.0 // Use a floor value to represent silence and avoid log(0).
                    }

                    decibel.value = (dbValue + 80.0).coerceAtLeast(0.0)
                }
            }
        }
    }

    fun stopRecording() {
        if (isRecording.value) {
            isRecording.value = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            decibel.value = 0.0
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}
