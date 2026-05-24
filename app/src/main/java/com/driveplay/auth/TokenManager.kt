package com.driveplay.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class TokenState {
    object Valid : TokenState()
    data class ReAuthRequired(val signInAccount: GoogleSignInAccount?) : TokenState()
}

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private val _tokenState = MutableSharedFlow<TokenState>(extraBufferCapacity = 1)
    val tokenState: SharedFlow<TokenState> = _tokenState.asSharedFlow()

    fun getCachedToken(): String? {
        val token = prefs.getString("access_token", null)
        val expiry = prefs.getLong("token_expiry", 0L)
        if (token != null && System.currentTimeMillis() < expiry - 60_000) {
            return token
        }
        return null
    }

    suspend fun getValidTokenBlocking(): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val cachedToken = prefs.getString("access_token", null)
            val expiry = prefs.getLong("token_expiry", 0L)

            // Return if cache is valid and not within 60s of expiry
            if (cachedToken != null && System.currentTimeMillis() < expiry - 60_000) {
                return@withContext cachedToken
            }

            // Perform token refresh via GoogleAccountCredential or fallback silent sign in
            try {
                val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (lastAccount == null) {
                    _tokenState.tryEmit(TokenState.ReAuthRequired(null))
                    throw Exception("No active Google account found for refresh")
                }

                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf("https://www.googleapis.com/auth/drive.readonly")
                ).apply {
                    selectedAccountName = lastAccount.email
                }

                val freshToken = credential.getToken() ?: throw Exception("Failed to fetch fresh token from credentials")
                val newExpiry = System.currentTimeMillis() + 3600_000 // 1 hour typical expiry

                prefs.edit()
                    .putString("access_token", freshToken)
                    .putLong("token_expiry", newExpiry)
                    .apply()

                _tokenState.tryEmit(TokenState.Valid)
                return@withContext freshToken
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                // Recoverable auth exception (need to prompt re-auth in UI)
                val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
                _tokenState.tryEmit(TokenState.ReAuthRequired(lastAccount))
                throw e
            } catch (e: Exception) {
                // General error, fallback to returning cached even if expired rather than crashing immediately,
                // or propagate error.
                prefs.getString("access_token", null) ?: throw e
            }
        }
    }

    fun saveToken(token: String, expiryMs: Long) {
        prefs.edit()
            .putString("access_token", token)
            .putLong("token_expiry", expiryMs)
            .apply()
        _tokenState.tryEmit(TokenState.Valid)
    }

    fun clearCachedToken() {
        prefs.edit()
            .remove("access_token")
            .remove("token_expiry")
            .apply()
    }
}
