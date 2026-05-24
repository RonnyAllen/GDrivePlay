package com.driveplay.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.driveplay.R
import com.driveplay.player.gestures.GestureCoordinator
import com.driveplay.player.gestures.GestureResponse
import com.driveplay.player.gestures.LockedGesture
import com.driveplay.ui.components.GestureOverlay
import com.driveplay.ui.components.PlaylistDrawer
import com.driveplay.ui.components.SpeedBottomSheet
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BufferedColor
import com.driveplay.ui.theme.PlayerTimecodeStyle
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    val uiState by viewModel.uiState.collectAsState()
    val resumePromptState by viewModel.resumePromptState.collectAsState()
    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val rememberSpeed by viewModel.rememberSpeed.collectAsState()
    val seekThumbnail by viewModel.currentThumbnail.collectAsState()

    val exoPlayer = remember { viewModel.exoPlayerManager.getOrCreatePlayer() }
    val gestureCoordinator = remember { GestureCoordinator(context, activity) }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var repeatMode by remember { mutableIntStateOf(exoPlayer.repeatMode) }
    var shuffleModeEnabled by remember { mutableStateOf(exoPlayer.shuffleModeEnabled) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }

            override fun onRepeatModeChanged(newRepeatMode: Int) {
                repeatMode = newRepeatMode
            }

            override fun onShuffleModeEnabledChanged(newShuffleModeEnabled: Boolean) {
                shuffleModeEnabled = newShuffleModeEnabled
            }
        }
        exoPlayer.addListener(listener)
        isPlaying = exoPlayer.isPlaying
        repeatMode = exoPlayer.repeatMode
        shuffleModeEnabled = exoPlayer.shuffleModeEnabled

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Control Overlays
    var controlsVisible by remember { mutableStateOf(true) }
    var screenLocked by remember { mutableStateOf(false) }
    var activeGesture by remember { mutableStateOf("none") }
    var brightnessPercent by remember { mutableStateOf(50) }
    var volumePercent by remember { mutableStateOf(50) }
    var seekTargetMs by remember { mutableLongStateOf(0L) }
    var videoDurationMs by remember { mutableLongStateOf(0L) }
    var videoPlayheadMs by remember { mutableLongStateOf(0L) }
    var videoBufferedMs by remember { mutableLongStateOf(0L) }

    // Dialog Sheets
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Immersive system bar controls
    DisposableEffect(Unit) {
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Lock to landscape by default for videos
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            viewModel.exoPlayerManager.releasePlayer()
        }
    }

    // Live Playback Ticks
    LaunchedEffect(exoPlayer) {
        while (true) {
            videoPlayheadMs = exoPlayer.currentPosition
            videoDurationMs = exoPlayer.duration.coerceAtLeast(0L)
            videoBufferedMs = exoPlayer.bufferedPosition
            
            // Throttle save bookmark every 5s
            if (uiState is PlayerUiState.Ready && videoDurationMs > 0) {
                val ready = uiState as PlayerUiState.Ready
                viewModel.triggerPositionSave(
                    fileId = ready.videoId,
                    name = ready.videoTitle,
                    positionMs = videoPlayheadMs,
                    durationMs = videoDurationMs,
                    thumbnailLink = null
                )
            }
            delay(1000)
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible) {
        if (controlsVisible && !screenLocked) {
            delay(3000)
            controlsVisible = false
        }
    }

    BackHandler {
        viewModel.savePositionUnconditionally()
        onBack()
    }

    Surface(
        color = Color.Black,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (screenLocked) {
                                // Show temporary unlock prompt
                                controlsVisible = true
                            } else {
                                controlsVisible = !controlsVisible
                            }
                        },
                        onDoubleTap = { offset ->
                            if (!screenLocked) {
                                val halfWidth = size.width / 2
                                if (offset.x < halfWidth) {
                                    // Rewind
                                    val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                } else {
                                    // Fast forward
                                    val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                    exoPlayer.seekTo(newPos)
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    // Margin scrolling gesture hooks
                    val screenWidth = size.width
                    val screenHeight = size.height
                    
                    var totalDx = 0f
                    var totalDy = 0f

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()

                            if (change.pressed) {
                                val position = change.position
                                gestureCoordinator.onDown(position.x, position.y, screenWidth)
                                totalDx = 0f
                                totalDy = 0f
                            }

                            if (change.previousPressed && change.pressed) {
                                val positionChange = change.position - change.previousPosition
                                totalDx += positionChange.x
                                totalDy += positionChange.y

                                val response = gestureCoordinator.onMove(
                                    dx = totalDx,
                                    dy = totalDy,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                    density = density,
                                    durationMs = videoDurationMs,
                                    currentPlayheadMs = videoPlayheadMs
                                )

                                when (response) {
                                    is GestureResponse.BrightnessChanged -> {
                                        activeGesture = "brightness"
                                        brightnessPercent = response.percent
                                    }
                                    is GestureResponse.VolumeChanged -> {
                                        activeGesture = "volume"
                                        volumePercent = response.percent
                                    }
                                    is GestureResponse.SeekScrubbing -> {
                                        activeGesture = "seek"
                                        seekTargetMs = response.targetMs
                                        // Load thumbnail preview frame
                                        val ready = uiState as? PlayerUiState.Ready
                                        if (ready != null) {
                                            viewModel.requestSeekThumbnail(ready.streamUrl, seekTargetMs)
                                        }
                                    }
                                    is GestureResponse.None -> {
                                        activeGesture = "none"
                                    }
                                }
                            }

                            if (change.previousPressed && !change.pressed) {
                                val gestureEnded = gestureCoordinator.onRelease()
                                if (gestureEnded == LockedGesture.SEEK) {
                                    exoPlayer.seekTo(seekTargetMs)
                                }
                                activeGesture = "none"
                            }
                        }
                    }
                }
        ) {
            when (val state = uiState) {
                is PlayerUiState.Loading -> {
                    CircularProgressIndicator(
                        color = AccentPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PlayerUiState.Ready -> {
                    // ExoPlayer Surface Viewport
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false // Use custom Compose layouts
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Swipe feedback overlays
                    val readyState = uiState as PlayerUiState.Ready
                    GestureOverlay(
                        brightnessPercent = brightnessPercent,
                        volumePercent = volumePercent,
                        seekTimeMs = seekTargetMs,
                        totalDurationMs = videoDurationMs,
                        previewThumbnail = seekThumbnail,
                        activeGesture = activeGesture
                    )

                    // Immersive Controls Layout
                    AnimatedVisibility(
                        visible = controlsVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            if (screenLocked) {
                                // Locked Screen overlay controls
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            screenLocked = false
                                            controlsVisible = true
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = AccentPrimary
                                        )
                                    }

                                    Text(
                                        text = stringResource(R.string.screen_locked),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            } else {
                                // Full Unlocked UI overlay
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Top Bar Scrim
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            viewModel.savePositionUnconditionally()
                                            onBack()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = state.videoTitle,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Subtitle CC toggle
                                        if (state.subtitles.isNotEmpty()) {
                                            IconButton(onClick = { /* Select subtitle track */ }) {
                                                Icon(
                                                    imageVector = Icons.Default.ClosedCaption,
                                                    contentDescription = "Subtitles",
                                                    tint = AccentPrimary
                                                )
                                            }
                                        }

                                        // Speed button
                                        IconButton(onClick = { showSpeedSheet = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = "Speed",
                                                tint = Color.White
                                            )
                                        }

                                        // Sleep Timer button
                                        IconButton(onClick = { showSleepSheet = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Snooze,
                                                contentDescription = "Sleep Timer",
                                                tint = if (sleepTimerState.active) AccentPrimary else Color.White
                                            )
                                        }

                                        // Lock Screen trigger
                                        IconButton(onClick = {
                                            screenLocked = true
                                            controlsVisible = false
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.LockOpen,
                                                contentDescription = "Lock Controls",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Bottom Bar Scrim & Slider Seeker
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(16.dp)
                                    ) {
                                        // Speed overlay label
                                        if (speed != 1.0f) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .padding(bottom = 8.dp)
                                                    .background(AccentPrimary, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.speed_badge, speed),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.Black
                                                )
                                            }
                                        }

                                        // Custom Seek Bar Slider
                                        Slider(
                                            value = videoPlayheadMs.toFloat(),
                                            onValueChange = { exoPlayer.seekTo(it.toLong()) },
                                            valueRange = 0f..videoDurationMs.toFloat(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color.White,
                                                activeTrackColor = AccentPrimary,
                                                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatMillis(videoPlayheadMs),
                                                style = PlayerTimecodeStyle,
                                                color = Color.White
                                            )

                                            // Play Pause & Queue Controls Row
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Shuffle Toggle
                                                IconButton(onClick = {
                                                    exoPlayer.shuffleModeEnabled = !shuffleModeEnabled
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shuffle,
                                                        contentDescription = "Shuffle",
                                                        tint = if (shuffleModeEnabled) AccentPrimary else Color.White.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                // Skip Previous
                                                IconButton(onClick = { viewModel.playPrevious() }) {
                                                    Icon(
                                                        imageVector = Icons.Default.SkipPrevious,
                                                        contentDescription = "Skip Previous",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }

                                                // Rewind 10s
                                                IconButton(onClick = {
                                                    val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                                                    exoPlayer.seekTo(newPos)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Replay10,
                                                        contentDescription = "Rewind 10s",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }

                                                // Play / Pause Circle
                                                IconButton(
                                                    onClick = {
                                                        if (isPlaying) {
                                                            exoPlayer.pause()
                                                        } else {
                                                            exoPlayer.play()
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(AccentPrimary, RoundedCornerShape(50.dp))
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = "Play/Pause",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }

                                                // Forward 10s
                                                IconButton(onClick = {
                                                    val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                                    exoPlayer.seekTo(newPos)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Forward10,
                                                        contentDescription = "Forward 10s",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }

                                                // Skip Next
                                                IconButton(onClick = { viewModel.playNext() }) {
                                                    Icon(
                                                        imageVector = Icons.Default.SkipNext,
                                                        contentDescription = "Skip Next",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }

                                                // Repeat Cycle Toggle
                                                IconButton(onClick = {
                                                    val nextMode = when (repeatMode) {
                                                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                                                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                                                        else -> Player.REPEAT_MODE_OFF
                                                    }
                                                    exoPlayer.repeatMode = nextMode
                                                }) {
                                                    Icon(
                                                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                                        contentDescription = "Repeat",
                                                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) AccentPrimary else Color.White.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = formatMillis(videoDurationMs),
                                                style = PlayerTimecodeStyle,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Speed configurations bottom sheet
                    if (showSpeedSheet) {
                        SpeedBottomSheet(
                            speed = speed,
                            rememberSpeed = rememberSpeed,
                            onSpeedChange = { viewModel.updatePlaybackSpeed(it) },
                            onRememberSpeedToggle = { viewModel.toggleRememberSpeed(it) },
                            onDismissRequest = { showSpeedSheet = false },
                            sheetState = sheetState
                        )
                    }

                    // Sleep Timer selection Dialog
                    if (showSleepSheet) {
                        SleepTimerDialog(
                            onMinutesSelected = { min ->
                                viewModel.setSleepTimer(min)
                                showSleepSheet = false
                            },
                            onDismiss = { showSleepSheet = false }
                        )
                    }
                }
                is PlayerUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val errMessage = when (state.type) {
                                PlayerError.FILE_NOT_FOUND -> stringResource(R.string.error_file_not_found)
                                PlayerError.ACCESS_DENIED -> stringResource(R.string.error_access_denied)
                                PlayerError.UNSUPPORTED_FORMAT -> stringResource(R.string.error_unsupported_format)
                                PlayerError.NETWORK_LOST -> stringResource(R.string.error_network_lost)
                                PlayerError.TOKEN_EXPIRED -> stringResource(R.string.error_token_expired)
                            }
                            Text(text = errMessage, color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                            ) {
                                Text(text = "Back")
                            }
                        }
                    }
                }
                else -> {}
            }

            // Watch history resume bookmark popup prompt dialog
            if (resumePromptState.show && uiState is PlayerUiState.Ready) {
                val ready = uiState as PlayerUiState.Ready
                AlertDialog(
                    onDismissRequest = { viewModel.startPlaybackDirectly(ready.streamUrl, ready.subtitles, 0L) },
                    title = { Text(text = "Resume Playback?") },
                    text = { Text(text = stringResource(R.string.resume_prompt, formatMillis(resumePromptState.positionMs))) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.startPlaybackDirectly(ready.streamUrl, ready.subtitles, resumePromptState.positionMs)
                        }) {
                            Text(text = "Resume", color = AccentPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            viewModel.startPlaybackDirectly(ready.streamUrl, ready.subtitles, 0L)
                        }) {
                            Text(text = "Start Over", color = TextSecondary)
                        }
                    },
                    containerColor = SurfaceDark
                )
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    onMinutesSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Sleep Timer") },
        text = {
            Column {
                listOf(15, 30, 45, 60).forEach { mins ->
                    TextButton(
                        onClick = { onMinutesSelected(mins) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "$mins Minutes", color = AccentPrimary)
                    }
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                }
                TextButton(
                    onClick = { onMinutesSelected(0) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Turn Off Timer", color = Color.Red)
                }
            }
        },
        confirmButton = {},
        containerColor = SurfaceDark
    )
}

private fun formatMillis(millis: Long): String {
    val sec = (millis / 1000) % 60
    val min = (millis / (1000 * 60)) % 60
    val hr = (millis / (1000 * 60 * 60))
    return if (hr > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hr, min, sec)
    } else {
        String.format(Locale.US, "%d:%02d", min, sec)
    }
}
