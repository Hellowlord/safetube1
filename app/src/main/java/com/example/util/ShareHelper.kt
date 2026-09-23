package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareHelper {

    /**
     * Converts a video ID into a proper shareable web link.
     */
    fun getVideoShareUrl(videoId: String): String {
        return when {
            videoId.startsWith("http://") || videoId.startsWith("https://") -> videoId
            else -> "https://youtu.be/$videoId"
        }
    }

    /**
     * Builds and launches an Android ACTION_SEND Intent with a chooser
     * to share the video link and title to other apps.
     */
    fun shareVideo(context: Context, videoId: String, title: String) {
        try {
            val videoUrl = getVideoShareUrl(videoId)
            val displayTitle = title.replace("YouTube Video", "Video", ignoreCase = true).trim()
            val shareMessage = "Watch \"$displayTitle\" on SafeTube:\n$videoUrl"

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, displayTitle)
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }

            val chooserIntent = Intent.createChooser(sendIntent, "Share video via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to share video link", Toast.LENGTH_SHORT).show()
        }
    }
}
