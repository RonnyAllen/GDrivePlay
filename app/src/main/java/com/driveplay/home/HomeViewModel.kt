package com.driveplay.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveplay.data.db.WatchHistoryDao
import com.driveplay.data.db.WatchHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val continueWatching: List<WatchHistoryEntity>,
        val recentFolders: List<RecentFolderItem>
    ) : HomeUiState()
}

data class RecentFolderItem(
    val folderId: String,
    val folderName: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        watchHistoryDao.getWatchHistoryFlow()
            .onEach { historyList ->
                // Stored positions filter ratios (5% < ratio < 95% is enforced on database insertions)
                val continueWatching = historyList.take(5)

                // Mock dynamic folders list
                val folders = listOf(
                    RecentFolderItem("root", "My Drive"),
                    RecentFolderItem("shared", "Shared with Me")
                )

                _uiState.value = HomeUiState.Success(
                    continueWatching = continueWatching,
                    recentFolders = folders
                )
            }
            .launchIn(viewModelScope)
    }
}
