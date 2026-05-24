package com.driveplay.playlist

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.data.db.QueueItemEntity
import com.driveplay.data.db.SavedPlaylistEntity
import com.driveplay.ui.components.EmptyState
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.SurfaceElevated
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    onPlayQueue: (List<QueueItemEntity>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val loopMode by viewModel.loopMode.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Playlists & Queue",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                is PlaylistUiState.Loading -> {
                    // Loading placeholder
                }
                is PlaylistUiState.Success -> {
                    // Playback toggles chip bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Loop Mode Chip
                        InputChip(
                            selected = loopMode != LoopMode.OFF,
                            onClick = { viewModel.toggleLoopMode() },
                            label = {
                                Text(
                                    text = when (loopMode) {
                                        LoopMode.OFF -> "Loop Off"
                                        LoopMode.ONE -> "Loop One"
                                        LoopMode.ALL -> "Loop All"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = "Loop"
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = AccentPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )

                        // Shuffle Chip
                        InputChip(
                            selected = isShuffle,
                            onClick = { viewModel.toggleShuffle() },
                            label = { Text("Shuffle") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle"
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = AccentPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }

                    if (state.queue.isEmpty() && state.savedPlaylists.isEmpty()) {
                        EmptyState(
                            message = "Your active queue is empty",
                            hint = "Go to the file browser and tap a video or folder to populate."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Queue Header Section
                            if (state.queue.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Active Queue (${state.queue.size} items)",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                itemsIndexed(state.queue) { index, item ->
                                    QueueRowItem(
                                        item = item,
                                        index = index,
                                        queueSize = state.queue.size,
                                        onPlay = { onPlayQueue(state.queue, index) },
                                        onDelete = { viewModel.removeFromQueue(item) },
                                        onMoveUp = { viewModel.reorderQueue(index, index - 1) },
                                        onMoveDown = { viewModel.reorderQueue(index, index + 1) }
                                    )
                                }

                                // Play All CTA Button
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Button(
                                            onClick = { onPlayQueue(state.queue, 0) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AccentPrimary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play All"
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Play All Queue", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Saved Playlists Catalog
                            if (state.savedPlaylists.isNotEmpty()) {
                                item {
                                    Divider(color = Color(0x1F, 0x1F, 0x1F), modifier = Modifier.padding(vertical = 12.dp))
                                    Text(
                                        text = "Saved Playlists",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }

                                itemsIndexed(state.savedPlaylists) { _, playlist ->
                                    SavedPlaylistRowItem(
                                        playlist = playlist,
                                        onDelete = { viewModel.deleteSavedPlaylist(playlist.playlistName) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueRowItem(
    item: QueueItemEntity,
    index: Int,
    queueSize: Int,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = "Drag",
            tint = AccentPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onPlay)
        ) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Episode ${index + 1}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Reordering arrows to support fully reliable manual ordering
        if (index > 0) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (index < queueSize - 1) {
            IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Down",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SavedPlaylistRowItem(
    playlist: SavedPlaylistEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail collage placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Loop,
                contentDescription = "Playlist",
                tint = AccentPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.playlistName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${playlist.itemCount} items",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red.copy(alpha = 0.7f)
            )
        }
    }
}
