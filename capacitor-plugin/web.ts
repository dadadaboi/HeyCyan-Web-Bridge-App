import { WebPlugin, registerPlugin } from "@capacitor/core";

import type {
  HeyCyanGlassesPlugin,
  BatteryInfo,
  MediaInfo,
  DeviceMode,
  PhotoResult,
  VideoResult,
  AudioResult,
  AIRecognitionType,
  TranslationOptions,
  TransferResult,
  ConnectionState,
} from "./definitions";

export class HeyCyanGlassesWeb
  extends WebPlugin
  implements HeyCyanGlassesPlugin
{
  async startScan(): Promise<void> {
    console.log("HeyCyanGlassesWeb: startScan");
    // Simulate finding a device after 2 seconds
    setTimeout(() => {
      this.notifyListeners("deviceFound", {
        device: {
          id: "mock-device-123",
          name: "HeyCyan Glasses",
          rssi: -65,
        },
      });
    }, 2000);
  }

  async stopScan(): Promise<void> {
    console.log("HeyCyanGlassesWeb: stopScan");
  }

  async connect(options: { deviceId: string }): Promise<void> {
    console.log("HeyCyanGlassesWeb: connect", options);
    this.notifyListeners("connectionChanged", {
      state: "connected" as ConnectionState,
      device: { id: options.deviceId, name: "HeyCyan Glasses", rssi: -65 },
    });
  }

  async disconnect(): Promise<void> {
    console.log("HeyCyanGlassesWeb: disconnect");
    this.notifyListeners("connectionChanged", { state: "disconnected" });
  }

  async getConnectionState(): Promise<{ state: ConnectionState }> {
    console.log("HeyCyanGlassesWeb: getConnectionState");
    return { state: "connected" };
  }

  async getBattery(): Promise<BatteryInfo> {
    console.log("HeyCyanGlassesWeb: getBattery");
    return { level: 85, isCharging: false };
  }

  async getMediaInfo(): Promise<MediaInfo> {
    console.log("HeyCyanGlassesWeb: getMediaInfo");
    return { photos: 12, videos: 5, recordings: 3 };
  }

  async getVolume(): Promise<{ volume: number }> {
    console.log("HeyCyanGlassesWeb: getVolume");
    return { volume: 75 };
  }

  async getDeviceMode(): Promise<{ mode: DeviceMode }> {
    console.log("HeyCyanGlassesWeb: getDeviceMode");
    return { mode: "idle" };
  }

  async setDeviceMode(options: { mode: DeviceMode }): Promise<void> {
    console.log("HeyCyanGlassesWeb: setDeviceMode", options);
  }

  async setVolume(options: { volume: number }): Promise<void> {
    console.log("HeyCyanGlassesWeb: setVolume", options);
  }

  async takePhoto(): Promise<PhotoResult> {
    console.log("HeyCyanGlassesWeb: takePhoto");
    return {
      path: "/mock/photos/photo_" + Date.now() + ".jpg",
      timestamp: Date.now(),
    };
  }

  async startVideo(): Promise<void> {
    console.log("HeyCyanGlassesWeb: startVideo");
  }

  async stopVideo(): Promise<VideoResult> {
    console.log("HeyCyanGlassesWeb: stopVideo");
    return {
      path: "/mock/videos/video_" + Date.now() + ".mp4",
      duration: 10,
      timestamp: Date.now(),
    };
  }

  async startAudioRecording(): Promise<void> {
    console.log("HeyCyanGlassesWeb: startAudioRecording");
  }

  async stopAudioRecording(): Promise<AudioResult> {
    console.log("HeyCyanGlassesWeb: stopAudioRecording");
    return {
      path: "/mock/recordings/recording_" + Date.now() + ".mp3",
      duration: 5,
      timestamp: Date.now(),
    };
  }

  async startAIRecognition(options: {
    type: AIRecognitionType;
  }): Promise<void> {
    console.log("HeyCyanGlassesWeb: startAIRecognition", options);
    // Simulate AI result after 3 seconds
    setTimeout(() => {
      this.notifyListeners("aiResult", {
        type: options.type,
        result: "Simulated AI recognition result",
        confidence: 0.95,
      });
    }, 3000);
  }

  async stopAIRecognition(): Promise<void> {
    console.log("HeyCyanGlassesWeb: stopAIRecognition");
  }

  async startTranslation(options: TranslationOptions): Promise<void> {
    console.log("HeyCyanGlassesWeb: startTranslation", options);
    // Simulate translation result after 2 seconds
    setTimeout(() => {
      this.notifyListeners("translationResult", {
        originalText: options.text || "Hello world",
        translatedText: "Simulated translation result",
      });
    }, 2000);
  }

  async stopTranslation(): Promise<void> {
    console.log("HeyCyanGlassesWeb: stopTranslation");
  }

  async setVoiceWakeup(options: {
    enabled: boolean;
    keyword?: string;
  }): Promise<void> {
    console.log("HeyCyanGlassesWeb: setVoiceWakeup", options);
  }

  async startWiFiTransfer(): Promise<TransferResult> {
    console.log("HeyCyanGlassesWeb: startWiFiTransfer");
    // Simulate transfer progress
    let progress = 0;
    const interval = setInterval(() => {
      progress += 10;
      if (progress >= 100) {
        clearInterval(interval);
        this.notifyListeners("transferProgress", { progress: 100 });
        return;
      }
      this.notifyListeners("transferProgress", {
        progress,
        currentFile: "file_" + progress + ".jpg",
      });
    }, 500);

    return {
      progress: 0,
      totalFiles: 5,
      transferredFiles: 0,
    };
  }

  async stopWiFiTransfer(): Promise<void> {
    console.log("HeyCyanGlassesWeb: stopWiFiTransfer");
  }
}
