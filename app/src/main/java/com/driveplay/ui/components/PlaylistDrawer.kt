package com.driveplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.data.db.QueueItemEntity
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary

@Composable
fun PlaylistDrawer(
    visible: Boolean,
    queue: List<QueueItemEntity>,
    currentIndex: Int,
    onItemClick: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(SurfaceDark.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playlist Queue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }
                }

                Divider(color = Color(0x1F, 0x1F, 0x1F))

                // Playlist Items
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(queue) { index, item ->
                        val isPlaying = index == currentIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(index) }
                                .background(if (isPlaying) Color(0xFF, 0x8C, 0x00, 0x1F) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPlaying) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Playing",
                                    tint = AccentPrimary,
                                    modifier = Modifier.width(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPlaying) AccentPrimary else TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Video File",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
