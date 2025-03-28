package com.snapshare.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapshare.app.data.EventAction
import com.snapshare.app.model.EventState

@Composable
fun ConnectionRequestReceivedDialog(
    state: EventState,
    onEventAction: (EventAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEventAction(EventAction.HideDialog)
        },
        title = { Text(text = "Incoming Connection Request") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Accept connection to ${state.connectionRequestReceivedDeviceInfo?.endpointName}")
                Text(text = "Confirm the code matches on both devices: ${state.connectionRequestReceivedDeviceInfo?.pin}")
            }
        }, confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    state.connectionRequestReceivedDeviceInfo?.let {
                        EventAction.OnConnectionReceivedRequestAccept(
                            it
                        )
                    }?.let { onEventAction(it) }
                }) {
                    Text(text = "Accept")
                }
            }
        }, dismissButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    state.connectionRequestReceivedDeviceInfo?.let {
                        EventAction.OnConnectionReceivedRequestReject(
                            it
                        )
                    }?.let { onEventAction(it) }
                }) {
                    Text(text = "Decline")
                }
            }
        }
    )
}