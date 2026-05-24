package com.driveplay.cast

import android.content.Context
import com.driveplay.auth.TokenManager
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastManager @Inject constructor(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private var castContext: CastContext? = null
    private var proxyServer: DriveStreamProxy? = null

    private val _isCasting = MutableStateFlow(false)
    val isCasting: StateFlow<Boolean> = _isCasting.asStateFlow()

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            _isCasting.value = true
        }
        override fun onSessionStartFailed(session: CastSession, error: Int) {
            _isCasting.value = false
            stopProxy()
        }
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionEnded(session: CastSession, error: Int) {
            _isCasting.value = false
            stopProxy()
        }
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            _isCasting.value = true
        }
        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            _isCasting.value = false
            stopProxy()
        }
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    init {
        try {
            castContext = CastContext.getSharedInstance(context)
            castContext?.sessionManager?.addSessionManagerListener(sessionListener, CastSession::class.java)
        } catch (e: Exception) {
            // Context might not be available or Play Services are missing
        }
    }

    fun startCasting(fileId: String, fileName: String) {
        val session = castContext?.sessionManager?.currentCastSession ?: return
        
        // Start local proxy
        val proxyUrl = startProxy(fileId) ?: return

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, fileName)
        }

        val mediaInfo = MediaInfo.Builder(proxyUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .setMetadata(metadata)
            .build()

        session.remoteMediaClient?.load(mediaInfo)
    }

    private fun startProxy(fileId: String): String? {
        stopProxy()
        return try {
            val proxy = DriveStreamProxy(tokenManager, OkHttpClient())
            proxyServer = proxy
            proxy.startForFile(fileId)
        } catch (e: Exception) {
            null
        }
    }

    private fun stopProxy() {
        proxyServer?.stop()
        proxyServer = null
    }
}

class DriveStreamProxy(
    private val tokenManager: TokenManager,
    private val okHttpClient: OkHttpClient
) : NanoHTTPD(0) { // port 0 = OS assigns a free port

    fun startForFile(fileId: String): String {
        if (!isAlive) start(SOCKET_READ_TIMEOUT, false)
        return "http://${getLocalIpAddress()}:$listeningPort/stream/$fileId"
    }

    override fun serve(session: IHTTPSession): Response {
        val fileId = session.uri.removePrefix("/stream/")
        
        val token = try {
            // Must retrieve OAuth Token
            tokenManager.getCachedToken() ?: "expired"
        } catch (e: Exception) {
            "expired"
        }
        
        val driveUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media&supportsAllDrives=true"

        val requestBuilder = Request.Builder()
            .url(driveUrl)
            .header("Authorization", "Bearer $token")

        // Forward Range header so the Cast receiver can seek
        session.headers["range"]?.let { requestBuilder.header("Range", it) }

        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val statusCode = if (response.code == 206)
            Response.Status.PARTIAL_CONTENT else Response.Status.OK

        return newChunkedResponse(
            statusCode,
            response.header("Content-Type") ?: "video/mp4",
            response.body!!.byteStream()
        )
    }

    private fun getLocalIpAddress(): String {
        NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.let { return it.hostAddress ?: "127.0.0.1" }
        return "127.0.0.1"
    }
}
