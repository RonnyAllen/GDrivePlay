package com.driveplay

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.driveplay.auth.GoogleSignInManager
import com.driveplay.auth.TokenManager
import com.driveplay.auth.TokenState
import com.driveplay.browser.BrowserScreen
import com.driveplay.browser.BrowserViewModel
import com.driveplay.data.db.QueueItemEntity
import com.driveplay.data.prefs.UserPreferencesDataStore
import com.driveplay.home.HomeScreen
import com.driveplay.home.HomeViewModel
import com.driveplay.player.PlayerScreen
import com.driveplay.player.PlayerViewModel
import com.driveplay.playlist.PlaylistScreen
import com.driveplay.playlist.PlaylistViewModel
import com.driveplay.settings.SettingsScreen
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.BackgroundDark
import com.driveplay.ui.theme.DrivePlayTheme
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var googleSignInManager: GoogleSignInManager

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    private var isPlayerActive = false

    private var loginSuccessCallback: (() -> Unit)? = null

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                // Clear any cached invalid token
                tokenManager.clearCachedToken()
                // Fetch the actual OAuth 2.0 Access Token in a coroutine
                lifecycleScope.launch {
                    try {
                        tokenManager.getValidTokenBlocking()
                        loginSuccessCallback?.invoke()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Failed to retrieve access token: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In failed to capture tokens", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val amoledMode by userPreferences.amoledTrueBlack.collectAsState(initial = false)
            val customBgHex by userPreferences.customBackgroundColor.collectAsState(initial = null)
            val customAccentHex by userPreferences.customAccentColor.collectAsState(initial = null)

            val customBgColor = customBgHex?.let { parseHexColor(it) }
            val customAccentColor = customAccentHex?.let { parseHexColor(it) }

            DrivePlayTheme(
                isAmoledMode = amoledMode,
                customBackgroundColor = customBgColor,
                customAccentColor = customAccentColor
            ) {
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedTab by remember { mutableIntStateOf(0) }
                val scope = rememberCoroutineScope()

                // Route contexts for inline player triggers
                var activePlayerQueue by remember { mutableStateOf<List<QueueItemEntity>>(emptyList()) }
                var activeQueueIndex by remember { mutableIntStateOf(-1) }

                // Token recovery launcher
                LaunchedEffect(Unit) {
                    tokenManager.tokenState
                        .onEach { state ->
                            if (state is TokenState.ReAuthRequired) {
                                Toast.makeText(this@MainActivity, "OAuth session expired. Re-authenticating...", Toast.LENGTH_LONG).show()
                                triggerGoogleLogin {
                                    Toast.makeText(this@MainActivity, "Re-authentication successful!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .launchIn(this)

                    // Perform silent sign in
                    val silentOk = googleSignInManager.silentSignIn()
                    if (silentOk) {
                        currentScreen = "main"
                    } else {
                        currentScreen = "login"
                    }
                }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreenLoader()
                    }
                    "login" -> {
                        LoginScreen(
                            onSignInClick = {
                                loginSuccessCallback = {
                                    currentScreen = "main"
                                }
                                triggerGoogleLogin(loginSuccessCallback!!)
                            }
                        )
                    }
                    "main" -> {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = SurfaceDark,
                                    tonalElevation = 8.dp
                                ) {
                                    listOf("Home", "Browse", "Playlists", "Settings").forEachIndexed { index, label ->
                                        NavigationBarItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            label = { Text(text = label) },
                                            icon = {
                                                Icon(
                                                    imageVector = when (index) {
                                                        0 -> Icons.Default.Home
                                                        1 -> Icons.Default.FolderOpen
                                                        2 -> Icons.Default.PlaylistPlay
                                                        else -> Icons.Default.Settings
                                                    },
                                                    contentDescription = label
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = AccentPrimary,
                                                indicatorColor = AccentPrimary,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            },
                            containerColor = BackgroundDark
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (selectedTab) {
                                    0 -> {
                                        val homeVm: HomeViewModel = hiltViewModel()
                                        HomeScreen(
                                            viewModel = homeVm,
                                            onNavigateToBrowser = { fid, fname ->
                                                selectedTab = 1
                                                // Load folder directly in browser
                                            },
                                            onPlayVideo = { fid, fname, parentFolder ->
                                                val queueItem = QueueItemEntity(
                                                    fileId = fid,
                                                    name = fname,
                                                    durationMs = 0L,
                                                    size = 0L,
                                                    thumbnailLink = null,
                                                    mimeType = "video/mp4",
                                                    parentFolderId = parentFolder,
                                                    displayOrder = 0
                                                )
                                                activePlayerQueue = listOf(queueItem)
                                                activeQueueIndex = 0
                                                currentScreen = "player"
                                            },
                                            onBrowseDriveClick = {
                                                selectedTab = 1
                                            }
                                        )
                                    }
                                    1 -> {
                                        val browserVm: BrowserViewModel = hiltViewModel()
                                        BrowserScreen(
                                            viewModel = browserVm,
                                            onPlayVideo = { videos, startIndex ->
                                                activePlayerQueue = videos.mapIndexed { idx, item ->
                                                    QueueItemEntity(
                                                        fileId = item.fileId,
                                                        name = item.name,
                                                        durationMs = item.durationMs,
                                                        size = item.size,
                                                        thumbnailLink = item.thumbnailLink,
                                                        mimeType = item.mimeType ?: "video/mp4",
                                                        parentFolderId = item.parentFolderId,
                                                        displayOrder = idx
                                                    )
                                                }
                                                activeQueueIndex = startIndex
                                                currentScreen = "player"
                                            },
                                            onBack = { selectedTab = 0 }
                                        )
                                    }
                                    2 -> {
                                        val playlistVm: PlaylistViewModel = hiltViewModel()
                                        PlaylistScreen(
                                            viewModel = playlistVm,
                                            onPlayQueue = { queue, idx ->
                                                activePlayerQueue = queue
                                                activeQueueIndex = idx
                                                currentScreen = "player"
                                            }
                                        )
                                    }
                                    3 -> {
                                        SettingsScreen(
                                            userPreferences = userPreferences,
                                            googleSignInManager = googleSignInManager,
                                            onSignOut = {
                                                currentScreen = "login"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "player" -> {
                        val playerVm: PlayerViewModel = hiltViewModel()
                        
                        DisposableEffect(Unit) {
                            isPlayerActive = true
                            onDispose {
                                isPlayerActive = false
                            }
                        }

                        // Prepare playlist items once on entry
                        LaunchedEffect(activePlayerQueue, activeQueueIndex) {
                            playerVm.loadPlaylist(activePlayerQueue, activeQueueIndex)
                        }

                        PlayerScreen(
                            viewModel = playerVm,
                            onBack = {
                                currentScreen = "main"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPlayerActive) {
            enterPictureInPictureMode()
        }
    }

    private fun triggerGoogleLogin(onSuccess: () -> Unit) {
        loginSuccessCallback = onSuccess
        val intent = googleSignInManager.getSignInIntent()
        signInLauncher.launch(intent)
    }
}

@Composable
fun SplashScreenLoader() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = AccentPrimary)
        }
    }
}

@Composable
fun LoginScreen(
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DrivePlay",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = AccentPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stream your Drive, anywhere.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onSignInClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Sign in with Google",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Parses a hex color string (e.g. "#FF8C00" or "FF8C00") to a Compose [Color].
 * Returns null if the string is malformed.
 */
fun parseHexColor(hex: String): Color? {
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

