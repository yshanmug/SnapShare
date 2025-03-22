package com.example.snapshare.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snapshare.ui.screens.CameraView
import com.example.snapshare.ui.screens.EventScreen
import com.example.snapshare.ui.screens.HomeScreen
import com.example.snapshare.ui.screens.SplashScreen
import com.example.snapshare.viewmodel.SnapShareViewModel

@Composable
fun AppNavigation(
    startDestination: String = AppDestinations.SPLASH_SCREEN_DESTINATION,
    snapShareViewModel: SnapShareViewModel,
) {
    val navController = rememberNavController()
    val destinations = AppDestinations
    val actions = remember(navController) {
        AppActions(navController, destinations)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        setDestination(destinations, snapShareViewModel, actions = actions)
    }
}

private fun NavGraphBuilder.setDestination(
    destinations: AppDestinations,
    snapShareViewModel: SnapShareViewModel,
    actions: AppActions,
) {

    composable(destinations.SPLASH_SCREEN_DESTINATION)
    {
        SplashScreen(onNextPage = actions.onHomePageNavigation)
    }
    composable(
        destinations.EVENT_HOME_DESTINATION
    ) {
        val state by snapShareViewModel.state.collectAsState()
        HomeScreen(
            state = state,
            snapShareViewModel = snapShareViewModel,
            onEventAction = snapShareViewModel::onEventAction,
            onEventClicked = actions.onEventClicked,
            onOpenCameraButtonClicked = actions.onOpenCameraButtonClicked
        )
    }
    composable(
        destinations.EVENT_DETAIL_DESTINATION
    ) {
        val state by snapShareViewModel.state.collectAsState()
        EventScreen(state = state, navigateUp = actions.navigateUp)
    }
    composable(
        destinations.OPEN_CAMERA_DESTINATION
    ) {
        val state by snapShareViewModel.state.collectAsState()
        if (snapShareViewModel.getCameraExecutor() != null && snapShareViewModel.getCameraProvider() != null) {
            CameraView(
                outputDirectory = snapShareViewModel.getExternalImageDirectoryFile(),
                executor = snapShareViewModel.getCameraExecutor()!!,
                cameraProvider = snapShareViewModel.getCameraProvider()!!,
                state = state,
                onEventAction = snapShareViewModel::onEventAction
            )
        }
    }

}
