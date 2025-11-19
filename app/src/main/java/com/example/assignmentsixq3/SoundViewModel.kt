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

class SoundViewModel : ViewModel() {
    val decibel = mutableStateOf(0.0)
    val isRecording = mutableStateOf(false)
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun startRecording(activity: MainActivity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isRecording.value) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    // Calculate the Root Mean Square (RMS) of the audio buffer
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += buffer[i].toDouble() * buffer[i].toDouble()
                    }
                    val rms = sqrt(sum / readSize)

                    // Reference amplitude for 16-bit audio
                    val referenceAmplitude = 32767.0

                    // Calculate decibels (dBFS). The result is negative.
                    // Ensure division is floating-point by using a Double.
                    val dbValue = if (rms > 0) {
                        20 * log10(rms / referenceAmplitude)
                    } else {
                        -120.0 // A floor value for silence
                    }

                    // Shift the range from [-120, 0] to [0, 120] for a more
                    // user-friendly display in the UI.
                    decibel.value = dbValue + 80.0
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
