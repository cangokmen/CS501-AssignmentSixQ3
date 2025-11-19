package com.example.assignmentsixq3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.assignmentsixq3.ui.theme.AssignmentSixQ3Theme

class MainActivity : ComponentActivity() {

    private val soundViewModel: SoundViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                soundViewModel.startRecording(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssignmentSixQ3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoundMeterScreen(soundViewModel, ::requestAudioPermission)
                }
            }
        }
    }

    private fun requestAudioPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) -> {
                soundViewModel.startRecording(this)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        soundViewModel.stopRecording()
    }
}

@Composable
fun SoundMeterScreen(soundViewModel: SoundViewModel, onStartRecording: () -> Unit) {
    val decibels by soundViewModel.decibel
    val isRecording by soundViewModel.isRecording
    val noiseThreshold = 85.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sound Meter",
            fontSize = 40.sp,
        )
        Spacer(modifier = Modifier.height(32.dp))

        SoundLevelIndicator(decibels = decibels, threshold = noiseThreshold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "%.2f dB".format(decibels),
            fontSize = 28.sp,
            color = if (decibels > noiseThreshold) Color.Red else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (decibels > noiseThreshold) {
            Text(
                text = "Alert: Noise level is too high!",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = {
            if (isRecording) {
                soundViewModel.stopRecording()
            } else {
                onStartRecording()
            }
        }) {
            Text(if (isRecording) "Stop Measuring" else "Start Measuring")
        }
    }
}

@Composable
fun SoundLevelIndicator(decibels: Double, threshold: Double) {
    val progress = (decibels / 120).toFloat().coerceIn(0f, 1f) // Assuming max 120 dB
    val color = when {
        decibels > threshold -> Color.Red
        decibels > threshold * 0.7 -> Color.Yellow
        else -> Color.Green
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Text(text = "0 dB", modifier = Modifier.align(Alignment.Start))
    }
}
