package com.example.service.tracking

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class EyeHeadTrackingManager(private val context: Context) {

    private val tag = "EyeHeadTrackingManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var cameraExecutor: ExecutorService? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var faceDetector: FaceDetector? = null

    private val _config = MutableStateFlow(TrackingConfig())
    val config: StateFlow<TrackingConfig> = _config.asStateFlow()

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private val _actionEvents = MutableSharedFlow<TrackingAction>(extraBufferCapacity = 8)
    val actionEvents: SharedFlow<TrackingAction> = _actionEvents.asSharedFlow()

    // Internal timing & gesture tracking
    private var lastGestureTime = 0L
    private var lastActionTime = 0L
    private var consecutiveLookingAwayFrames = 0
    private var consecutiveFacingFrames = 0
    private var bothEyesClosedStartTime = 0L
    private var leftWinkStartTime = 0L
    private var rightWinkStartTime = 0L
    private var headTiltStartTime = 0L
    private var activeTiltDirection = 0 // -1 left, 1 right, 0 neutral
    private var headNodStartTime = 0L

    private fun getOrCreateFaceDetector(): FaceDetector? {
        if (faceDetector != null) return faceDetector
        return try {
            com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(context.applicationContext)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build()
            FaceDetection.getClient(options).also { faceDetector = it }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize MLKit FaceDetector", e)
            null
        }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(isCameraPermissionGranted = granted)
    }

    fun updateConfig(block: (TrackingConfig) -> TrackingConfig) {
        val newConfig = block(_config.value)
        _config.value = newConfig
    }

    fun toggleTracking(enable: Boolean, lifecycleOwner: LifecycleOwner? = null) {
        _config.value = _config.value.copy(isTrackingEnabled = enable)
        if (enable) {
            if (lifecycleOwner != null) {
                startCamera(lifecycleOwner)
            }
        } else {
            stopCamera()
            _state.value = _state.value.copy(
                isTrackingActive = false,
                isFaceDetected = false,
                isLookingAtScreen = true,
                isPausedByAttention = false,
                lastGesture = TrackingGesture.NONE
            )
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        if (!_config.value.isTrackingEnabled) return
        stopCamera()

        val executor = Executors.newSingleThreadExecutor()
        cameraExecutor = executor

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    processImageProxy(imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                provider.unbindAll()
                if (provider.hasCamera(cameraSelector)) {
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
                    _state.value = _state.value.copy(isTrackingActive = true)
                } else {
                    Log.w(tag, "No front camera found on device")
                    _state.value = _state.value.copy(isTrackingActive = false)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to bind camera lifecycle", e)
                _state.value = _state.value.copy(isTrackingActive = false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            cameraExecutor?.shutdown()
            cameraExecutor = null
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down camera", e)
        }
        _state.value = _state.value.copy(isTrackingActive = false)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        val imageWidth = imageProxy.width
        val imageHeight = imageProxy.height

        getOrCreateFaceDetector()?.process(image)
            ?.addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    handleFaceDetected(faces.first(), imageWidth, imageHeight)
                } else {
                    handleNoFaceDetected()
                }
            }
            ?.addOnFailureListener { e ->
                Log.e(tag, "Face detection failure", e)
            }
            ?.addOnCompleteListener {
                imageProxy.close()
            } ?: imageProxy.close()
    }

    private fun handleFaceDetected(face: Face, frameWidth: Int, frameHeight: Int) {
        val now = System.currentTimeMillis()
        val currentConfig = _config.value
        val sensitivity = currentConfig.sensitivity

        val yaw = face.headEulerAngleY // negative = looking left, positive = looking right
        val pitch = face.headEulerAngleX // positive = looking up, negative = nodding down
        val roll = face.headEulerAngleZ // negative = tilt left, positive = tilt right

        val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f

        // Calculate face distance estimation based on bounding box ratio
        val bounds = face.boundingBox
        val faceArea = (bounds.width().coerceAtLeast(1) * bounds.height().coerceAtLeast(1)).toFloat()
        val totalArea = (frameWidth * frameHeight).toFloat().coerceAtLeast(1f)
        val coverageRatio = (faceArea / totalArea).coerceIn(0f, 1f)
        val isTooClose = coverageRatio > 0.36f

        // Smooth head pointer calculation
        val currentPointerX = _state.value.pointerX
        val currentPointerY = _state.value.pointerY
        // Mirror yaw for front camera interaction
        val targetPointerX = (0.5f - (yaw / 35f)).coerceIn(0.05f, 0.95f)
        val targetPointerY = (0.5f - (pitch / 30f)).coerceIn(0.05f, 0.95f)
        val smoothedPointerX = currentPointerX + (targetPointerX - currentPointerX) * 0.35f
        val smoothedPointerY = currentPointerY + (targetPointerY - currentPointerY) * 0.35f

        // Check if looking at screen
        val isFacingScreen = abs(yaw) < sensitivity.yawThreshold && abs(pitch) < sensitivity.pitchThreshold

        if (isFacingScreen) {
            consecutiveFacingFrames++
            consecutiveLookingAwayFrames = 0
        } else {
            consecutiveLookingAwayFrames++
            consecutiveFacingFrames = 0
        }

        var newLastGesture = _state.value.lastGesture
        var triggeredAction: TrackingAction? = null
        var actionMsg: String? = null

        // 1. SMART ATTENTION PAUSE / RESUME
        if (currentConfig.isSmartAttentionEnabled) {
            if (!isFacingScreen && consecutiveLookingAwayFrames >= 3 && _state.value.isLookingAtScreen) {
                _state.value = _state.value.copy(isLookingAtScreen = false, isPausedByAttention = true)
                triggeredAction = TrackingAction.PAUSE_ATTENTION
                newLastGesture = TrackingGesture.LOOKING_AWAY
                actionMsg = "Attention Pause: Looking Away"
            } else if (isFacingScreen && consecutiveFacingFrames >= 2 && !_state.value.isLookingAtScreen) {
                _state.value = _state.value.copy(isLookingAtScreen = true, isPausedByAttention = false)
                triggeredAction = TrackingAction.RESUME_ATTENTION
                newLastGesture = TrackingGesture.ATTENTION_RESUMED
                actionMsg = "Attention Resumed: Welcome Back"
            }
        }

        // 2. EYE POSTURE / DISTANCE ALERT
        if (currentConfig.isDistanceAlertEnabled && isTooClose && !_state.value.isTooCloseToScreen) {
            newLastGesture = TrackingGesture.TOO_CLOSE
            actionMsg = "Eye Safety: Move screen further away"
        }

        // 3. HEAD TILT SEEKING
        val cooldown = 1400L
        if (currentConfig.isHeadTiltSeekEnabled && (now - lastActionTime > cooldown)) {
            if (roll > sensitivity.rollThreshold) {
                // Tilted Right -> Seek Forward
                if (activeTiltDirection == 1) {
                    if (headTiltStartTime == 0L) headTiltStartTime = now
                    if (now - headTiltStartTime > 280L) {
                        triggeredAction = TrackingAction.SEEK_FORWARD_10S
                        newLastGesture = TrackingGesture.HEAD_TILT_RIGHT
                        actionMsg = "Head Tilt Right: +10s"
                        lastActionTime = now
                        headTiltStartTime = 0L
                    }
                } else {
                    activeTiltDirection = 1
                    headTiltStartTime = now
                }
            } else if (roll < -sensitivity.rollThreshold) {
                // Tilted Left -> Seek Backward
                if (activeTiltDirection == -1) {
                    if (headTiltStartTime == 0L) headTiltStartTime = now
                    if (now - headTiltStartTime > 280L) {
                        triggeredAction = TrackingAction.SEEK_BACKWARD_10S
                        newLastGesture = TrackingGesture.HEAD_TILT_LEFT
                        actionMsg = "Head Tilt Left: -10s"
                        lastActionTime = now
                        headTiltStartTime = 0L
                    }
                } else {
                    activeTiltDirection = -1
                    headTiltStartTime = now
                }
            } else {
                activeTiltDirection = 0
                headTiltStartTime = 0L
            }
        }

        // 4. HEAD NOD (Play / Pause)
        if (currentConfig.isTrackingEnabled && (now - lastActionTime > 1600L)) {
            if (pitch < -sensitivity.pitchThreshold && abs(yaw) < 15f) {
                if (headNodStartTime == 0L) headNodStartTime = now
                if (now - headNodStartTime > 300L) {
                    triggeredAction = TrackingAction.TOGGLE_PLAY_PAUSE
                    newLastGesture = TrackingGesture.HEAD_NOD_DOWN
                    actionMsg = "Head Nod: Play/Pause"
                    lastActionTime = now
                    headNodStartTime = 0L
                }
            } else {
                headNodStartTime = 0L
            }
        }

        // 5. EYE BLINK & WINK CONTROLS
        if (currentConfig.isBlinkControlEnabled && (now - lastActionTime > 1200L)) {
            val bothClosed = leftEyeOpen < 0.22f && rightEyeOpen < 0.22f
            val leftWink = leftEyeOpen < 0.20f && rightEyeOpen > 0.65f
            val rightWink = rightEyeOpen < 0.20f && leftEyeOpen > 0.65f

            if (bothClosed) {
                if (bothEyesClosedStartTime == 0L) bothEyesClosedStartTime = now
            } else {
                if (bothEyesClosedStartTime > 0L) {
                    val blinkDuration = now - bothEyesClosedStartTime
                    bothEyesClosedStartTime = 0L
                    if (blinkDuration in 220L..750L) {
                        triggeredAction = TrackingAction.TOGGLE_PLAY_PAUSE
                        newLastGesture = TrackingGesture.DOUBLE_BLINK
                        actionMsg = "Eye Blink: Play/Pause"
                        lastActionTime = now
                    }
                }
            }

            if (leftWink) {
                if (leftWinkStartTime == 0L) leftWinkStartTime = now
                if (now - leftWinkStartTime > 350L) {
                    triggeredAction = TrackingAction.SEEK_BACKWARD_10S
                    newLastGesture = TrackingGesture.WINK_LEFT
                    actionMsg = "Left Eye Wink: -10s"
                    lastActionTime = now
                    leftWinkStartTime = 0L
                }
            } else {
                leftWinkStartTime = 0L
            }

            if (rightWink) {
                if (rightWinkStartTime == 0L) rightWinkStartTime = now
                if (now - rightWinkStartTime > 350L) {
                    triggeredAction = TrackingAction.SEEK_FORWARD_10S
                    newLastGesture = TrackingGesture.WINK_RIGHT
                    actionMsg = "Right Eye Wink: +10s"
                    lastActionTime = now
                    rightWinkStartTime = 0L
                }
            } else {
                rightWinkStartTime = 0L
            }
        }

        _state.value = _state.value.copy(
            isFaceDetected = true,
            headPitch = pitch,
            headYaw = yaw,
            headRoll = roll,
            leftEyeOpenProb = leftEyeOpen,
            rightEyeOpenProb = rightEyeOpen,
            faceCoverageRatio = coverageRatio,
            isTooCloseToScreen = isTooClose,
            pointerX = smoothedPointerX,
            pointerY = smoothedPointerY,
            lastGesture = newLastGesture,
            lastAction = triggeredAction ?: _state.value.lastAction,
            lastActionMessage = actionMsg ?: _state.value.lastActionMessage,
            lastActionTimestamp = if (actionMsg != null) now else _state.value.lastActionTimestamp
        )

        if (triggeredAction != null) {
            _actionEvents.tryEmit(triggeredAction)
        }
    }

    private fun handleNoFaceDetected() {
        val now = System.currentTimeMillis()
        consecutiveLookingAwayFrames++
        consecutiveFacingFrames = 0

        var triggeredAction: TrackingAction? = null
        var actionMsg: String? = null

        if (_config.value.isSmartAttentionEnabled && consecutiveLookingAwayFrames >= 5 && _state.value.isLookingAtScreen) {
            _state.value = _state.value.copy(isLookingAtScreen = false, isPausedByAttention = true)
            triggeredAction = TrackingAction.PAUSE_ATTENTION
            actionMsg = "Attention Pause: User Stepped Away"
        }

        _state.value = _state.value.copy(
            isFaceDetected = false,
            lastAction = triggeredAction ?: _state.value.lastAction,
            lastActionMessage = actionMsg ?: _state.value.lastActionMessage,
            lastActionTimestamp = if (actionMsg != null) now else _state.value.lastActionTimestamp
        )

        if (triggeredAction != null) {
            _actionEvents.tryEmit(triggeredAction)
        }
    }

    // Direct gesture simulation for local unit testing and quick demo
    fun simulateGesture(gesture: TrackingGesture) {
        val now = System.currentTimeMillis()
        var action: TrackingAction = TrackingAction.NONE
        var msg = gesture.displayName

        when (gesture) {
            TrackingGesture.LOOKING_AWAY -> {
                action = TrackingAction.PAUSE_ATTENTION
                _state.value = _state.value.copy(isLookingAtScreen = false, isPausedByAttention = true)
            }
            TrackingGesture.ATTENTION_RESUMED -> {
                action = TrackingAction.RESUME_ATTENTION
                _state.value = _state.value.copy(isLookingAtScreen = true, isPausedByAttention = false)
            }
            TrackingGesture.HEAD_TILT_RIGHT -> {
                action = TrackingAction.SEEK_FORWARD_10S
                msg = "Head Tilt Right: +10s"
            }
            TrackingGesture.HEAD_TILT_LEFT -> {
                action = TrackingAction.SEEK_BACKWARD_10S
                msg = "Head Tilt Left: -10s"
            }
            TrackingGesture.HEAD_NOD_DOWN -> {
                action = TrackingAction.TOGGLE_PLAY_PAUSE
                msg = "Head Nod: Play/Pause"
            }
            TrackingGesture.DOUBLE_BLINK -> {
                action = TrackingAction.TOGGLE_PLAY_PAUSE
                msg = "Eye Blink: Play/Pause"
            }
            TrackingGesture.WINK_LEFT -> {
                action = TrackingAction.SEEK_BACKWARD_10S
                msg = "Left Wink: -10s"
            }
            TrackingGesture.WINK_RIGHT -> {
                action = TrackingAction.SEEK_FORWARD_10S
                msg = "Right Wink: +10s"
            }
            TrackingGesture.TOO_CLOSE -> {
                action = TrackingAction.DISTANCE_WARNING
                msg = "Eye Safety: Move screen further away"
                _state.value = _state.value.copy(isTooCloseToScreen = true)
            }
            else -> {}
        }

        _state.value = _state.value.copy(
            lastGesture = gesture,
            lastAction = action,
            lastActionMessage = msg,
            lastActionTimestamp = now
        )

        if (action != TrackingAction.NONE) {
            _actionEvents.tryEmit(action)
        }
    }

    fun resetAttentionPause() {
        _state.value = _state.value.copy(isPausedByAttention = false, isLookingAtScreen = true)
    }
}
