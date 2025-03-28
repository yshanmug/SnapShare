package com.snapshare.app.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.snapshare.app.data.Event
import com.snapshare.app.db.EventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val context: Context,
) {

    suspend fun addEvent(newEvent: Event) = eventDao.addEvent(newEvent)

    suspend fun isEventExists(eventTitle: String): Boolean =
        eventDao.isEventExist(eventTitle = eventTitle)

    suspend fun updateEvent(updatedEvent: Event) = eventDao.updateEvent(updatedEvent)

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()


    suspend fun saveImageToMediaStore(eventTitle: String, bitmap: Bitmap): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/SnapShare/$eventTitle")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                }
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    suspend fun getEventImages(eventTitle: String): List<Uri> {
        return withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
            )

            val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%SnapShare/$eventTitle%")

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val uris = mutableListOf<Uri>()
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    uris.add(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    )
                }
                uris
            } ?: emptyList()
        }
    }

    suspend fun getLatestEventImage(eventTitle: String): Uri? {
        return getEventImages(eventTitle).firstOrNull()

    }

    suspend fun getEventImageCount(eventTitle: String): Int {
        return getEventImages(eventTitle).size
    }


}