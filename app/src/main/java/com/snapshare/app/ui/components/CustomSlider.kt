package com.snapshare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapshare.app.data.Mode
import com.snapshare.app.ui.theme.greenColor
import com.snapshare.app.ui.theme.redColor


@Composable
fun CustomSlider(
    selectedOption: Mode,
    onOptionSelected: (Mode) -> Unit,
) {

    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(modifier = Modifier
                .width(100.dp)
                .height(60.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(if (selectedOption == Mode.DISCOVER) greenColor else Color.Transparent)
                .clickable {
                    onOptionSelected(Mode.DISCOVER)
                }
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Receive",
                    color = Color.White
                )
            }
            Box(modifier = Modifier
                .width(100.dp)
                .height(60.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(if (selectedOption == Mode.OFF) redColor else Color.Transparent)
                .clickable {
                    onOptionSelected(Mode.OFF)
                }
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ), contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Off",
                    color = Color.White
                )

            }
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(if (selectedOption == Mode.ADVERTISE) greenColor else Color.Transparent)
                    .clickable {
                        onOptionSelected(Mode.ADVERTISE)

                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Send",
                    color = Color.White
                )

            }
        }

    }
}









