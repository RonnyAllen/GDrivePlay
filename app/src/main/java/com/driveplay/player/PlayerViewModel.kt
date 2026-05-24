package com.driveplay.player

import android.graphics.Bitmap
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.driveplay.auth.TokenManager
import com.driveplay.data.NetworkMonitor
import com.driveplay.data.db.QueueItemEntity
import com.driveplay.data.prefs.UserPreferencesDataStore
import com.driveplay.data.remote.SubtitleTrack
import com.driveplay.domain.usecase.BuildStreamUrlUseCase
import com.driveplay.domain.usecase.GetResumePositionUseCase
import com.driveplay.domain.usecase.GetSubtitlesForVideoUseCase
import com.driveplay.domain.usecase.SaveWatchPositionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlayerUiState {
    object Idle : PlayerUiState()
    object Loading : PlayerUiState()
    data class Ready(
        val videoId: String,
        val videoTitle: String,
        val streamUrl: String,
        val duration: Long,
        val subtitles: List<SubtitleTrack> = emptyList()
    ) : PlayerUiState()
    data class Error(val type: PlayerError) : PlayerUiState()
}

enum class PlayerError {
    FILE_NOT_FOUND, ACCESS_DENIED, UNSUPPORTED_FORMAT, NETWORK_LOST, TOKEN_EXPIRED
}

data class ResumePromptState(val positionMs: Long, val show: Boolean)
data class SleepTimerState(val remainingMs: Long, val active: Boolean)

@OptIn(FlowPreview::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val networkMonitor: NetworkMonitor,
    private val userPreferences: UserPreferencesDataStore,
    private val buildStreamUrlUseCase: BuildStreamUrlUseCase,
    private val getSubtitlesUseCase: GetSubtitlesForVideoUseCase,
    private val saveWatchPositionUseCase: SaveWatchPositionUseCase,
    private val getResumePositionUseCase: GetResumePositionUseCase,
    val exoPlayerManager: ExoPlayerManager,
    val seekThumbnailCache: SeekThumbnailCache
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _resumePromptState = MutableStateFlow(ResumePromptState(0L, false))
    val resumePromptState: StateFlow<ResumePromptState> = _resumePromptState.asStateFlow()

    private val _sleepTimerState = MutableStateFlow(SleepTimerState(0L, false))
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private val _currentThumbnail = MutableStateFlow<Bitmap?>(null)
    val currentThumbnail: StateFlow<Bitmap?> = _currentThumbnail.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow(0) // 0: Fit, 1: Fill, 2: Stretch, 3: 4:3, 4: 16:9
    val aspectRatioMode: StateFlow<Int> = _aspectRatioMode.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _rememberSpeed = MutableStateFlow(false)
    val rememberSpeed: StateFlow<Boolean> = _rememberSpeed.asStateFlow()

    // Persistent Queue representation
    private var playlistItems: List<QueueItemEntity> = emptyList()
    private var currentPlaylistIndex = -1

    // Throttled watch position saving flow (once every 5 seconds)
    private val positionSaveRequest = MutableSharedFlow<PositionSavePayload>(extraBufferCapacity = 1)
    private var lastSavedPayload: PositionSavePayload? = null

    private var sleepCountDownTimer: CountDownTimer? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                playNext()
            }
        }
    }

    init {
        exoPlayerManager.getOrCreatePlayer().addListener(playerListener)

        // Collect network state changes
        networkMonitor.isConnected
            .onEach { isConnected ->
                if (!isConnected && _uiState.value is PlayerUiState.Ready) {
                    _uiState.value = PlayerUiState.Error(PlayerError.NETWORK_LOST)
                }
            }
            .launchIn(viewModelScope)

        // Setup position saving throttling (5 seconds debounce)
        positionSaveRequest
            .debounce(5000)
            .onEach { payload ->
                savePositionToDb(payload)
            }
            .launchIn(viewModelScope)

        // Load speed preferences
        viewModelScope.launch {
            _rememberSpeed.value = userPreferences.rememberSpeed.first()
            if (_rememberSpeed.value) {
                _speed.value = userPreferences.defaultSpeed.first()
            }
        }
    }

    fun loadPlaylist(items: List<QueueItemEntity>, startIndex: Int) {
        playlistItems = items
        currentPlaylistIndex = startIndex
        if (items.isNotEmpty() && startIndex in items.indices) {
            val target = items[startIndex]
            loadVideo(target.fileId, target.name, target.thumbnailLink, target.parentFolderId)
        }
    }

    private fun loadVideo(
        fileId: String,
        name: String,
        thumbnailLink: String?,
        parentFolderId: String
    ) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            try {
                val token = tokenManager.getValidTokenBlocking()
                val streamUrlObj = buildStreamUrlUseCase(fileId, token)

                // Fetch subtitling tracks
                val subs = getSubtitlesUseCase(name, parentFolderId, token)

                _uiState.value = PlayerUiState.Ready(
                    videoId = fileId,
                    videoTitle = name,
                    streamUrl = streamUrlObj.url,
                    duration = 0L, // Will be updated by player listener
                    subtitles = subs
                )

                // Check for resume history bookmarks
                val resumeData = getResumePositionUseCase(fileId)
                if (resumeData != null && resumeData.positionMs > 0L) {
                    _resumePromptState.value = ResumePromptState(resumeData.positionMs, true)
                } else {
                    startPlaybackDirectly(streamUrlObj.url, subs, 0L)
                }
            } catch (e: Exception) {
                _uiState.value = PlayerUiState.Error(PlayerError.FILE_NOT_FOUND)
            }
        }
    }

    fun startPlaybackDirectly(url: String, subs: List<SubtitleTrack>, startMs: Long) {
        viewModelScope.launch {
            val speedVal = _speed.value
            val pitchCorr = userPreferences.pitchCorrection.first()
            exoPlayerManager.prepareMedia(
                videoUrl = url,
                subtitles = subs,
                startPositionMs = startMs,
                playbackSpeed = speedVal,
                enablePitchCorrection = pitchCorr
            )
            _resumePromptState.value = ResumePromptState(0L, false)
        }
    }

    fun updatePlaybackSpeed(newSpeed: Float) {
        _speed.value = newSpeed
        viewModelScope.launch {
            val pitchCorr = userPreferences.pitchCorrection.first()
            exoPlayerManager.setPlaybackSpeed(newSpeed, pitchCorr)
            if (_rememberSpeed.value) {
                userPreferences.setDefaultSpeed(newSpeed)
            }
        }
    }

    fun toggleRememberSpeed(remember: Boolean) {
        _rememberSpeed.value = remember
        viewModelScope.launch {
            userPreferences.setRememberSpeed(remember)
            if (remember) {
                userPreferences.setDefaultSpeed(_speed.value)
            }
        }
    }

    fun cycleAspectRatio() {
        _aspectRatioMode.value = (_aspectRatioMode.value + 1) % 5
    }

    fun triggerPositionSave(
        fileId: String,
        name: String,
        positionMs: Long,
        durationMs: Long,
        thumbnailLink: String?
    ) {
        val payload = PositionSavePayload(fileId, name, positionMs, durationMs, thumbnailLink)
        lastSavedPayload = payload
        positionSaveRequest.tryEmit(payload)
    }

    // Force save playhead position unconditionally (on pause/stop)
    fun savePositionUnconditionally() {
        lastSavedPayload?.let {
            viewModelScope.launch {
                savePositionToDb(it)
            }
        }
    }

    private suspend fun savePositionToDb(payload: PositionSavePayload) {
        saveWatchPositionUseCase(
            fileId = payload.fileId,
            name = payload.name,
            positionMs = payload.positionMs,
            durationMs = payload.durationMs,
            thumbnailLink = payload.thumbnailLink
        )
    }

    fun requestSeekThumbnail(url: String, positionMs: Long) {
        viewModelScope.launch {
            val token = tokenManager.getValidTokenBlocking()
            seekThumbnailCache.getThumbnail(url, token, positionMs) { bitmap ->
                _currentThumbnail.value = bitmap
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepCountDownTimer?.cancel()
        if (minutes <= 0) {
            _sleepTimerState.value = SleepTimerState(0L, false)
            return
        }

        val duration = minutes * 60_000L
        sleepCountDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerState.value = SleepTimerState(millisUntilFinished, true)
            }

            override fun onFinish() {
                _sleepTimerState.value = SleepTimerState(0L, false)
                exoPlayerManager.getOrCreatePlayer().pause()
            }
        }.start()
    }

    fun playNext() {
        if (playlistItems.isEmpty()) return

        val playerInstance = exoPlayerManager.getOrCreatePlayer()
        val nextIndex = if (playerInstance.repeatMode == Player.REPEAT_MODE_ONE) {
            currentPlaylistIndex
        } else if (playerInstance.shuffleModeEnabled) {
            if (playlistItems.size > 1) {
                var rand = currentPlaylistIndex
                while (rand == currentPlaylistIndex) {
                    rand = playlistItems.indices.random()
                }
                rand
            } else {
                0
            }
        } else {
            if (currentPlaylistIndex < playlistItems.size - 1) {
                currentPlaylistIndex + 1
            } else if (playerInstance.repeatMode == Player.REPEAT_MODE_ALL) {
                0
            } else {
                -1
            }
        }

        if (nextIndex in playlistItems.indices) {
            loadPlaylist(playlistItems, nextIndex)
        }
    }

    fun playPrevious() {
        if (playlistItems.isEmpty()) return

        val playerInstance = exoPlayerManager.getOrCreatePlayer()
        val prevIndex = if (playerInstance.repeatMode == Player.REPEAT_MODE_ONE) {
            currentPlaylistIndex
        } else if (playerInstance.shuffleModeEnabled) {
            if (playlistItems.size > 1) {
                var rand = currentPlaylistIndex
                while (rand == currentPlaylistIndex) {
                    rand = playlistItems.indices.random()
                }
                rand
            } else {
                0
            }
        } else {
            if (currentPlaylistIndex > 0) {
                currentPlaylistIndex - 1
            } else if (playerInstance.repeatMode == Player.REPEAT_MODE_ALL) {
                playlistItems.size - 1
            } else {
                -1
            }
        }

        if (prevIndex in playlistItems.indices) {
            loadPlaylist(playlistItems, prevIndex)
        }
    }

    override fun onCleared() {
        try {
            exoPlayerManager.getOrCreatePlayer().removeListener(playerListener)
        } catch (e: Exception) {
            // Safe removal
        }
        sleepCountDownTimer?.cancel()
        savePositionUnconditionally()
        seekThumbnailCache.clear()
        super.onCleared()
    }
}

data class PositionSavePayload(
    val fileId: String,
    val name: String,
    val positionMs: Long,
    val durationMs: Long,
    val thumbnailLink: String?
)
