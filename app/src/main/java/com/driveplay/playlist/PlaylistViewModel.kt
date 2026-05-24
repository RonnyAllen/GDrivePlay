package com.driveplay.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveplay.data.db.PlaylistDao
import com.driveplay.data.db.QueueItemEntity
import com.driveplay.data.db.SavedPlaylistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoopMode { OFF, ONE, ALL }

sealed class PlaylistUiState {
    object Loading : PlaylistUiState()
    data class Success(
        val queue: List<QueueItemEntity>,
        val savedPlaylists: List<SavedPlaylistEntity>
    ) : PlaylistUiState()
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private val _loopMode = MutableStateFlow(LoopMode.OFF)
    val loopMode: StateFlow<LoopMode> = _loopMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val activeQueue = mutableListOf<QueueItemEntity>()
    private var originalQueue = listOf<QueueItemEntity>()

    init {
        loadPlaylistData()
    }

    fun loadPlaylistData() {
        viewModelScope.launch {
            _uiState.value = PlaylistUiState.Loading
            val queue = playlistDao.getActiveQueue()
            activeQueue.clear()
            activeQueue.addAll(queue)
            originalQueue = ArrayList(queue)

            val playlists = playlistDao.getAllPlaylists()
            _uiState.value = PlaylistUiState.Success(
                queue = ArrayList(activeQueue),
                savedPlaylists = playlists
            )
        }
    }

    fun addToQueue(item: QueueItemEntity) {
        viewModelScope.launch {
            val order = activeQueue.size
            val newItem = item.copy(id = 0, displayOrder = order)
            playlistDao.saveQueue(listOf(newItem))
            loadPlaylistData()
        }
    }

    fun setQueue(items: List<QueueItemEntity>) {
        viewModelScope.launch {
            playlistDao.clearQueue()
            val mapped = items.mapIndexed { index, item ->
                item.copy(id = 0, displayOrder = index)
            }
            playlistDao.saveQueue(mapped)
            loadPlaylistData()
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in activeQueue.indices || toIndex !in activeQueue.indices) return
        val item = activeQueue.removeAt(fromIndex)
        activeQueue.add(toIndex, item)

        // Resave to database
        viewModelScope.launch {
            playlistDao.clearQueue()
            val updated = activeQueue.mapIndexed { index, q ->
                q.copy(id = 0, displayOrder = index)
            }
            playlistDao.saveQueue(updated)
            
            val playlists = playlistDao.getAllPlaylists()
            _uiState.value = PlaylistUiState.Success(
                queue = ArrayList(activeQueue),
                savedPlaylists = playlists
            )
        }
    }

    fun removeFromQueue(item: QueueItemEntity) {
        activeQueue.remove(item)
        viewModelScope.launch {
            playlistDao.clearQueue()
            val updated = activeQueue.mapIndexed { index, q ->
                q.copy(id = 0, displayOrder = index)
            }
            playlistDao.saveQueue(updated)
            loadPlaylistData()
        }
    }

    fun toggleLoopMode() {
        _loopMode.value = when (_loopMode.value) {
            LoopMode.OFF -> LoopMode.ONE
            LoopMode.ONE -> LoopMode.ALL
            LoopMode.ALL -> LoopMode.OFF
        }
    }

    fun toggleShuffle() {
        val enabled = !_isShuffleEnabled.value
        _isShuffleEnabled.value = enabled
        viewModelScope.launch {
            if (enabled) {
                // Shuffle items
                val shuffled = activeQueue.shuffled()
                setQueue(shuffled)
            } else {
                // Restore original items order
                setQueue(originalQueue)
            }
        }
    }

    fun saveCurrentPlaylist(name: String) {
        viewModelScope.launch {
            if (activeQueue.isEmpty()) return@launch
            val thumbs = activeQueue.mapNotNull { it.thumbnailLink }.take(4).joinToString(",")
            val playlist = SavedPlaylistEntity(
                playlistName = name,
                itemCount = activeQueue.size,
                thumbnailCollage = thumbs
            )
            playlistDao.insertPlaylist(playlist)
            loadPlaylistData()
        }
    }

    fun deleteSavedPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(name)
            loadPlaylistData()
        }
    }
}
