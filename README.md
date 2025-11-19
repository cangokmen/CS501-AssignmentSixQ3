# Assignment Six – Q3: Sound Meter (Decibel Level Detector)

## Overview
**Sound Meter** is an Android app that measures real-time noise levels using the device **microphone**.  
It converts raw audio amplitude into **decibel (dB)** values and displays them through a dynamic visual meter.  
The app also alerts the user when sound exceeds a configurable noise threshold.

---

## Features
- 🎤 **Microphone Input**
  - Captures audio data using **AudioRecord**
  - Continuously reads raw amplitude values

- 📏 **Decibel Conversion**
  - Converts amplitude → dB using logarithmic scaling  
  - Shows real-time decibel readings

- 📊 **Visual Sound Level Meter**
  - Dynamic bar / progress-based visualization  
  - Color-coded levels (green → yellow → red)

- 🚨 **Noise Alert**
  - Warns the user when dB exceeds a set threshold  
  - Can be implemented using UI highlight, vibration, or Snackbar

- 💡 **Responsive UI**
  - Smooth updates using coroutines + StateFlow  
  - Reactive Compose UI

---

## How It Works
1. **AudioRecord** buffers short PCM audio samples from the microphone.  
2. The app computes the **root mean square (RMS)** amplitude.  
3. RMS amplitude is converted to decibels with:  
   \[
   dB = 20 \times \log_{10}(\text{amplitude})
   \]
4. UI updates in real time through **StateFlow** collected in Compose.  
5. If dB > threshold (e.g., 85 dB), the app displays a visual alert.

---

## How to Run
```bash
git clone https://github.com/cangokmen/CS501-AssignmentSixQ3
# Open in Android Studio and run on a physical device
# (Microphone access does not work on most emulators)
```

## How to Use
1. **Launch the app** and grant microphone permission.
2. **Speak, clap, or make noise** — the sound meter will update instantly.
3. Watch the:
   - Real-time **dB value**
   - **Color bar** expanding with louder sounds
4. If the noise exceeds the threshold, an **alert** will appear.
