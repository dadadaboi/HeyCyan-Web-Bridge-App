export interface HeyCyanGlassesPlugin {
  /**
   * Start scanning for HeyCyan glasses
   */
  startScan(): Promise<void>;

  /**
   * Stop scanning for HeyCyan glasses
   */
  stopScan(): Promise<void>;

  /**
   * Connect to a HeyCyan glass device
   */
  connect(options: { deviceId: string }): Promise<void>;

  /**
   * Disconnect from the current HeyCyan glass device
   */
  disconnect(): Promise<void>;

  /**
   * Get the current connection state
   */
  getConnectionState(): Promise<{ state: ConnectionState }>;

  /**
   * Get battery information from the glasses
   */
  getBattery(): Promise<BatteryInfo>;

  /**
   * Get media information from the glasses
   */
  getMediaInfo(): Promise<MediaInfo>;

  /**
   * Get volume information from the glasses
   */
  getVolume(): Promise<{ volume: number }>;

  /**
   * Get current device mode
   */
  getDeviceMode(): Promise<{ mode: DeviceMode }>;

  /**
   * Set device mode
   */
  setDeviceMode(options: { mode: DeviceMode }): Promise<void>;

  /**
   * Set volume level
   */
  setVolume(options: { volume: number }): Promise<void>;

  /**
   * Take a photo with the glasses
   */
  takePhoto(): Promise<PhotoResult>;

  /**
   * Start video recording
   */
  startVideo(): Promise<void>;

  /**
   * Stop video recording
   */
  stopVideo(): Promise<VideoResult>;

  /**
   * Start audio recording
   */
  startAudioRecording(): Promise<void>;

  /**
   * Stop audio recording
   */
  stopAudioRecording(): Promise<AudioResult>;

  /**
   * Start AI recognition
   */
  startAIRecognition(options: { type: AIRecognitionType }): Promise<void>;

  /**
   * Stop AI recognition
   */
  stopAIRecognition(): Promise<void>;

  /**
   * Start translation
   */
  startTranslation(options: TranslationOptions): Promise<void>;

  /**
   * Stop translation
   */
  stopTranslation(): Promise<void>;

  /**
   * Set voice wakeup settings
   */
  setVoiceWakeup(options: {
    enabled: boolean;
    keyword?: string;
  }): Promise<void>;

  /**
   * Start WiFi transfer
   */
  startWiFiTransfer(): Promise<TransferResult>;

  /**
   * Stop WiFi transfer
   */
  stopWiFiTransfer(): Promise<void>;
}

export type ConnectionState =
  | "disconnected"
  | "connecting"
  | "connected"
  | "disconnecting";

export interface BatteryInfo {
  level: number; // 0-100
  isCharging: boolean;
}

export interface MediaInfo {
  photos: number;
  videos: number;
  recordings: number;
}

export type DeviceMode = "photo" | "video" | "audio" | "ai" | "idle";

export interface PhotoResult {
  path: string;
  timestamp: number;
}

export interface VideoResult {
  path: string;
  duration: number;
  timestamp: number;
}

export interface AudioResult {
  path: string;
  duration: number;
  timestamp: number;
}

export type AIRecognitionType = "object" | "text" | "face" | "scene";

export interface TranslationOptions {
  sourceLanguage: string;
  targetLanguage: string;
  text?: string;
}

export interface TransferResult {
  progress: number;
  totalFiles: number;
  transferredFiles: number;
}
