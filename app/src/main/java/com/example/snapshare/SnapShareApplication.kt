package com.example.snapshare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.snapshare.utils.MediaUtils.getCameraProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@HiltAndroidApp
class SnapShareApplication : Application() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var applicationContext: Context

    override fun onCreate() {
        super.onCreate()
        applicationContext = this.getApplicationContext()
        CoroutineScope(Dispatchers.IO).launch {
            cameraExecutor = Executors.newSingleThreadExecutor()
            cameraProvider = getCameraProvider(applicationContext)
        }
    }
    override fun onTerminate() {
        super.onTerminate()
        cameraExecutor.shutdown()
    }
    fun getCameraExecutor(): ExecutorService? {
        return cameraExecutor
    }
    fun getCameraProvider(): ProcessCameraProvider? {
        return cameraProvider
    }


}