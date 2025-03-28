package com.snapshare.app.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snapshare.app.data.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEvent(event: Event)

    @Query("SELECT * FROM event WHERE eventId = :eventId")
    suspend fun findEventById(eventId: Long): Event

    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT EXISTS(SELECT 1 FROM event WHERE eventTitle = :eventTitle)")
    suspend fun isEventExist(eventTitle: String): Boolean

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

}