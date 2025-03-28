package com.snapshare.app.model

import android.net.Uri
import com.snapshare.app.data.DeviceConnectionInfo
import com.snapshare.app.data.Event
import com.snapshare.app.data.Mode

data class EventState(
    val events: List<Event> = emptyList(),
    val deviceInfoList: List<DeviceConnectionInfo> = emptyList(),
    val allImagesList: List<Uri> = emptyList(),
    val eventTitle: String = "",
    val clickedEvent: Event? = null,
    val isAddingEvent: Boolean = false,
    val isConnectionRequestReceived: Boolean = false,
    val isConnectionRequestSent: Boolean = false,
    val connectionRequestReceivedDeviceInfo: DeviceConnectionInfo? = null,
    val currentMode: Mode = Mode.OFF,
    val mediaProgress: Float = 0f,
)
