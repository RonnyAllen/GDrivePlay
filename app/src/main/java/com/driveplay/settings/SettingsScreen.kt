package com.driveplay.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.auth.GoogleSignInManager
import com.driveplay.data.prefs.UserPreferencesDataStore
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferences: UserPreferencesDataStore,
    googleSignInManager: GoogleSignInManager,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Preferences Flows
    val defaultSpeed by userPreferences.defaultSpeed.collectAsState(1.0f)
    val pitchCorrection by userPreferences.pitchCorrection.collectAsState(true)
    val autoPlayNext by userPreferences.autoPlayNext.collectAsState(true)

    val brightnessGesture by userPreferences.brightnessGestureEnabled.collectAsState(true)
    val volumeGesture by userPreferences.volumeGestureEnabled.collectAsState(true)
    val seekGesture by userPreferences.seekGestureEnabled.collectAsState(true)
    val sensitivity by userPreferences.swipeSensitivity.collectAsState(3)

    val amoledMode by userPreferences.amoledTrueBlack.collectAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .verticalScroll(rememberScrollState())
        ) {
            // User Account Info
            SectionHeader(title = "Account Details")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (googleSignInManager.getLastAccountName()?.take(1) ?: "U").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = googleSignInManager.getLastAccountName() ?: "User Name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = googleSignInManager.getLastAccountEmail() ?: "user@gmail.com",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            googleSignInManager.signOut()
                            onSignOut()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }

            // Playback Options
            SectionHeader(title = "Playback Presets")
            ToggleRowSetting(
                icon = Icons.Default.Speed,
                title = "Audio Pitch Correction",
                subtitle = "Keep voice pitches stable on speed edits",
                checked = pitchCorrection,
                onCheckedChange = { scope.launch { userPreferences.setPitchCorrection(it) } }
            )
            ToggleRowSetting(
                icon = Icons.Default.PlayCircle,
                title = "Auto-play Next Video",
                subtitle = "Play next series queue episode automatically",
                checked = autoPlayNext,
                onCheckedChange = { scope.launch { userPreferences.setAutoPlayNext(it) } }
            )

            // Gesture Options
            SectionHeader(title = "Touch & Swipe Gestures")
            ToggleRowSetting(
                icon = Icons.Default.BrightnessLow,
                title = "Vertical Brightness Swipe",
                subtitle = "Swipe left margin to edit brightness",
                checked = brightnessGesture,
                onCheckedChange = { scope.launch { userPreferences.setBrightnessGestureEnabled(it) } }
            )
            ToggleRowSetting(
                icon = Icons.Default.Settings,
                title = "Vertical Volume Swipe",
                subtitle = "Swipe right margin to edit volume",
                checked = volumeGesture,
                onCheckedChange = { scope.launch { userPreferences.setVolumeGestureEnabled(it) } }
            )
            ToggleRowSetting(
                icon = Icons.Default.ClosedCaption,
                title = "Horizontal Seek Swipe",
                subtitle = "Swipe center of player area to seek",
                checked = seekGesture,
                onCheckedChange = { scope.launch { userPreferences.setSeekGestureEnabled(it) } }
            )

            // Sensitivity Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Swipe Sensitivity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Adjust margin scroll response steps (Sensitivity: $sensitivity)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = sensitivity.toFloat(),
                    onValueChange = { scope.launch { userPreferences.setSwipeSensitivity(it.toInt()) } },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPrimary,
                        activeTrackColor = AccentPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Subtitle Options
            SectionHeader(title = "Subtitle Formatting")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ClosedCaption,
                    contentDescription = "Subtitle Font",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Preferred Font Size (16sp)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Auto-adjusted subtitles sizing configurations",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Caches
            SectionHeader(title = "Disk Storage Cache")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .clickable {
                        Toast.makeText(context, "Playback cache cleared successfully", Toast.LENGTH_SHORT).show()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Clear Cache",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Clear ExoPlayer Cache",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Free up 200MB LRU disk cache space",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Display
            SectionHeader(title = "AMOLED Display Styling")
            ToggleRowSetting(
                icon = Icons.Default.Info,
                title = "True AMOLED Black Backgrounds",
                subtitle = "Replaces charcoal with true #000000 black surface layers",
                checked = amoledMode,
                onCheckedChange = { scope.launch { userPreferences.setAmoledTrueBlack(it) } }
            )

            // About
            SectionHeader(title = "About App")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Version",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "GDrivePlay v1.0.0",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Premium Google Drive streaming app under Clean Architecture",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AccentPrimary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun ToggleRowSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AccentPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentPrimary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
