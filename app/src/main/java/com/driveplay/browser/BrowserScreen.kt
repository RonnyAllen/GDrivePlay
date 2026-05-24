package com.driveplay.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onPlayVideo: (List<PlaylistItem>, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortField by viewModel.sortField.collectAsState()
    val sortDir by viewModel.sortDirection.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()

    var showDetailsSheet by remember { mutableStateOf(false) }
    var selectedVideoForDetails by remember { mutableStateOf<PlaylistItem?>(null) }
    val detailsSheetState = rememberModalBottomSheetState()

    var showContextSheet by remember { mutableStateOf(false) }
    var contextItem by remember { mutableStateOf<PlaylistItem?>(null) }

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
                                    if (isMultiSelectMode) {
                                        viewModel.toggleItemSelection(folder)
                                    } else {
                                        viewModel.loadFolder(folder.fileId, folder.name)
                                    }
                                },
                                onLongTap = {
                                    contextItem = folder
                                    showContextSheet = true
                                },
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = selectedItems.contains(folder),
                                onToggleSelection = { viewModel.toggleItemSelection(folder) }
                            )
                        }

                        // Video Listings
                        items(state.videos) { video ->
                            VideoCard(
                                video = video,
                                onTap = {
                                    if (isMultiSelectMode) {
                                        viewModel.toggleItemSelection(video)
                                    } else {
                                        val index = state.videos.indexOf(video)
                                        onPlayVideo(state.videos, index)
                                    }
                                },
                                onLongTap = {
                                    contextItem = video
                                    showContextSheet = true
                                },
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = selectedItems.contains(video),
                                onToggleSelection = { viewModel.toggleItemSelection(video) }
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

        // New custom Folder/Video Context Menu Bottom Sheet
        if (showContextSheet && contextItem != null) {
            val item = contextItem!!
            val isFolder = item.mimeType == "application/vnd.google-apps.folder"
            
            ModalBottomSheet(
                onDismissRequest = {
                    showContextSheet = false
                    contextItem = null
                },
                containerColor = SurfaceDark,
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isFolder) Icons.Default.Folder else Icons.Default.PlayArrow,
                            contentDescription = "Item Type",
                            tint = AccentPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isFolder) "Google Drive Folder" else "Google Drive Video",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 1: Add to Playlist
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showContextSheet = false
                                contextItem = null
                                if (isFolder) {
                                    viewModel.addFolderToPlaylist(item)
                                } else {
                                    viewModel.addFileToPlaylist(item)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Add Playlist",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (isFolder) "Add Folder to Playlist" else "Add Video to Playlist",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    // Option 2: Select Multiple
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showContextSheet = false
                                contextItem = null
                                viewModel.toggleMultiSelectMode()
                                viewModel.toggleItemSelection(item)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Select Multiple",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Select Multiple Items",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    // Option 3: View Details
                    if (!isFolder) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showContextSheet = false
                                    selectedVideoForDetails = item
                                    showDetailsSheet = true
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Details",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "View File Details",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Floating contextual Action Bar
        if (isMultiSelectMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(SurfaceDark.copy(alpha = 0.95f))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${selectedItems.size} Selected",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = { viewModel.addSelectedToPlaylist() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Add Selected",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add to Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { viewModel.toggleMultiSelectMode() }
                    ) {
                        Text(text = "Cancel", color = Color.Red, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun Modifier.SpacerWidth(dp: Int) = this.padding(end = dp.dp)
