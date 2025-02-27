package com.example.snapshare.data

import android.net.Uri

sealed interface EventAction {
    object SaveEvent : EventAction
    data class SetEventTitle(val eventTitle: String) : EventAction
    data class OnItemClicked(val clickedEvent: Event) : EventAction
    object ShowDialog : EventAction
    object HideDialog : EventAction
    data class OnConnectionReceivedRequestAccept(val deviceConnectionInfo: DeviceConnectionInfo) : EventAction
    data class OnConnectionReceivedRequestReject(val deviceConnectionInfo: DeviceConnectionInfo) : EventAction
    data class SendConnectionRequest(val deviceConnectionInfo: DeviceConnectionInfo) : EventAction
    data class OnOpenCameraButtonClicked(val event: Event) : EventAction
    data class OnCapturedImage(val capturedUri: Uri) : EventAction
    data class FetchImages(val eventTitle: String) : EventAction
    data class SendImage(val imageUri: Uri) : EventAction
    data class OnModeChange(val currentMode: Mode) : EventAction
}

enum class Mode {
    DISCOVER,
    OFF,
    ADVERTISE
}