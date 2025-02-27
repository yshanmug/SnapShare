package com.example.snapshare.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.snapshare.R
import com.example.snapshare.data.ConnectionStatus
import com.example.snapshare.data.EventAction
import com.example.snapshare.data.Mode
import com.example.snapshare.data.Event
import com.example.snapshare.model.EventState
import com.example.snapshare.ui.components.AddEventDialog
import com.example.snapshare.ui.components.ConnectionRequestReceivedDialog
import com.example.snapshare.ui.components.CustomSlider
import com.example.snapshare.ui.components.FabButton
import com.example.snapshare.ui.theme.colorPrimaryDark

import com.example.snapshare.viewmodel.SnapShareViewModel
import java.util.Locale


@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: EventState,
    snapShareViewModel: SnapShareViewModel,
    onEventAction: (EventAction) -> Unit,
    onEventClicked: () -> Unit,
    onOpenCameraButtonClicked: () -> Unit
) {
    val showSheet = remember { MutableTransitionState(initialState = false) }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Surface {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(colorPrimaryDark),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_transparent),
                        contentDescription = stringResource(R.string.content_desc_up_navigate),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .padding(10.dp)
                            .width(150.dp)
                            .height(50.dp)
                    )
                }
            }
        ) { it ->

            if (state.isAddingEvent) {
                AddEventDialog(onEventAction = onEventAction)
            }
            if (state.isConnectionRequestReceived) {
                ConnectionRequestReceivedDialog(state = state, onEventAction = onEventAction)
            }

            AddItemToList(
                Modifier.padding(it),
                state,
                snapShareViewModel = snapShareViewModel,
                onEventAction,
                onEventClicked,
                onOpenCameraButtonClicked
            )

            val visibleState = remember { MutableTransitionState(initialState = false) }

            visibleState.targetState = state.currentMode == Mode.DISCOVER

            AnimatedVisibility(
                visibleState = visibleState, enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 1000)
                )
            ) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter)
                {
                    val currentDeviceInfoList =
                        state.deviceInfoList.count { it.status == ConnectionStatus.CONNECTED }
                    FabButton(deviceCount = currentDeviceInfoList) {
                        showSheet.targetState = !showSheet.currentState
                    }
                }
            }

            AnimatedVisibility(
                visibleState = showSheet
            ) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSheet.targetState = false
                    },
                    sheetState = modalBottomSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    shape = RoundedCornerShape(15.dp),
                ) {

                    DiscoveryScreen(
                        state = state,
                        onEventAction = onEventAction,
                        onDismiss = { showSheet.targetState = false }
                    )

                }


            }


        }


    }
}


@Composable
fun AddItemToList(
    modifier: Modifier,
    state: EventState,
    snapShareViewModel: SnapShareViewModel,
    onEventAction: (EventAction) -> Unit,
    onEventClicked: () -> Unit,
    onOpenCameraButtonClicked: () -> Unit
) {
    val eventList = remember { mutableStateListOf<Event>() }

    val isVisibleStates = remember {
        mutableStateMapOf<Event, Boolean>()
            .apply {
                eventList.map { event ->
                    event to false
                }.toMap().also {
                    putAll(it)
                }
            }
    }

    Scaffold(modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp))
                    .background(colorPrimaryDark)
            ) {
                CustomSlider(
                    selectedOption = state.currentMode,
                    onOptionSelected = { option ->
                        onEventAction(EventAction.OnModeChange(option))
                    }
                )

                IconButton(modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10))
                    .background(Color.White), onClick = {
                    onEventAction(EventAction.ShowDialog)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Icon",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create new Event",
                            color = Color.Black
                        )
                    }
                }


            }
        }
    ) { innerPadding ->
        state.events
        EventListBody(
            modifier = Modifier.padding(innerPadding),
            state = state,
            snapShareViewModel = snapShareViewModel,
            onEventAction = onEventAction,
            onEventClicked = onEventClicked,
            onOpenCameraButtonClicked

        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EventListBody(
    modifier: Modifier,
    state: EventState,
    snapShareViewModel: SnapShareViewModel,
    onEventAction: (EventAction) -> Unit,
    onEventClicked: () -> Unit,
    onOpenCameraButtonClicked: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp))
            .background(
                Color(
                    0xFFFFFFFF
                )
            )
    ) {

        itemsIndexed(state.events) { index, event ->
            val imageFile = snapShareViewModel.getLatestImageFileFromDirectory(event.eventTitle)
            val numberOfImageFiles =
                snapShareViewModel.getTotalNumberOfImageFileFromDirectory(event.eventTitle)
            val isImageAvailable = imageFile != null


            Card(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), colors = CardDefaults.cardColors(Color(0xFFFAFAFA)),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                ), onClick = {
                    onEventAction(EventAction.OnItemClicked(event))
                    onEventAction(EventAction.FetchImages(event.eventTitle))
                    onEventClicked.invoke()
                }
            ) {

                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AnimatedContent(
                            targetState = isImageAvailable
                        ) { targetState ->
                            if (targetState) {
                                val coverImage = rememberAsyncImagePainter(model = imageFile)
                                Image(
                                    painter = coverImage,
                                    contentDescription = "EventImage",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .size(100.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(color = Color(event.color)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        event.eventTitle[0].toString().uppercase(Locale.ROOT),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 25.sp
                                    )

                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        )
                        {
                            Text(
                                text = event.eventTitle,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = if (numberOfImageFiles > 0) "$numberOfImageFiles images" else "0 image",
                                color = Color.LightGray,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }

                    }
                    Box(
                        modifier = Modifier
                            .clickable {
                                onOpenCameraButtonClicked.invoke()
                                onEventAction(EventAction.OnOpenCameraButtonClicked(event))
                            }
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            tint = Color.Black
                        )
                    }

                }

            }
        }
    }
}



















