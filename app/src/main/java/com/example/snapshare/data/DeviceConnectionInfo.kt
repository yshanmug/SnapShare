package com.example.snapshare.data

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

data class DeviceConnectionInfo(
    var endpointID: String? = null,
    var endpointName: String? = null,
    var info: DiscoveredEndpointInfo? = null,
    var status: ConnectionStatus = ConnectionStatus.NOT_CONNECTED,
    var pin: Int = 0
)

enum class ConnectionStatus {
    CONNECTED,
    NOT_CONNECTED
}