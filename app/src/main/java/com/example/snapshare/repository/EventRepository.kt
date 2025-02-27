package com.example.snapshare.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import com.example.snapshare.db.EventDao
import com.example.snapshare.data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class EventRepository @Inject constructor(private val eventDao: EventDao) {

    suspend fun addEvent(newEvent: Event) = eventDao.addEvent(newEvent)

    suspend fun isEventExists(eventTitle: String): Boolean =
        eventDao.isEventExist(eventTitle = eventTitle)

    suspend fun updateEvent(updatedEvent: Event) = eventDao.updateEvent(updatedEvent)

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    fun getLatestImageFileFromDirectory(eventTitle: String): File? {
        var latestImageFile: File? = null
        val directoryPath =
            "${Environment.getExternalStorageDirectory().path}/SnapShare/${eventTitle}"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null && files.isNotEmpty()) {
                latestImageFile = files.maxByOrNull { it.lastModified() }
            }
        }
        return latestImageFile
    }

    fun getTotalNumberOfImageFileFromDirectory(eventTitle: String): Int {
        var totalNumberOfImage = 0
        val directoryPath =
            "${Environment.getExternalStorageDirectory().path}/SnapShare/${eventTitle}"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null && files.isNotEmpty()) {
                totalNumberOfImage = files.size
            }
        }
        return totalNumberOfImage
    }

    suspend fun freshAllImagesFromDirectory(eventTitle: String): List<File> = withContext(
        Dispatchers.IO
    ) { // Replace with the actual directory path
        val directoryPath =
            "${Environment.getExternalStorageDirectory().path}/SnapShare/${eventTitle}"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()

        return@withContext files
    }

    fun getExternalImageDirectoryFile(
        title: String = "",
        currentClickedEvent: MutableStateFlow<Event?>
    ): File {
        val secondaryStorageDir = Environment.getExternalStorageDirectory()
        val snapShareFolder = File(secondaryStorageDir, "SnapShare")
        val eventFolder = File(
            snapShareFolder,
            if (title.isNotBlank() && title.isNotEmpty()) title else if (currentClickedEvent.value == null) "All trips" else currentClickedEvent.value!!.eventTitle
        )
        if (!snapShareFolder.exists()) {
            snapShareFolder.mkdirs()
        }
        if (!eventFolder.exists()) {
            eventFolder.mkdirs()
        }
        return eventFolder
    }

    fun saveImage(context: Context, imageFile: File) {
        try {
            val resolver = context.contentResolver
            val imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
            }
            resolver.insert(imageCollection, imageDetails)
            MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}