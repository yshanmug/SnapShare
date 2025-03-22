package com.example.snapshare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

object MediaUtils {

    suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get())
                }, ContextCompat.getMainExecutor(context))
            }
        }

    fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                return BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getRandomPositions(totalPixels: Float): Float {
        val random = Random.Default
        return (random.nextDouble(30.toDouble(), totalPixels.toDouble())).toFloat()
    }


}