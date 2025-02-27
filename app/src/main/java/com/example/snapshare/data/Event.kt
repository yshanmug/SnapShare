package com.example.snapshare.data

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event")
data class Event(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "eventId")
    var eventId: Long = 0L,

    @ColumnInfo(name = "eventTitle")
    var eventTitle: String,

    @ColumnInfo(name = "eventColor")
    var color: Int
)
