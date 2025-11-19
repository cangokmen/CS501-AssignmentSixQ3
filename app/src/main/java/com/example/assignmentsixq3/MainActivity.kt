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


/**
 * The main entry point of the application.
 * It's responsible for handling user permissions and displaying the UI.
 */
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

    /**
     * Checks for the RECORD_AUDIO permission. If granted, it starts recording.
     * If not, it launches the permission request dialog.
     */
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

    /**
     * Lifecycle method called when the activity is no longer visible.
     * We stop recording here to save battery and release the microphone.
     */
    override fun onStop() {
        super.onStop()
        soundViewModel.stopRecording()
    }
}


/**
 * The main Composable function that defines the UI for the sound meter screen.
 */
@Composable
fun SoundMeterScreen(soundViewModel: SoundViewModel, onStartRecording: () -> Unit) {
    // Observe the state values from the ViewModel.
    val decibels by soundViewModel.decibel
    val isRecording by soundViewModel.isRecording
    // Enforce a minimum value of 0.0 for the UI display.
    val displayDecibels = decibels.coerceAtLeast(0.0)
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

        SoundLevelIndicator(decibels = displayDecibels, threshold = noiseThreshold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "%.2f dB".format(displayDecibels),
            fontSize = 28.sp,
            // Change color to red if the noise level exceeds the threshold.
            color = if (displayDecibels > noiseThreshold) Color.Red else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (displayDecibels > noiseThreshold && isRecording) {
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

/**
 * A Composable that displays a visual progress bar representing the sound level.
 */
@Composable
fun SoundLevelIndicator(decibels: Double, threshold: Double) {
    // Map the [0, 120] dB range to a [0, 1] progress value.
    val progress = (decibels / 120).toFloat().coerceIn(0f, 1f)
    val color = when {
        decibels > threshold -> Color.Red
        decibels > threshold * 0.8 -> Color.Yellow // A reasonable range for "getting loud"
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
        // Add labels for the progress bar range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0 dB")
            Text(text = "120 dB")
        }
    }
}
