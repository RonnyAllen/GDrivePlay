package com.driveplay.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveplay.domain.model.PlaylistItem
import com.driveplay.domain.usecase.GetFolderContentsUseCase
import com.driveplay.domain.usecase.DriveIdType
import com.driveplay.domain.usecase.DriveLinkResult
import com.driveplay.domain.usecase.LoadFolderByLinkUseCase
import com.driveplay.data.db.PlaylistDao
import com.driveplay.data.db.QueueItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BrowserUiState {
    object Loading : BrowserUiState()
    data class Success(
        val folders: List<PlaylistItem>,
        val videos: List<PlaylistItem>,
        val breadcrumbs: List<BreadcrumbItem>
    ) : BrowserUiState()
    data class Empty(val folderName: String) : BrowserUiState()
    data class Error(val message: String, val canRetry: Boolean) : BrowserUiState()
}

data class BreadcrumbItem(
    val folderId: String,
    val folderName: String
)

enum class SortField { NAME, DATE_MODIFIED, SIZE }
enum class SortDirection { ASC, DESC }

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val getFolderContentsUseCase: GetFolderContentsUseCase,
    private val loadFolderByLinkUseCase: LoadFolderByLinkUseCase,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowserUiState>(BrowserUiState.Loading)
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _currentFolderId = MutableStateFlow("root")
    val currentFolderId: StateFlow<String> = _currentFolderId.asStateFlow()

    private val _sortField = MutableStateFlow(SortField.NAME)
    val sortField: StateFlow<SortField> = _sortField.asStateFlow()

    private val _sortDirection = MutableStateFlow(SortDirection.ASC)
    val sortDirection: StateFlow<SortDirection> = _sortDirection.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<PlaylistItem>>(emptySet())
    val selectedItems: StateFlow<Set<PlaylistItem>> = _selectedItems.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    // Breadcrumbs list
    private val breadcrumbs = mutableListOf(BreadcrumbItem("root", "My Drive"))

    private var nextPageToken: String? = null
    private var isFetchingNextPage = false

    init {
        loadFolder("root", "My Drive")
    }

    fun loadFolder(folderId: String, folderName: String, clearBreadcrumbsFrom: Int = -1) {
        viewModelScope.launch {
            _uiState.value = BrowserUiState.Loading
            _currentFolderId.value = folderId
            nextPageToken = null

            // Update breadcrumbs
            if (clearBreadcrumbsFrom != -1 && clearBreadcrumbsFrom < breadcrumbs.size) {
                val toRemove = breadcrumbs.size - clearBreadcrumbsFrom - 1
                repeat(toRemove) {
                    if (breadcrumbs.size > 1) {
                        breadcrumbs.removeAt(breadcrumbs.size - 1)
                    }
                }
            } else if (folderId == "root") {
                breadcrumbs.clear()
                breadcrumbs.add(BreadcrumbItem("root", "My Drive"))
            } else if (breadcrumbs.none { it.folderId == folderId }) {
                breadcrumbs.add(BreadcrumbItem(folderId, folderName))
            }

            fetchFolderContents(folderId)
        }
    }

    private suspend fun fetchFolderContents(folderId: String) {
        when (val result = getFolderContentsUseCase(folderId)) {
            is GetFolderContentsUseCase.FolderResult.Success -> {
                nextPageToken = result.nextPageToken
                if (result.folders.isEmpty() && result.videos.isEmpty()) {
                    _uiState.value = BrowserUiState.Empty(breadcrumbs.lastOrNull()?.folderName ?: "Folder")
                } else {
                    _uiState.value = BrowserUiState.Success(
                        folders = result.folders,
                        videos = result.videos,
                        breadcrumbs = ArrayList(breadcrumbs)
                    )
                    applySort()
                }
            }
            is GetFolderContentsUseCase.FolderResult.Error -> {
                _uiState.value = BrowserUiState.Error(
                    message = result.exception.message ?: "Failed to retrieve folder contents",
                    canRetry = true
                )
            }
        }
    }

    fun loadNextPage() {
        val currentFolder = _currentFolderId.value
        val currentToken = nextPageToken
        if (isFetchingNextPage || currentToken == null) return

        viewModelScope.launch {
            isFetchingNextPage = true
            when (val result = getFolderContentsUseCase(currentFolder, currentToken)) {
                is GetFolderContentsUseCase.FolderResult.Success -> {
                    nextPageToken = result.nextPageToken
                    val state = _uiState.value
                    if (state is BrowserUiState.Success) {
                        val mergedFolders = (state.folders + result.folders).distinctBy { it.fileId }
                        val mergedVideos = (state.videos + result.videos).distinctBy { it.fileId }
                        _uiState.value = BrowserUiState.Success(
                            folders = mergedFolders,
                            videos = mergedVideos,
                            breadcrumbs = ArrayList(breadcrumbs)
                        )
                        applySort()
                    }
                }
                is GetFolderContentsUseCase.FolderResult.Error -> {
                    // Fail silently on pagination loading
                }
            }
            isFetchingNextPage = false
        }
    }

    fun changeSort(field: SortField) {
        if (_sortField.value == field) {
            // Toggle direction
            _sortDirection.value = if (_sortDirection.value == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
        } else {
            _sortField.value = field
            _sortDirection.value = SortDirection.ASC
        }
        applySort()
    }

    private fun applySort() {
        val state = _uiState.value as? BrowserUiState.Success ?: return
        val field = _sortField.value
        val direction = _sortDirection.value

        val sortedFolders = when (field) {
            SortField.NAME -> state.folders.sortedWith { f1, f2 -> compareItems(f1.name, f2.name, direction) }
            else -> state.folders // Folders usually sort by name
        }

        val sortedVideos = when (field) {
            SortField.NAME -> state.videos.sortedWith { v1, v2 -> compareItems(v1.name, v2.name, direction) }
            SortField.SIZE -> state.videos.sortedWith { v1, v2 -> compareItems(v1.size, v2.size, direction) }
            SortField.DATE_MODIFIED -> state.videos // Fallback or sort by duration as mod date is omitted in Drive simple models
        }

        _uiState.value = BrowserUiState.Success(
            folders = sortedFolders,
            videos = sortedVideos,
            breadcrumbs = state.breadcrumbs
        )
    }

    private fun <T : Comparable<T>> compareItems(val1: T, val2: T, direction: SortDirection): Int {
        return if (direction == SortDirection.ASC) {
            val1.compareTo(val2)
        } else {
            val2.compareTo(val1)
        }
    }

    fun loadFromLink(link: String, onFileResolved: (String, String, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = BrowserUiState.Loading
            val result = loadFolderByLinkUseCase(link)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                if (data.type == DriveIdType.FILE && data.targetFileId != null) {
                    // Navigate to folder and auto-play target file
                    loadFolder(data.resolvedFolderId, "Shared Folder")
                    onFileResolved(data.resolvedFolderId, data.targetFileId, "Shared File")
                } else {
                    // Simple folder load
                    loadFolder(data.resolvedFolderId, "Shared Folder")
                }
            } else {
                _uiState.value = BrowserUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Failed to parse paste link",
                    canRetry = false
                )
            }
        }
    }

    // Playlist Add & Multiselect Actions
    fun addFolderToPlaylist(folder: PlaylistItem) {
        viewModelScope.launch {
            val result = getFolderContentsUseCase(folder.fileId)
            if (result is GetFolderContentsUseCase.FolderResult.Success) {
                val videos = result.videos
                if (videos.isNotEmpty()) {
                    val activeQueue = playlistDao.getActiveQueue()
                    var nextOrder = activeQueue.size
                    val queueItems = videos.map { item ->
                        QueueItemEntity(
                            fileId = item.fileId,
                            name = item.name,
                            durationMs = item.durationMs,
                            size = item.size,
                            thumbnailLink = item.thumbnailLink,
                            mimeType = item.mimeType ?: "video/mp4",
                            parentFolderId = folder.fileId,
                            displayOrder = nextOrder++
                        )
                    }
                    playlistDao.saveQueue(queueItems)
                }
            }
        }
    }

    fun addFileToPlaylist(video: PlaylistItem) {
        viewModelScope.launch {
            val activeQueue = playlistDao.getActiveQueue()
            val order = activeQueue.size
            val newItem = QueueItemEntity(
                fileId = video.fileId,
                name = video.name,
                durationMs = video.durationMs,
                size = video.size,
                thumbnailLink = video.thumbnailLink,
                mimeType = video.mimeType ?: "video/mp4",
                parentFolderId = video.parentFolderId,
                displayOrder = order
            )
            playlistDao.saveQueue(listOf(newItem))
        }
    }

    fun toggleMultiSelectMode() {
        val enabled = !_isMultiSelectMode.value
        _isMultiSelectMode.value = enabled
        if (!enabled) {
            _selectedItems.value = emptySet()
        }
    }

    fun toggleItemSelection(item: PlaylistItem) {
        val current = _selectedItems.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedItems.value = current
    }

    fun addSelectedToPlaylist() {
        val selected = _selectedItems.value
        if (selected.isEmpty()) return

        viewModelScope.launch {
            val activeQueue = playlistDao.getActiveQueue()
            var nextOrder = activeQueue.size

            val queueItems = mutableListOf<QueueItemEntity>()
            for (item in selected) {
                if (item.mimeType == "application/vnd.google-apps.folder") {
                    // Fetch immediate videos in folder
                    val result = getFolderContentsUseCase(item.fileId)
                    if (result is GetFolderContentsUseCase.FolderResult.Success) {
                        result.videos.forEach { video ->
                            queueItems.add(
                                QueueItemEntity(
                                    fileId = video.fileId,
                                    name = video.name,
                                    durationMs = video.durationMs,
                                    size = video.size,
                                    thumbnailLink = video.thumbnailLink,
                                    mimeType = video.mimeType ?: "video/mp4",
                                    parentFolderId = item.fileId,
                                    displayOrder = nextOrder++
                                )
                            )
                        }
                    }
                } else {
                    queueItems.add(
                        QueueItemEntity(
                            fileId = item.fileId,
                            name = item.name,
                            durationMs = item.durationMs,
                            size = item.size,
                            thumbnailLink = item.thumbnailLink,
                            mimeType = item.mimeType ?: "video/mp4",
                            parentFolderId = item.parentFolderId,
                            displayOrder = nextOrder++
                        )
                    )
                }
            }

            if (queueItems.isNotEmpty()) {
                playlistDao.saveQueue(queueItems)
            }

            // Reset
            _selectedItems.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }
}
