package com.driveplay.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // Playback Keys
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val REMEMBER_SPEED = booleanPreferencesKey("remember_speed")
        val PITCH_CORRECTION = booleanPreferencesKey("pitch_correction")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")

        // Gesture Keys
        val BRIGHTNESS_GESTURE_ENABLED = booleanPreferencesKey("brightness_gesture_enabled")
        val VOLUME_GESTURE_ENABLED = booleanPreferencesKey("volume_gesture_enabled")
        val SEEK_GESTURE_ENABLED = booleanPreferencesKey("seek_gesture_enabled")
        val SWIPE_SENSITIVITY = intPreferencesKey("swipe_sensitivity")

        // Subtitle Keys
        val SUBTITLE_FONT_SIZE = intPreferencesKey("subtitle_font_size")
        val SUBTITLE_OPACITY = floatPreferencesKey("subtitle_opacity")
        val SUBTITLE_LANGUAGE = stringPreferencesKey("subtitle_language")
        val SUBTITLE_ENCODING = stringPreferencesKey("subtitle_encoding")

        // Background/Network
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
        val PIP_AUTO_ENTER = booleanPreferencesKey("pip_auto_enter")

        // Display
        val AMOLED_TRUE_BLACK = booleanPreferencesKey("amoled_true_black")

        // Color Customization
        val CUSTOM_BACKGROUND_COLOR = stringPreferencesKey("custom_background_color")
        val CUSTOM_ACCENT_COLOR = stringPreferencesKey("custom_accent_color")
    }

    // Playback Settings Flow
    val defaultSpeed: Flow<Float> = dataStore.data.map { it[DEFAULT_SPEED] ?: 1.0f }
    val rememberSpeed: Flow<Boolean> = dataStore.data.map { it[REMEMBER_SPEED] ?: false }
    val pitchCorrection: Flow<Boolean> = dataStore.data.map { it[PITCH_CORRECTION] ?: true }
    val autoPlayNext: Flow<Boolean> = dataStore.data.map { it[AUTO_PLAY_NEXT] ?: true }

    // Gesture Settings Flow
    val brightnessGestureEnabled: Flow<Boolean> = dataStore.data.map { it[BRIGHTNESS_GESTURE_ENABLED] ?: true }
    val volumeGestureEnabled: Flow<Boolean> = dataStore.data.map { it[VOLUME_GESTURE_ENABLED] ?: true }
    val seekGestureEnabled: Flow<Boolean> = dataStore.data.map { it[SEEK_GESTURE_ENABLED] ?: true }
    val swipeSensitivity: Flow<Int> = dataStore.data.map { it[SWIPE_SENSITIVITY] ?: 3 }

    // Subtitle Settings Flow
    val subtitleFontSize: Flow<Int> = dataStore.data.map { it[SUBTITLE_FONT_SIZE] ?: 16 }
    val subtitleOpacity: Flow<Float> = dataStore.data.map { it[SUBTITLE_OPACITY] ?: 0.8f }
    val subtitleLanguage: Flow<String> = dataStore.data.map { it[SUBTITLE_LANGUAGE] ?: "English" }
    val subtitleEncoding: Flow<String> = dataStore.data.map { it[SUBTITLE_ENCODING] ?: "UTF-8" }

    // Background/Network Flow
    val backgroundPlayback: Flow<Boolean> = dataStore.data.map { it[BACKGROUND_PLAYBACK] ?: false }
    val pipAutoEnter: Flow<Boolean> = dataStore.data.map { it[PIP_AUTO_ENTER] ?: true }

    // AMOLED Flow
    val amoledTrueBlack: Flow<Boolean> = dataStore.data.map { it[AMOLED_TRUE_BLACK] ?: false }

    // Color Customization Flow (nullable hex strings e.g. "#FF8C00")
    val customBackgroundColor: Flow<String?> = dataStore.data.map { it[CUSTOM_BACKGROUND_COLOR] }
    val customAccentColor: Flow<String?> = dataStore.data.map { it[CUSTOM_ACCENT_COLOR] }

    // Updaters
    suspend fun setDefaultSpeed(speed: Float) = dataStore.edit { it[DEFAULT_SPEED] = speed }
    suspend fun setRememberSpeed(remember: Boolean) = dataStore.edit { it[REMEMBER_SPEED] = remember }
    suspend fun setPitchCorrection(enabled: Boolean) = dataStore.edit { it[PITCH_CORRECTION] = enabled }
    suspend fun setAutoPlayNext(enabled: Boolean) = dataStore.edit { it[AUTO_PLAY_NEXT] = enabled }

    suspend fun setBrightnessGestureEnabled(enabled: Boolean) = dataStore.edit { it[BRIGHTNESS_GESTURE_ENABLED] = enabled }
    suspend fun setVolumeGestureEnabled(enabled: Boolean) = dataStore.edit { it[VOLUME_GESTURE_ENABLED] = enabled }
    suspend fun setSeekGestureEnabled(enabled: Boolean) = dataStore.edit { it[SEEK_GESTURE_ENABLED] = enabled }
    suspend fun setSwipeSensitivity(sensitivity: Int) = dataStore.edit { it[SWIPE_SENSITIVITY] = sensitivity }

    suspend fun setSubtitleFontSize(size: Int) = dataStore.edit { it[SUBTITLE_FONT_SIZE] = size }
    suspend fun setSubtitleOpacity(opacity: Float) = dataStore.edit { it[SUBTITLE_OPACITY] = opacity }
    suspend fun setSubtitleLanguage(language: String) = dataStore.edit { it[SUBTITLE_LANGUAGE] = language }
    suspend fun setSubtitleEncoding(encoding: String) = dataStore.edit { it[SUBTITLE_ENCODING] = encoding }

    suspend fun setBackgroundPlayback(enabled: Boolean) = dataStore.edit { it[BACKGROUND_PLAYBACK] = enabled }
    suspend fun setPipAutoEnter(enabled: Boolean) = dataStore.edit { it[PIP_AUTO_ENTER] = enabled }

    suspend fun setAmoledTrueBlack(enabled: Boolean) = dataStore.edit { it[AMOLED_TRUE_BLACK] = enabled }

    suspend fun setCustomBackgroundColor(hex: String?) = dataStore.edit {
        if (hex != null) it[CUSTOM_BACKGROUND_COLOR] = hex
        else it.remove(CUSTOM_BACKGROUND_COLOR)
    }
    suspend fun setCustomAccentColor(hex: String?) = dataStore.edit {
        if (hex != null) it[CUSTOM_ACCENT_COLOR] = hex
        else it.remove(CUSTOM_ACCENT_COLOR)
    }
}
