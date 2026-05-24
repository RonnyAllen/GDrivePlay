package com.driveplay.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.domain.model.PlaylistItem
import com.driveplay.ui.components.Breadcrumb
import com.driveplay.ui.components.EmptyState
import com.driveplay.ui.components.FolderCard
import com.driveplay.ui.components.SkeletonLoader
import com.driveplay.ui.components.VideoCard
import com.driveplay.ui.components.VideoInfoBottomSheet
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onPlayVideo: (String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortField by viewModel.sortField.collectAsState()
    val sortDir by viewModel.sortDirection.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()

    var showDetailsSheet by remember { mutableStateOf(false) }
    var selectedVideoForDetails by remember { mutableStateOf<PlaylistItem?>(null) }
    val detailsSheetState = rememberModalBottomSheetState()

    val listState = rememberLazyListState()

    // Pagination trigger when scrolling close to bottom
    val isScrollCloseToBottom = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 5
        }
    }

    LaunchedEffect(isScrollCloseToBottom.value) {
        if (isScrollCloseToBottom.value) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "File Browser",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is BrowserUiState.Loading -> {
                    SkeletonLoader()
                }
                is BrowserUiState.Success -> {
                    // Breadcrumbs Path Bar
                    Breadcrumb(
                        breadcrumbs = state.breadcrumbs,
                        onSegmentClick = { fid, fname, index ->
                            viewModel.loadFolder(fid, fname, clearBreadcrumbsFrom = index)
                        }
                    )

                    Divider(color = Color(0x1F, 0x1F, 0x1F))

                    // Sort Chips Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = sortField == SortField.NAME,
                            onClick = { viewModel.changeSort(SortField.NAME) },
                            label = { Text("Name") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = sortField == SortField.SIZE,
                            onClick = { viewModel.changeSort(SortField.SIZE) },
                            label = { Text("Size") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }

                    // Hierarchy lazy columns
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Folder Listings
                        items(state.folders) { folder ->
                            FolderCard(
                                folder = folder,
                                onTap = {
                                    viewModel.loadFolder(folder.fileId, folder.name)
                                }
                            )
                        }

                        // Video Listings
                        items(state.videos) { video ->
                            VideoCard(
                                video = video,
                                onTap = {
                                    onPlayVideo(video.fileId, video.name, currentFolderId)
                                },
                                onLongTap = {
                                    selectedVideoForDetails = video
                                    showDetailsSheet = true
                                }
                            )
                        }
                    }
                }
                is BrowserUiState.Empty -> {
                    EmptyState(
                        message = "No videos found in this folder",
                        hint = "This folder appears empty or only contains unsupported file formats.",
                        onActionClick = { viewModel.loadFolder(currentFolderId, "Current Folder") },
                        actionText = "Refresh"
                    )
                }
                is BrowserUiState.Error -> {
                    EmptyState(
                        message = "Failed to load directory items",
                        hint = state.message,
                        onActionClick = { viewModel.loadFolder(currentFolderId, "Current Folder") },
                        actionText = "Retry"
                    )
                }
            }
        }

        // Details metadata drawer bottom sheet
        if (showDetailsSheet && selectedVideoForDetails != null) {
            VideoInfoBottomSheet(
                video = selectedVideoForDetails!!,
                onDismissRequest = {
                    showDetailsSheet = false
                    selectedVideoForDetails = null
                },
                sheetState = detailsSheetState,
                onDownloadClick = {
                    // Trigger download placeholder logic
                }
            )
        }
    }
}

private fun Modifier.SpacerWidth(dp: Int) = this.padding(end = dp.dp)
