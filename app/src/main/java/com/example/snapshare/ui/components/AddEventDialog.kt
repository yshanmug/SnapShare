package com.example.snapshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapshare.data.EventAction
import com.example.snapshare.model.EventState

@Composable
fun AddEventDialog(
    onEventAction: (EventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val eventTitle = remember { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEventAction(EventAction.HideDialog)
        },
        title = { Text(text = "Add Event") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = eventTitle.value,
                    onValueChange = {
                        eventTitle.value = it
                    },
                    placeholder = {
                        Text(text = "Event Title")
                    }
                )

            }
        }, confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    onEventAction(EventAction.SetEventTitle(eventTitle.value))
                    onEventAction(EventAction.SaveEvent)
                }) {
                    Text(text = "Create")
                }
            }
        }
    )
}