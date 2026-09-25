package com.example.util

import android.webkit.CookieManager
import android.webkit.WebResourceResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

/**
 * Central helper for embedding the official YouTube IFrame player inside the app WebViews.
 *
 * Since late 2025 YouTube enforces its "Required Minimum Functionality" policy for embedded
 * players: the request for the /embed/ player document MUST carry an HTTP `Referer` header that
 * identifies the hosting page. Regular browsers attach that header automatically, but WebViews
 * that host the iframe from wrapper HTML loaded with `loadDataWithBaseURL()` frequently send an
 * empty/missing Referer, and YouTube then refuses playback with player error 153 ("Video player
 * configuration error") or 150 — which broke ALL embedded playback in the app (regular videos
 * AND Shorts).
 *
 * Fix strategy used everywhere in the app:
 *  1. The wrapper HTML page and the player iframe share one origin ([PRIMARY_HOST], or the
 *     [FALLBACK_HOST] privacy-enhanced domain when retrying).
 *  2. [interceptEmbedRequest] is called from `WebViewClient.shouldInterceptRequest()` and
 *     re-fetches the player document with an explicit `Referer` header, guaranteeing that the
 *     required identity information always reaches YouTube.
 *  3. On player error 153 the screens automatically retry once with the fallback origin before
 *     ever showing the "video restricted" overlay.
 */
object YouTubeEmbedPlayer {

    /** Standard embed host (what YouTube itself hands out in its official embed code). */
    const val PRIMARY_HOST = "https://www.youtube.com"

    /**
     * Mobile User-Agent used by every player WebView AND by [interceptEmbedRequest].
     *
     * It is kept as a constant (instead of reading `webView.settings.userAgentString`) because
     * `WebViewClient.shouldInterceptRequest()` runs on a background thread and ANY call into a
     * WebView method there — including `getSettings()` — throws
     * `RuntimeException: A WebView method was called on thread ...` via `WebView.checkThread()`
     * and crashes the app. Never touch the WebView from that callback.
     */
    const val MOBILE_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    /** Privacy-enhanced fallback host, used for the automatic retry on error 153. */
    const val FALLBACK_HOST = "https://www.youtube-nocookie.com"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** The origin that hosts both the wrapper page and the player iframe. */
    fun host(useFallback: Boolean): String = if (useFallback) FALLBACK_HOST else PRIMARY_HOST

    /**
     * Builds the player iframe `src` URL with a consistent origin identity
     * (`origin` + `widget_referrer` always match the page that hosts the iframe).
     */
    fun buildEmbedUrl(
        videoId: String,
        useFallbackHost: Boolean = false,
        autoplay: Boolean = true,
        controls: Boolean = true,
        enableJsApi: Boolean = true,
        extraParams: String = ""
    ): String {
        val host = host(useFallbackHost)
        val url = StringBuilder(host).append("/embed/").append(videoId).append('?')
        if (autoplay) url.append("autoplay=1&")
        url.append("playsinline=1&")
            .append("controls=").append(if (controls) "1" else "0").append('&')
            .append("rel=0&modestbranding=1&fs=1&iv_load_policy=3")
        if (enableJsApi) {
            url.append("&enablejsapi=1&origin=").append(host)
                .append("&widget_referrer=").append(host)
        }
        if (extraParams.isNotBlank()) url.append('&').append(extraParams)
        return url.toString()
    }

    /** True when the URL is the /embed/ player document that must carry our Referer header. */
    fun isEmbedDocumentUrl(url: String): Boolean {
        return url.startsWith("$PRIMARY_HOST/embed/") ||
                url.startsWith("$FALLBACK_HOST/embed/") ||
                url.startsWith("https://youtube.com/embed/") ||
                url.startsWith("https://youtube-nocookie.com/embed/") ||
                url.startsWith("https://m.youtube.com/embed/")
    }

    /**
     * Fetches the YouTube embed player document with the HTTP `Referer` required by YouTube's
     * embed policy and returns it as a [WebResourceResponse]. Called from
     * `WebViewClient.shouldInterceptRequest()`; returns null (letting the WebView load the
     * request normally) for any non-embed request or on any network failure.
     */
    fun interceptEmbedRequest(
        url: String,
        userAgent: String? = null,
        useFallbackHost: Boolean = false
    ): WebResourceResponse? {
        if (!isEmbedDocumentUrl(url)) return null
        return try {
            val hostOrigin = host(useFallbackHost)
            val builder = Request.Builder()
                .url(url)
                // YouTube's embed policy (player error 153) requires the /embed/ document
                // request to carry identification of the hosting page: both a Referer and a
                // matching Origin. WebView does not reliably attach these around
                // loadDataWithBaseURL() wrapper pages, so we fetch the document ourselves.
                .header("Referer", "$hostOrigin/")
                .header("Origin", hostOrigin)
            if (!userAgent.isNullOrBlank()) {
                builder.header("User-Agent", userAgent)
            }
            // Forward the WebView's cookies so YouTube serves the same document a normal
            // browser session would get (no cookie-less consent/bot-check variants).
            try {
                val cookies = CookieManager.getInstance().getCookie(url)
                if (!cookies.isNullOrEmpty()) {
                    builder.header("Cookie", cookies)
                }
            } catch (_: Exception) {
            }
            httpClient.newCall(builder.build()).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body ?: return@use null
                val bytes = body.bytes()
                if (bytes.isEmpty()) return@use null
                val contentType = response.header("Content-Type") ?: "text/html"
                val mime = contentType.substringBefore(';').trim().ifBlank { "text/html" }
                val charset = contentType.substringAfter("charset=", "")
                    .substringBefore(';').trim()
                    .ifBlank { "utf-8" }
                val headers = mapOf(
                    "Access-Control-Allow-Origin" to "*",
                    "Referrer-Policy" to "strict-origin-when-cross-origin"
                )
                WebResourceResponse(mime, charset, 200, "OK", headers, ByteArrayInputStream(bytes))
            }
        } catch (_: Exception) {
            // Never break playback because of the interception — fall back to normal loading.
            null
        }
    }
}
