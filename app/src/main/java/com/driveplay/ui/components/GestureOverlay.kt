package com.driveplay.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.PlayerTimecodeStyle
import java.util.Locale

@Composable
fun GestureOverlay(
    brightnessPercent: Int,
    volumePercent: Int,
    seekTimeMs: Long,
    totalDurationMs: Long,
    previewThumbnail: Bitmap?,
    activeGesture: String, // "brightness", "volume", "seek", "none"
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Brightness pill overlay (Left Edge)
        AnimatedVisibility(
            visible = activeGesture == "brightness",
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessLow,
                    contentDescription = "Brightness",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vertical slider representation
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(100.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(brightnessPercent / 100f)
                            .background(AccentPrimary, RoundedCornerShape(2.dp))
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$brightnessPercent%",
                    style = PlayerTimecodeStyle,
                    color = Color.White
                )
            }
        }

        // Volume pill overlay (Right Edge)
        AnimatedVisibility(
            visible = activeGesture == "volume",
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vertical slider representation
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(100.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(volumePercent / 100f)
                            .background(AccentPrimary, RoundedCornerShape(2.dp))
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$volumePercent%",
                    style = PlayerTimecodeStyle,
                    color = Color.White
                )
            }
        }

        // Seek timeline scrubbing floating time bubble (Center)
        AnimatedVisibility(
            visible = activeGesture == "seek",
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(16.dp)
            ) {
                // Seek frame thumbnail preview
                if (previewThumbnail != null) {
                    Image(
                        bitmap = previewThumbnail.asImageBitmap(),
                        contentDescription = "Seek Frame Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 160.dp, height = 90.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMillis(seekTimeMs),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary,
                        style = PlayerTimecodeStyle
                    )
                    Text(
                        text = " / " + formatMillis(totalDurationMs),
                        fontSize = 14.sp,
                        color = Color.White,
                        style = PlayerTimecodeStyle
                    )
                }
            }
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
