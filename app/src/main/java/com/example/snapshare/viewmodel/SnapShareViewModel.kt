package com.example.snapshare.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshare.SnapShareApplication
import com.example.snapshare.data.ConnectionStatus
import com.example.snapshare.data.DeviceConnectionInfo
import com.example.snapshare.data.EventAction
import com.example.snapshare.repository.EventRepository
import com.example.snapshare.data.Mode
import com.example.snapshare.constants.SnapShareConstants
import com.example.snapshare.data.Event
import com.example.snapshare.model.EventState
import com.example.snapshare.utils.MediaUtils
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import javax.inject.Inject


@HiltViewModel
class SnapShareViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val context: Context
) :
    ViewModel() {
    private val _state = MutableStateFlow(EventState())
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    private val events: StateFlow<List<Event>> get() = _events
    private val _allImages = MutableStateFlow<List<File>>(emptyList())
    private val allImages: StateFlow<List<File>> get() = _allImages
    private val _deviceInfoList = MutableStateFlow<List<DeviceConnectionInfo>>(emptyList())
    private val deviceInfoList: StateFlow<List<DeviceConnectionInfo>> get() = _deviceInfoList
    private var connectionsClient: ConnectionsClient
    private val SERVICE_ID = SnapShareConstants.PACKAGE_NAME
    private var currentClickedEvent: MutableStateFlow<Event?> = MutableStateFlow(null)
    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    val state = combine(
        _state,
        events,
        deviceInfoList,
        allImages
    ) { state, events, deviceInfoList, allImages ->
        state.copy(
            events = events,
            deviceInfoList = deviceInfoList,
            allImagesList = allImages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventState())


    init {
        getAllEvents()
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    private fun getAllEvents() {
        viewModelScope.launch {
            eventRepository.getAllEvents().collect { events ->
                _events.value = events
            }
        }
    }

    fun onEventAction(eventAction: EventAction) {
        when (eventAction) {
            EventAction.HideDialog -> {
                _state.update {
                    it.copy(
                        isAddingEvent = false
                    )
                }
            }

            EventAction.SaveEvent -> {
                val eventTitle = state.value.eventTitle

                if (eventTitle.isBlank()) {
                    return
                }
                val event = Event(
                    eventTitle = eventTitle,
                    color = (Math.random() * 16777215).toInt() or (0xFF shl 24)
                )

                viewModelScope.launch {
                    eventRepository.addEvent(event)
                }

                _state.update {
                    it.copy(
                        isAddingEvent = false,
                        eventTitle = ""
                    )
                }
            }

            is EventAction.SetEventTitle -> {
                _state.update {
                    it.copy(
                        eventTitle = eventAction.eventTitle
                    )
                }
            }

            EventAction.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingEvent = true
                    )
                }
            }

            is EventAction.OnItemClicked -> {
                _state.update {
                    it.copy(
                        clickedEvent = eventAction.clickedEvent,
                    )
                }
                currentClickedEvent.value = eventAction.clickedEvent
            }

            is EventAction.SendConnectionRequest -> {
                sendConnectionRequest(eventAction.deviceConnectionInfo)
                _state.update {
                    it.copy(
                        isConnectionRequestSent = true
                    )
                }
            }

            is EventAction.OnConnectionReceivedRequestAccept -> {
                acceptConnection(eventAction.deviceConnectionInfo)
                _state.update {
                    it.copy(
                        isConnectionRequestReceived = false,
                        connectionRequestReceivedDeviceInfo = null
                    )
                }
            }

            is EventAction.OnConnectionReceivedRequestReject -> {
                rejectConnection(eventAction.deviceConnectionInfo)
                _state.update {
                    it.copy(
                        isConnectionRequestReceived = false,
                        connectionRequestReceivedDeviceInfo = null
                    )
                }
            }

            is EventAction.OnOpenCameraButtonClicked -> {
                _state.update {
                    it.copy(
                        clickedEvent = eventAction.event
                    )
                }
                currentClickedEvent.value = state.value.clickedEvent
            }

            is EventAction.OnCapturedImage -> {
                saveImage(context, imageFile = eventAction.capturedUri.toFile())
            }

            is EventAction.FetchImages -> {
                freshAllImagesFromDirectory(eventAction.eventTitle)
            }

            is EventAction.SendImage -> {
                transferImage(eventAction.imageUri)
            }

            is EventAction.OnModeChange -> {
                _state.update { it.copy(currentMode = eventAction.currentMode) }
                when (eventAction.currentMode) {
                    Mode.ADVERTISE -> {
                        stopDiscover()
                        startAdvertising()
                    }

                    Mode.DISCOVER -> {
                        stopAdvertising()
                        startDiscover()
                    }

                    Mode.OFF -> {
                        stopDiscover()
                        stopAdvertising()
                    }
                }
            }
        }
    }

    fun getLatestImageFileFromDirectory(eventTitle: String): File? {
        return eventRepository.getLatestImageFileFromDirectory(eventTitle)
    }

    fun getTotalNumberOfImageFileFromDirectory(eventTitle: String): Int {
        return eventRepository.getTotalNumberOfImageFileFromDirectory(eventTitle)
    }

    private fun freshAllImagesFromDirectory(eventTitle: String) {
        viewModelScope.launch {
            _allImages.value = eventRepository.freshAllImagesFromDirectory(eventTitle)
        }
    }

    fun getExternalImageDirectoryFile(title: String = ""): File {
        return eventRepository.getExternalImageDirectoryFile(title, currentClickedEvent)
    }

    private fun saveImage(context: Context, imageFile: File) {
        eventRepository.saveImage(context, imageFile)
    }

    fun processPayloadAndSaveImage(
        imagePayload: Payload,
        textPayload: Payload,
        endPointId: String
    ) {
        val text = textPayload.asBytes()?.toString(Charsets.UTF_8)
        val imageUri = imagePayload.asFile()?.asUri()
        try {
            if (imageUri != null && !text.isNullOrBlank()) {
                val directory = getExternalImageDirectoryFile(text)
                val photoFile = File(
                    directory,
                    SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss-SSS",
                        Locale.CANADA
                    ).format(System.currentTimeMillis()) + ".jpg"
                )
                val inputStream = context.contentResolver.openInputStream(imageUri)
                photoFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                inputStream?.close()
                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!eventRepository.isEventExists(text)) {
                            val event = Event(
                                eventTitle = text,
                                color = (Math.random() * 16777215).toInt() or (0xFF shl 24)
                            )
                            eventRepository.addEvent(event)
                        }
                    }
                }
                saveImage(context = context, photoFile)
                freshAllImagesFromDirectory(text)
            }
        } catch (_: Exception) {
        } finally {
            if (imageUri != null) {
                val currentDeviceInfoList = _deviceInfoList.value.toMutableList()
                val connectDeviceList: List<String> =
                    currentDeviceInfoList.mapNotNull { deviceConnectionInfo ->
                        if (deviceConnectionInfo.endpointID != endPointId) {
                            deviceConnectionInfo.endpointID
                        } else {
                            null
                        }
                    }
                if (connectDeviceList.isNotEmpty()) {
                    val bitmap = MediaUtils.uriToBitmap(imageUri, context)
                    val file = File(context.cacheDir, "image_temp.jpg")
                    val imageFileStream = FileOutputStream(file)
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, imageFileStream)
                    imageFileStream.close()
                    val imgPayload = Payload.fromFile(file)
                    val txtPayload = Payload.fromBytes(text!!.toByteArray())
                    sendPayload(connectDeviceList, txtPayload)
                    sendPayload(connectDeviceList, imgPayload)
                }
                context.contentResolver.delete(imageUri, null, null)
            }

        }

    }

    fun getCameraExecutor(): ExecutorService? {
        return if (context is SnapShareApplication) context.getCameraExecutor() else null
    }

    fun getCameraProvider(): ProcessCameraProvider? {
        return if (context is SnapShareApplication) context.getCameraProvider() else null
    }

    private fun transferImage(imageUri: Uri) {
        try {
            val bitmap = MediaUtils.uriToBitmap(imageUri, context)
            val file = File(context.cacheDir, "image_temp.jpg")
            val imageFileStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, imageFileStream)
            imageFileStream.close()
            val imagePayload = Payload.fromFile(file)
            val textBytes = currentClickedEvent.value?.eventTitle
            val textPayload = Payload.fromBytes(textBytes!!.toByteArray())
            val currentDeviceInfoList = _deviceInfoList.value.toMutableList()
            val connectDeviceList: List<String> =
                currentDeviceInfoList.mapNotNull { deviceConnectionInfo ->
                    if (deviceConnectionInfo.status == ConnectionStatus.CONNECTED) {
                        deviceConnectionInfo.endpointID
                    } else {
                        null
                    }
                }
            sendPayload(connectDeviceList, textPayload)
            sendPayload(connectDeviceList, imagePayload)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun sendPayload(connectedList: List<String>, payload: Payload) {
        connectionsClient.sendPayload(connectedList, payload)
    }

    private fun sendConnectionRequest(deviceConnectionInfo: DeviceConnectionInfo) {
        connectionsClient.requestConnection(
            Build.MODEL,
            deviceConnectionInfo.endpointID!!,
            ConnectingProcessCallback()
        )
            .addOnSuccessListener {
                acceptConnection(deviceConnectionInfo)
            }
    }

    private fun acceptConnection(deviceConnectionInfo: DeviceConnectionInfo) {
        Nearby.getConnectionsClient(context)
            .acceptConnection(deviceConnectionInfo.endpointID!!, DataReceivedCallback())
    }

    private fun rejectConnection(deviceConnectionInfo: DeviceConnectionInfo) {
        Nearby.getConnectionsClient(context).rejectConnection(deviceConnectionInfo.endpointID!!)
    }

    private inner class DataReceivedCallback : PayloadCallback() {
        private var imagePayloadList: MutableMap<Long, Payload> = mutableMapOf()
        private var textPayloadList: MutableMap<Long, Payload> = mutableMapOf()
        var textPayload: Payload? = null
        var imagePayload: Payload? = null
        override fun onPayloadReceived(endPointId: String, payload: Payload) {
            _state.update {
                it.copy(mediaProgress = 0f)
            }
            if (payload.type == Payload.Type.FILE) {
                imagePayload = null
                val payloadId = payload.id
                imagePayloadList[payloadId] = payload
            } else if (payload.type == Payload.Type.BYTES) {
                textPayload = null
                val payloadId = payload.id
                textPayloadList[payloadId] = payload
            }
        }

        override fun onPayloadTransferUpdate(endPointId: String, update: PayloadTransferUpdate) {

            val payloadStatus = update.status
            val payloadId = update.payloadId

            when (payloadStatus) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    if (update.bytesTransferred == update.totalBytes) {
                        if (imagePayloadList.containsKey(payloadId)) {
                            imagePayload = imagePayloadList[payloadId]
                            if (imagePayload != null && textPayload != null) {
                                processPayloadAndSaveImage(
                                    imagePayload!!,
                                    textPayload!!,
                                    endPointId
                                )
                            }
                        } else if (textPayloadList.containsKey(payloadId)) {
                            textPayload = textPayloadList[payloadId]
                        }
                    }
                }

                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    val bytesTransferred = update.bytesTransferred
                    val totalBytes = update.totalBytes
                    val progressPercentage = (bytesTransferred * 100 / totalBytes).toFloat()
                    _state.update {
                        it.copy(mediaProgress = progressPercentage)
                    }
                }
            }
        }
    }

    private fun startAdvertising() {
        val advertisingOptions =
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startAdvertising(
            android.os.Build.MODEL, SERVICE_ID, ConnectingProcessCallback(), advertisingOptions
        )
    }

    private fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
    }

    private fun stopDiscover() {
        connectionsClient.stopDiscovery()
        val emptyList: MutableList<DeviceConnectionInfo> = mutableListOf()
        _deviceInfoList.value = emptyList
        connectionsClient.stopAllEndpoints()
    }

    private fun startDiscover() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        CoroutineScope(Dispatchers.IO).launch {
            val emptyDeviceInfoList: MutableList<DeviceConnectionInfo> = mutableListOf()
            _deviceInfoList.value = emptyDeviceInfoList
            connectionsClient.startDiscovery(SERVICE_ID, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endPointId: String, info: DiscoveredEndpointInfo) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentDeviceInfoList = _deviceInfoList.value.toMutableList()
                        val deviceInfo = DeviceConnectionInfo(
                            endpointID = endPointId,
                            endpointName = info.endpointName,
                            info = info
                        )
                        val existingDeviceInfo =
                            currentDeviceInfoList.find { it.endpointName == deviceInfo.endpointName && it.endpointID == deviceInfo.endpointID && it.info == deviceInfo.info }
                        if (existingDeviceInfo != null) {
                            val index = currentDeviceInfoList.indexOf(existingDeviceInfo)
                            currentDeviceInfoList[index] = deviceInfo
                        } else {
                            currentDeviceInfoList.add(deviceInfo)
                        }
                        _deviceInfoList.value = currentDeviceInfoList
                    }
                }

                override fun onEndpointLost(endPointId: String) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentDeviceInfoList =
                            _deviceInfoList.value.toMutableList()
                        val existingDeviceInfo =
                            currentDeviceInfoList.find { it.endpointID == endPointId }
                        if (existingDeviceInfo != null) {
                            val index = currentDeviceInfoList.indexOf(existingDeviceInfo)
                            currentDeviceInfoList.removeAt(index)
                            _deviceInfoList.value = currentDeviceInfoList
                        }
                    }
                }
            }, discoveryOptions)
        }
    }

    private inner class ConnectingProcessCallback : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endPointId: String, info: ConnectionInfo) {
            CoroutineScope(Dispatchers.IO).launch {
                val deviceInfo = DeviceConnectionInfo(
                    endpointID = endPointId,
                    endpointName = info.endpointName,
                    pin = info.authenticationDigits.toInt()
                )
                if (state.value.currentMode == Mode.DISCOVER) {
                    val currentDeviceInfoList = _deviceInfoList.value.toMutableList()
                    val existingDeviceInfo =
                        currentDeviceInfoList.find { it.endpointName == deviceInfo.endpointName && it.endpointID == deviceInfo.endpointID }
                    if (existingDeviceInfo != null) {
                        val index = currentDeviceInfoList.indexOf(existingDeviceInfo)
                        currentDeviceInfoList[index] = deviceInfo
                    } else {
                        currentDeviceInfoList.add(deviceInfo)
                    }
                    _deviceInfoList.value = currentDeviceInfoList
                } else if (state.value.currentMode == Mode.ADVERTISE) {
                    _state.update {
                        it.copy(
                            isConnectionRequestReceived = true,
                            connectionRequestReceivedDeviceInfo = deviceInfo
                        )
                    }
                }
            }
        }

        override fun onConnectionResult(endPointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    val deviceInfo = DeviceConnectionInfo(
                        endpointID = endPointId,
                        pin = 0,
                        status = ConnectionStatus.CONNECTED
                    )
                    val currentDeviceInfoList = _deviceInfoList.value.toMutableList()
                    val existingDeviceInfo =
                        currentDeviceInfoList.find { it.endpointID == deviceInfo.endpointID }
                    if (existingDeviceInfo != null) {
                        val index = currentDeviceInfoList.indexOf(existingDeviceInfo)
                        currentDeviceInfoList[index] = deviceInfo
                    } else {
                        currentDeviceInfoList.add(deviceInfo)
                    }
                    _deviceInfoList.value = currentDeviceInfoList
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {

                    val deviceInfo = DeviceConnectionInfo(
                        endpointID = endPointId,
                        pin = 0,
                        status = ConnectionStatus.NOT_CONNECTED
                    )
                    val currentDeviceInfoList = _deviceInfoList.value.toMutableList()

                    val existingDeviceInfo =
                        currentDeviceInfoList.find { it.endpointName == deviceInfo.endpointName && it.endpointID == deviceInfo.endpointID }
                    if (existingDeviceInfo != null) {
                        val index = currentDeviceInfoList.indexOf(existingDeviceInfo)
                        currentDeviceInfoList[index] = deviceInfo
                        _deviceInfoList.value = currentDeviceInfoList
                    }

                }
            }
        }

        override fun onDisconnected(p0: String) {}
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }
    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

}
