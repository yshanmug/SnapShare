package com.example.snapshare.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.snapshare.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailAppBar(
    navigateUp: () -> Unit,
    title: String
) {

    TopAppBar(

        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

        },
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = stringResource(R.string.content_desc_up_navigate)
                )

            }
        }
    )
}