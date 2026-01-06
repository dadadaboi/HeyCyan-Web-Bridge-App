# HeyCyan Glasses Capacitor Plugin

This Capacitor plugin provides an interface between web applications (created in Lovable) and HeyCyan smart glasses through the HeyCyan Web Bridge Android app.

## Overview

This plugin integrates with the existing HeyCyan Web Bridge app to provide access to HeyCyan smart glasses functionality from web applications. The plugin communicates with the HeyCyan SDK through the existing WebCommunicationService in the HeyCyan Web Bridge app.

## Architecture

```
Lovable Web App ←→ Capacitor Plugin ←→ HeyCyan Web Bridge App ←→ HeyCyan Glasses
     ↑                    ↑                       ↑                    ↑
  Capacitor        Native Android        WebCommunication      Bluetooth
  Bridge           Plugin                Service + EventBus    SDK
```

## Features

- Full access to HeyCyan glasses functionality from web applications
- All 24 methods defined in the handoff specification
- Event-based communication with real-time updates
- Permission handling for Bluetooth, location, camera, and microphone
- Integration with existing HeyCyan Web Bridge infrastructure

## Installation

### Prerequisites

- Android Studio with the HeyCyan Web Bridge app installed
- HeyCyan SDK AAR file in the Web Bridge app
- Capacitor project set up for your Lovable web application

### Installation Steps

1. Copy the plugin files to your Capacitor project:

   ```
   src/
   └── plugins/
       ├── heycyan/
       │   ├── definitions.ts
       │   ├── index.ts
       │   ├── web.ts
       │   └── android/
       │       └── src/main/java/com/heycyan/glasses/
       │           └── HeyCyanGlassesPlugin.kt
   ```

2. Update your Capacitor project's `capacitor.config.json` to include the plugin:

   ```json
   {
     "plugins": {
       "HeyCyanGlasses": {
         "permissions": [
           "bluetoothConnect",
           "bluetoothScan",
           "location",
           "camera",
           "microphone"
         ]
       }
     }
   }
   ```

3. Add the plugin to your Android project's `app/src/main/java/.../MainActivity.java`:

   ```java
   import com.heycyan.glasses.HeyCyanGlassesPlugin;

   public class MainActivity extends BridgeActivity {
       @Override
       public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);

           // Register the HeyCyan Glasses plugin
           registerPlugin(HeyCyanGlassesPlugin.class);
       }
   }
   ```

4. Add required permissions to `app/src/main/AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.RECORD_AUDIO" />
   <uses-permission android:name="android.permission.INTERNET" />
   ```

## Usage

### TypeScript/JavaScript Usage

```typescript
import { HeyCyanGlasses } from "./plugins/heycyan";

// Start scanning for glasses
await HeyCyanGlasses.startScan();

// Listen for device found events
HeyCyanGlasses.addListener("deviceFound", (device) => {
  console.log("Found device:", device);
  // Connect to the device
  HeyCyanGlasses.connect({ deviceId: device.id });
});

// Take a photo
const result = await HeyCyanGlasses.takePhoto();
console.log("Photo taken:", result.path);

// Get battery information
const battery = await HeyCyanGlasses.getBattery();
console.log("Battery level:", battery.level);
```

### Available Methods

#### Scanning & Connection

- `startScan()` - Start scanning for HeyCyan glasses
- `stopScan()` - Stop scanning
- `connect(options: { deviceId: string })` - Connect to a device
- `disconnect()` - Disconnect from current device
- `getConnectionState()` - Get current connection state

#### Device Information

- `getBattery()` - Get battery level and charging status
- `getMediaInfo()` - Get media counts (photos, videos, recordings)
- `getVolume()` - Get current volume level
- `getDeviceMode()` - Get current device mode

#### Device Control

- `setDeviceMode(options: { mode: DeviceMode })` - Set device mode
- `setVolume(options: { volume: number })` - Set volume level

#### Media Capture

- `takePhoto()` - Take a photo
- `startVideo()` - Start video recording
- `stopVideo()` - Stop video recording
- `startAudioRecording()` - Start audio recording
- `stopAudioRecording()` - Stop audio recording

#### AI Features

- `startAIRecognition(options: { type: AIRecognitionType })` - Start AI recognition
- `stopAIRecognition()` - Stop AI recognition
- `startTranslation(options: TranslationOptions)` - Start translation
- `stopTranslation()` - Stop translation

#### Voice & Transfer

- `setVoiceWakeup(options: { enabled: boolean; keyword?: string })` - Set voice wakeup
- `startWiFiTransfer()` - Start WiFi transfer
- `stopWiFiTransfer()` - Stop WiFi transfer

### Event Listeners

- `'deviceFound'` - Emitted when a device is found during scanning
- `'connectionChanged'` - Emitted when connection state changes
- `'batteryChanged'` - Emitted when battery level changes
- `'aiResult'` - Emitted when AI recognition completes
- `'translationResult'` - Emitted when translation completes
- `'transferProgress'` - Emitted during WiFi transfer

## Integration with Lovable Web App

The plugin is designed to work seamlessly with web applications created in Lovable:

1. The web app can call plugin methods directly
2. Events from the glasses are pushed to the web app in real-time
3. The existing HeyCyan Web Bridge app handles the SDK communication
4. No additional native development needed in Lovable

## Building and Testing

1. Build the web assets in Lovable:

   ```bash
   npm run build
   ```

2. Sync to native platforms:

   ```bash
   npx cap sync
   ```

3. Open in Android Studio:

   ```bash
   npx cap open android
   ```

4. Build and run the app on a device with HeyCyan glasses nearby

## Troubleshooting

- Make sure the HeyCyan Web Bridge app is installed and running
- Ensure Bluetooth permissions are granted to the app
- Verify the HeyCyan SDK AAR file is properly integrated
- Check that the WebCommunicationService is running in the HeyCyan Web Bridge app
- Monitor Android logs for any connection or communication errors

## Notes

- The plugin reuses the existing HeyCyan SDK integration from the Web Bridge app
- All Bluetooth communication is handled by the existing infrastructure
- The plugin provides a clean Capacitor interface to the existing functionality
- Web mock implementation is included for browser testing
- Real device testing requires physical HeyCyan glasses
