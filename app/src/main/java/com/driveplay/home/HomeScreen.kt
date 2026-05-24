package com.driveplay.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.driveplay.data.db.WatchHistoryEntity
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.SurfaceElevated
import com.driveplay.ui.theme.TextMuted
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBrowser: (String, String) -> Unit,
    onPlayVideo: (String, String, String) -> Unit,
    onBrowseDriveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLinkBottomSheet by remember { mutableStateOf(false) }
    var inputLink by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DrivePlay",
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* Global search trigger */ }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = AccentPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLinkBottomSheet = true },
                containerColor = AccentPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Import URL")
            }
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
                is HomeUiState.Loading -> {
                    // Just basic loading
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Continue Watching Section
                        if (state.continueWatching.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Continue Watching",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(state.continueWatching) { item ->
                                        ContinueWatchingCard(
                                            item = item,
                                            onTap = {
                                                onPlayVideo(item.fileId, item.name, "root")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Recent Folders Section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Recent Folders",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        items(state.recentFolders) { folder ->
                            RecentFolderRow(
                                folder = folder,
                                onTap = { onNavigateToBrowser(folder.folderId, folder.folderName) }
                            )
                        }
                    }
                }
            }
        }

        // Link insertion sheet
        if (showLinkBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLinkBottomSheet = false },
                containerColor = SurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Open Google Drive Link",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inputLink,
                        onValueChange = { inputLink = it },
                        label = { Text("Paste folder or file link here") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = AccentPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onBrowseDriveClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevated,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(text = "Browse Drive")
                        }

                        Button(
                            onClick = {
                                if (inputLink.isNotBlank()) {
                                    showLinkBottomSheet = false
                                    // Parse link
                                    onNavigateToBrowser("link", inputLink)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Open Link", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: WatchHistoryEntity,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .clickable(onClick = onTap)
    ) {
        // Thumbnail mock Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Resume",
                tint = AccentPrimary,
                modifier = Modifier.size(36.dp)
            )

            // Progress bar
            val progress = item.positionMs.toFloat() / item.durationMs.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(AccentPrimary)
                )
            }
        }

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentFolderRow(
    folder: RecentFolderItem,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Folder",
            tint = AccentPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = folder.folderName,
            fontSize = 15.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}
