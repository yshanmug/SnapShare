package com.example.snapshare.navigation

import androidx.navigation.NavHostController


class AppActions(
    private val navController: NavHostController,
    destination: AppDestinations
) {
    private fun navigateTo(destination: String) {
        return navController.navigate(destination)
    }

    val onEventClicked: () -> Unit = {
        navigateTo(destination.EVENT_DETAIL_DESTINATION)
    }
    val onOpenCameraButtonClicked: () -> Unit = {
        navigateTo(destination.OPEN_CAMERA_DESTINATION)
    }
    val onHomePageNavigation: () -> Unit = {
        navigateTo(destination.EVENT_HOME_DESTINATION)
    }
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }
}
