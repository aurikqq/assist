package com.aurikqq.assist.commands

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun captureScreenshot(activity: Activity, callback: (File?) -> Unit) {
    val rootView = activity.window.decorView.rootView
    val bitmap = createBitmap(rootView.width, rootView.height)
    val canvas = Canvas(bitmap)
    rootView.draw(canvas)

    saveBitmapToFile(activity, bitmap, callback)
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap, callback: (File?) -> Unit) {
    val timestamp = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
    val filename = "assist_screenshot_$timestamp.png"

    var outputStream: OutputStream? = null
    var imageFile: File? = null

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + File.separator + "Screenshots")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = imageUri?.let { resolver.openOutputStream(it) }
        }
        else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "Screenshots"
            val dir = File(imagesDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            imageFile = File(dir, filename)
            outputStream = FileOutputStream(imageFile)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            callback(imageFile)
        }
    }
    catch (ex: IOException) {
        Log.e("Screenshot", "Failed to save screenshot: ${ex.message}")
        callback(null)
    }
    finally {
        outputStream?.close()
    }
}
