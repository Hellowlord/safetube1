package com.example.service.tracking

enum class TrackingGesture(val displayName: String, val icon: String) {
    NONE("Neutral", "👀"),
    LOOKING_AWAY("Looking Away", "🙈"),
    ATTENTION_RESUMED("Looking at Screen", "👁️"),
    HEAD_TILT_LEFT("Head Tilted Left (Rewind)", "⏪"),
    HEAD_TILT_RIGHT("Head Tilted Right (Forward)", "⏩"),
    HEAD_NOD_DOWN("Head Nod (Play/Pause)", "⏯️"),
    HEAD_TILT_UP("Head Up", "⬆️"),
    DOUBLE_BLINK("Double Blink (Play/Pause)", "😉"),
    WINK_LEFT("Left Eye Wink (Rewind)", "👈"),
    WINK_RIGHT("Right Eye Wink (Forward)", "👉"),
    TOO_CLOSE("Too Close to Screen", "⚠️")
}

enum class TrackingAction {
    NONE,
    TOGGLE_PLAY_PAUSE,
    PAUSE_ATTENTION,
    RESUME_ATTENTION,
    SEEK_FORWARD_10S,
    SEEK_BACKWARD_10S,
    DISTANCE_WARNING
}

enum class TrackingSensitivity(val label: String, val yawThreshold: Float, val pitchThreshold: Float, val rollThreshold: Float) {
    LOW("Low (Gentle)", 32f, 24f, 22f),
    MEDIUM("Medium (Balanced)", 24f, 18f, 16f),
    HIGH("High (Responsive)", 18f, 14f, 12f)
}

data class TrackingConfig(
    val isTrackingEnabled: Boolean = false,
    val isSmartAttentionEnabled: Boolean = true,
    val isHeadTiltSeekEnabled: Boolean = true,
    val isBlinkControlEnabled: Boolean = true,
    val isDistanceAlertEnabled: Boolean = true,
    val isPointerCursorEnabled: Boolean = false,
    val showLiveCameraPreview: Boolean = false,
    val showDebugHUD: Boolean = false,
    val sensitivity: TrackingSensitivity = TrackingSensitivity.MEDIUM
)

data class TrackingState(
    val isCameraPermissionGranted: Boolean = false,
    val isTrackingActive: Boolean = false,
    val isFaceDetected: Boolean = false,
    val isLookingAtScreen: Boolean = true,
    val leftEyeOpenProb: Float = 1.0f,
    val rightEyeOpenProb: Float = 1.0f,
    val headPitch: Float = 0f,
    val headYaw: Float = 0f,
    val headRoll: Float = 0f,
    val faceCoverageRatio: Float = 0f,
    val isTooCloseToScreen: Boolean = false,
    val pointerX: Float = 0.5f,
    val pointerY: Float = 0.5f,
    val lastGesture: TrackingGesture = TrackingGesture.NONE,
    val lastAction: TrackingAction = TrackingAction.NONE,
    val lastActionMessage: String? = null,
    val lastActionTimestamp: Long = 0L,
    val isPausedByAttention: Boolean = false
)
