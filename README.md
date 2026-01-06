# HeyCyan Web Bridge Android App

This Android application bridges HeyCyan smart glasses with your web application via Bluetooth and web communication.

## Overview

This app extends the original HeyCyan SDK sample app to provide communication with web applications. It connects to HeyCyan smart glasses via Bluetooth and forwards commands and data between the glasses and your web application using WebSocket and HTTP APIs.

## Features

- Original HeyCyan SDK functionality (camera control, recording, battery monitoring, etc.)
- WebSocket communication with web applications
- Real-time data forwarding between glasses and web app
- Command execution from web application to glasses
- Status updates from glasses to web application
- Event-driven architecture using EventBus

## Architecture

```
Web Application ←→ Android App ←→ HeyCyan Glasses
     ↑                    ↑              ↑
WebSocket/HTTP      WebCommunication   Bluetooth
                   Service + EventBus
```

## Components

### WebCommunicationService

- Handles WebSocket connections to web applications
- Manages HTTP requests/responses
- Provides data forwarding between glasses and web app

### MainActivity

- Original HeyCyan SDK integration
- EventBus integration for communication
- Bluetooth connection management

### Event Classes

- GlassesCommandEvent - Commands from web app to glasses
- GlassesRequestEvent - Requests from web app for data
- GlassesDataEvent - Data from glasses to web app
- GlassesStatusEvent - Status updates to web app

## Web API Integration

The app communicates with your web application through:

1. **WebSocket Connection**: Real-time bidirectional communication

   - URL: `ws://your-web-app.com/ws/glasses`
   - Messages are JSON formatted

2. **Command Types**:

   - `{"type": "command", "command": "takePhoto", "params": {}}`
   - `{"type": "command", "command": "startRecording", "params": {}}`
   - `{"type": "command", "command": "stopRecording", "params": {}}`
   - `{"type": "command", "command": "getBattery", "params": {}}`

3. **Request Types**:

   - `{"type": "request", "request": "getDeviceInfo", "params": {}}`
   - `{"type": "request", "request": "getMediaCount", "params": {}}`

4. **Data Types Sent to Web App**:

   - `{"type": "data", "dataType": "battery", "data": {"level": 85, "isCharging": false}}`
   - `{"type": "data", "dataType": "deviceInfo", "data": {...}}`
   - `{"type": "data", "dataType": "mediaCount", "data": {...}}`

5. **Status Updates Sent to Web App**:
   - `{"type": "status", "status": "connection", "value": "connected"}`
   - `{"type": "status", "status": "camera", "value": "taking_photo"}`

## Configuration

To connect to your web application:

1. Update the URL in MainActivity.kt:

   ```kotlin
   webCommunicationService?.connectToWebApp("https://your-web-app.com")
   ```

2. Ensure your web application implements the WebSocket endpoint at `/ws/glasses`

## Building the App

1. Make sure you have Android Studio installed
2. Open the project in Android Studio
3. The HeyCyan SDK AAR file is already included in `app/libs/`
4. Build and run the application

## Permissions

The app requires the following permissions:

- Bluetooth permissions for connecting to glasses
- Internet permission for web communication
- Location permissions for Bluetooth scanning
- Storage permissions for media handling

## Usage

1. Install the app on an Android device
2. Pair with HeyCyan glasses via Bluetooth
3. The app will automatically connect to your web application
4. Control glasses functionality from your web interface
5. Receive real-time updates from glasses on your web application

## Troubleshooting

- Ensure Bluetooth permissions are granted
- Verify internet connectivity for web communication
- Check that your web application WebSocket endpoint is accessible
- Monitor logs for connection status and errors

## Dependencies

- HeyCyan SDK (included as AAR)
- OkHttp for HTTP/WebSocket communication
- Gson for JSON handling
- EventBus for internal communication
- Kotlin Coroutines for async operations

## Notes

- The app maintains the original HeyCyan SDK functionality while adding web communication
- All communication with the web app is optional - the app works standalone
- WebSocket reconnection is handled automatically
- Data is sent to the web app in real-time as it's received from glasses
