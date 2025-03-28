package com.snapshare.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.snapshare.app.R
import com.snapshare.app.ui.theme.colorPrimaryDark
import com.snapshare.app.ui.theme.colorPrimaryLight
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onNextPage: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorPrimaryLight,
                        colorPrimaryDark
                    )
                )
            ), contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.splash_icon),
            contentDescription = "EventImage",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        LaunchedEffect(key1 = "SplashScreen")
        {
            delay(1000)
            onNextPage.invoke()
        }

    }
}