package com.snapshare.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.snapshare.app.model.EventState
import com.snapshare.app.ui.components.EventDetailAppBar
import com.snapshare.app.ui.theme.redColor

@Composable
fun EventScreen(
    state: EventState,
    navigateUp: () -> Unit,
) {
    Surface {
        Scaffold(
            topBar = {
                EventDetailAppBar(
                    navigateUp = navigateUp, title = state.clickedEvent?.eventTitle ?: ""
                )
            }
        ) { it ->
            val lazyGridState = rememberLazyGridState()
            LaunchedEffect(state.allImagesList.size) {
                if (state.allImagesList.isNotEmpty())
                    lazyGridState.animateScrollToItem(state.allImagesList.lastIndex)
            }
            LazyVerticalGrid(modifier = Modifier.padding(it),
                state = lazyGridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp,
                    bottom = 16.dp
                ),
                content = {
                    items(state.allImagesList) { file ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = redColor),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            shape = RoundedCornerShape(3)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(file),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            )
        }

    }

}





