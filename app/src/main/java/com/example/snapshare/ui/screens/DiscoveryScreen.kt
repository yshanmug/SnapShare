package com.example.snapshare.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.snapshare.data.ConnectionStatus
import com.example.snapshare.data.DeviceConnectionInfo
import com.example.snapshare.data.EventAction
import com.example.snapshare.data.Mode
import com.example.snapshare.model.EventState
import com.example.snapshare.ui.theme.greenColor
import com.example.snapshare.ui.theme.redColor
import com.example.snapshare.utils.MediaUtils
import kotlin.math.roundToInt

@Composable
fun DiscoveryScreen(
    state: EventState,
    onEventAction: (EventAction) -> Unit,
    onDismiss: () -> Unit,
) {
    Surface {
        Scaffold(
        ) { it ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {

                val screenTotalHeightInPixels = maxHeight.value
                val screenTotalWidthInPixels = maxWidth.value
                val radiusAnimation = rememberInfiniteTransition()
                val radius by radiusAnimation.animateFloat(
                    initialValue = 50f,
                    targetValue = 150f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val strokeWidth = 1.dp.toPx()
                    val numCircles = 50

                    for (i in 1..numCircles) {
                        val circleRadius = radius + (i - 1) * 50f
                        val normalizedRadius = (circleRadius - radius) / (numCircles * 25f - radius)
                        var colorValue = (255f * (1f - normalizedRadius)).toInt()
                        if (colorValue < 0) {
                            colorValue = 0
                        }
                        val color = Color(255 - colorValue, 255 - colorValue, 255 - colorValue)
                        drawCircle(
                            color = color,
                            radius = circleRadius,
                            center = center,
                            style = Stroke(strokeWidth)
                        )

                    }
                }
                Card(
                    modifier = Modifier
                        .size(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = redColor,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "K", fontWeight = FontWeight.Bold)
                    }

                }
                for (deviceInfo in state.deviceInfoList) {
                    deviceInfoItem(
                        screenTotalHeightInPixels = screenTotalHeightInPixels,
                        screenTotalWidthInPixels = screenTotalWidthInPixels,
                        deviceConnectionInfo = deviceInfo,
                        onEventAction
                    )
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    IconButton(modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10))
                        .background(redColor), onClick = {
                        onEventAction(EventAction.OnModeChange(Mode.OFF))
                        onDismiss.invoke()
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Stop Discover",
                                color = Color.White
                            )
                        }
                    }

                }

            }

        }


    }


}

@Composable
fun deviceInfoItem(
    screenTotalHeightInPixels: Float,
    screenTotalWidthInPixels: Float,
    deviceConnectionInfo: DeviceConnectionInfo,
    onEventAction: (EventAction) -> Unit,
) {

    val pinNotZero = remember {
        mutableStateOf(false)
    }
    pinNotZero.value = deviceConnectionInfo.pin == 0

    val bubbleRadius: Float by animateFloatAsState(
        if (pinNotZero.value) 50f else 100f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    val bubbleRoundPercentage: Float by animateFloatAsState(
        if (pinNotZero.value) 50f else 15f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow,

            )
    )
    val bubbleRoundAngle: Float by animateFloatAsState(
        if (pinNotZero.value) 90f else 0f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )
    val x = remember {
        MediaUtils.getRandomPositions(screenTotalWidthInPixels - 80)
    }
    val y = remember {
        MediaUtils.getRandomPositions(screenTotalHeightInPixels - 80)
    }
    var offsetX by remember { mutableFloatStateOf(x) }
    var offsetY by remember { mutableFloatStateOf(y) }

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .clipToBounds()
                .padding(10.dp)
                .size(bubbleRadius.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .rotate(bubbleRoundAngle)
                .clip(RoundedCornerShape(bubbleRoundPercentage.toInt()))
                .border(
                    width = 5.dp,
                    color = if (deviceConnectionInfo.status == ConnectionStatus.NOT_CONNECTED) redColor else greenColor,
                    shape = RoundedCornerShape(bubbleRoundPercentage.toInt())
                )
                .clickable(indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    onEventAction(EventAction.SendConnectionRequest(deviceConnectionInfo))
                }, elevation = CardDefaults.cardElevation(
                defaultElevation = 90.dp, pressedElevation = 90.dp,
                focusedElevation = 90.dp,
                hoveredElevation = 90.dp,
                draggedElevation = 90.dp,
                disabledElevation = 90.dp
            )

        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (deviceConnectionInfo.status == ConnectionStatus.NOT_CONNECTED) redColor else greenColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        modifier = Modifier.rotate(360 - bubbleRoundAngle),
                        color = Color.White,
                        text = deviceConnectionInfo.endpointName.toString()[0].toString(),
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = !pinNotZero.value) {
                        Text(
                            modifier = Modifier.rotate(360 - bubbleRoundAngle),
                            color = Color.White,
                            text = "PIN: ${deviceConnectionInfo.pin}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        }

    }


}


