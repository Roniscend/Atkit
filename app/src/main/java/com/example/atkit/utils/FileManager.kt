package com.example.atkit.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileManager {
    fun getSessionDirectory(context: Context, sessionId: String): File {
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val sessionDir = File(mediaDir, "OralVis/Sessions/$sessionId")
        if (!sessionDir.exists()) {
            sessionDir.mkdirs()
        }
        return sessionDir
    }

    fun createImageFile(context: Context, sessionId: String): File {
        val sessionDir = getSessionDirectory(context, sessionId)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(sessionDir, "IMG_$timestamp.jpg")
    }

    fun getSessionImages(context: Context, sessionId: String): List<File> {
        val sessionDir = getSessionDirectory(context, sessionId)
        return sessionDir.listFiles { _, name -> name.startsWith("IMG_") && name.endsWith(".jpg") }
            ?.sortedBy { it.lastModified() } ?: emptyList()
    }
}
