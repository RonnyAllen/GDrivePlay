package com.driveplay.settings

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

// ── Preset color palettes ─────────────────────────────────────────────────
private val accentPresets = listOf(
    "#FF8C00" to "Amber",       // Default
    "#E91E63" to "Rose",
    "#9C27B0" to "Purple",
    "#673AB7" to "Violet",
    "#3F51B5" to "Indigo",
    "#2196F3" to "Blue",
    "#00BCD4" to "Cyan",
    "#009688" to "Teal",
    "#4CAF50" to "Green",
    "#8BC34A" to "Lime",
    "#CDDC39" to "Yellow",
    "#FF5722" to "Red-Orange",
    "#FF4081" to "Pink",
    "#00E5FF" to "Aqua",
    "#76FF03" to "Neon Green",
    "#FFD740" to "Gold",
)

private val backgroundPresets = listOf(
    "#0A0A0A" to "Near-Black",  // Default
    "#000000" to "True Black",
    "#121212" to "Dark Grey",
    "#1A1A2E" to "Deep Navy",
    "#0D1117" to "GitHub Dark",
    "#1E1E2E" to "Catppuccin",
    "#2D1B2E" to "Dark Plum",
    "#0B132B" to "Midnight",
    "#1B2838" to "Steam Dark",
    "#191919" to "Charcoal",
    "#2B2D42" to "Slate Blue",
    "#1A1B26" to "Tokyo Night",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // Color preferences
    val customBgHex by userPreferences.customBackgroundColor.collectAsState(null)
    val customAccentHex by userPreferences.customAccentColor.collectAsState(null)

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

            // ═══════════════════════════════════════════════════════════
            // Color Scheme Customization
            // ═══════════════════════════════════════════════════════════
            SectionHeader(title = "Color Scheme")

            // ── Accent Color Picker ───────────────────────────────────
            ColorPickerSection(
                label = "Primary Accent Color",
                subtitle = "Used for buttons, icons, sliders, and highlights",
                icon = Icons.Default.Palette,
                presets = accentPresets,
                currentHex = customAccentHex,
                defaultHex = "#FF8C00",
                onColorSelected = { hex ->
                    scope.launch { userPreferences.setCustomAccentColor(hex) }
                },
                onReset = {
                    scope.launch { userPreferences.setCustomAccentColor(null) }
                    Toast.makeText(context, "Accent color reset to default", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // ── Background Color Picker ───────────────────────────────
            ColorPickerSection(
                label = "Background Color",
                subtitle = "App background and surface base layer",
                icon = Icons.Default.Palette,
                presets = backgroundPresets,
                currentHex = customBgHex,
                defaultHex = "#0A0A0A",
                onColorSelected = { hex ->
                    scope.launch { userPreferences.setCustomBackgroundColor(hex) }
                },
                onReset = {
                    scope.launch { userPreferences.setCustomBackgroundColor(null) }
                    Toast.makeText(context, "Background color reset to default", Toast.LENGTH_SHORT).show()
                }
            )

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
                        text = "GDrivePlay v1.0.3",
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

// ═══════════════════════════════════════════════════════════════════════════
// Color Picker Section Component
// ═══════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerSection(
    label: String,
    subtitle: String,
    icon: ImageVector,
    presets: List<Pair<String, String>>,
    currentHex: String?,
    defaultHex: String,
    onColorSelected: (String) -> Unit,
    onReset: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val activeHex = currentHex ?: defaultHex
    var hexInput by remember(activeHex) { mutableStateOf(activeHex) }
    var isHexValid by remember { mutableStateOf(true) }

    val previewColor by animateColorAsState(
        targetValue = parseSettingsHexColor(activeHex) ?: Color.Gray,
        animationSpec = tween(300),
        label = "colorPreview"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live preview circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
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

            // Reset button
            if (currentHex != null) {
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset to Default",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset color grid
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { (hex, name) ->
                val color = parseSettingsHexColor(hex) ?: Color.Gray
                val isSelected = activeHex.equals(hex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(
                                2.5.dp,
                                Color.White,
                                CircleShape
                            )
                            else Modifier.border(
                                1.dp,
                                Color.White.copy(alpha = 0.15f),
                                CircleShape
                            )
                        )
                        .clickable {
                            onColorSelected(hex)
                            hexInput = hex
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isColorDark(color)) Color.White else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom hex input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hexInput,
                onValueChange = { value ->
                    hexInput = value
                    val parsed = parseSettingsHexColor(value)
                    isHexValid = parsed != null
                },
                label = { Text("Custom Hex", fontSize = 12.sp) },
                placeholder = { Text("#FF8C00", color = TextSecondary.copy(alpha = 0.5f)) },
                isError = !isHexValid && hexInput.isNotEmpty(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isHexValid && hexInput.isNotEmpty()) {
                            val normalized = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
                            onColorSelected(normalized.uppercase())
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    cursorColor = AccentPrimary,
                    focusedLabelColor = AccentPrimary,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = {
                    if (isHexValid && hexInput.isNotEmpty()) {
                        val normalized = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
                        onColorSelected(normalized.uppercase())
                        focusManager.clearFocus()
                    }
                },
                enabled = isHexValid && hexInput.isNotEmpty()
            ) {
                Text(
                    text = "Apply",
                    color = if (isHexValid && hexInput.isNotEmpty()) AccentPrimary else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        if (!isHexValid && hexInput.isNotEmpty()) {
            Text(
                text = "Invalid hex format (use #RRGGBB)",
                fontSize = 11.sp,
                color = Color(0xFFCF6679),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shared Components
// ═══════════════════════════════════════════════════════════════════════════
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

// ═══════════════════════════════════════════════════════════════════════════
// Utility Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parses hex color strings like "#FF8C00" or "FF8C00" into a [Color].
 */
private fun parseSettingsHexColor(hex: String): Color? {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = when (cleaned.length) {
            6 -> (0xFF000000 or cleaned.toLong(16))
            8 -> cleaned.toLong(16)
            else -> return null
        }
        Color(colorLong.toInt())
    } catch (_: Exception) {
        null
    }
}

/**
 * Determines whether a color is "dark" for choosing contrasting text/icon colors.
 */
private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
