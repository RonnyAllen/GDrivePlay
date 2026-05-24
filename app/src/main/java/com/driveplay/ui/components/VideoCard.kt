package com.driveplay.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.driveplay.domain.model.PlaylistItem
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.PlayerTimecodeStyle
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextMuted
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCard(
    video: PlaylistItem,
    onTap: () -> Unit,
    onLongTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongTap
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            if (!video.thumbnailLink.isNullOrBlank()) {
                AsyncImage(
                    model = video.thumbnailLink,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Amber play fallback icon
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Fallback Play",
                    tint = AccentPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Duration Badge
            if (video.durationMs > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatMillis(video.durationMs),
                        style = PlayerTimecodeStyle.copy(fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Metadata
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = video.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Size
                Text(
                    text = formatBytes(video.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "•", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(modifier = Modifier.width(8.dp))

                // Extension/Mime badge
                val ext = getExtension(video.name)
                Box(
                    modifier = Modifier
                        .background(Color(0xFF, 0x8C, 0x00, 0x1F), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = ext,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = AccentPrimary
                    )
                }
            }
        }

        IconButton(onClick = onLongTap) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = TextSecondary
            )
        }
    }
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

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun getExtension(name: String): String {
    val dot = name.lastIndexOf('.')
    return if (dot != -1 && dot < name.length - 1) {
        name.substring(dot + 1).uppercase()
    } else {
        "VIDEO"
    }
}
