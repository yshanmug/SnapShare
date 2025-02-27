package com.example.snapshare.model

import com.example.snapshare.data.DeviceConnectionInfo
import com.example.snapshare.data.Event
import com.example.snapshare.data.Mode
import java.io.File

data class EventState(
    val events: List<Event> = emptyList(),
    val deviceInfoList: List<DeviceConnectionInfo> = emptyList(),
    val allImagesList: List<File> = emptyList(),
    val eventTitle: String = "",
    val clickedEvent: Event? = null,
    val isAddingEvent: Boolean = false,
    val isConnectionRequestReceived: Boolean = false,
    val isConnectionRequestSent: Boolean = false,
    val connectionRequestReceivedDeviceInfo: DeviceConnectionInfo? = null,
    val currentMode: Mode = Mode.OFF,
    val mediaProgress: Float = 0f
)
