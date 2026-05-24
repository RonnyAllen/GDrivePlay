package com.driveplay.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(Scope("https://www.googleapis.com/auth/drive.readonly"))
        .build()

    private val signInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    fun isUserSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getLastAccountName(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.displayName
    }

    fun getLastAccountEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    fun getLastAccountPhotoUrl(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.photoUrl?.toString()
    }

    suspend fun silentSignIn(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                // Refresh token immediately to check validity
                tokenManager.getValidTokenBlocking()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signOut() {
        signInClient.signOut()
        tokenManager.clearCachedToken()
    }
}
