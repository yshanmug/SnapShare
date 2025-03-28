package com.snapshare.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.snapshare.app.navigation.AppNavigation
import com.snapshare.app.ui.components.CameraPermissionTextProvider
import com.snapshare.app.ui.components.LocationPermissionTextProvider
import com.snapshare.app.ui.components.PermissionDialog
import com.snapshare.app.ui.theme.SnapShareTheme
import com.snapshare.app.ui.theme.colorPrimaryDark
import com.snapshare.app.viewmodel.SnapShareViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var snapShareViewModel: SnapShareViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    private val permissionsToRequest: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.NEARBY_WIFI_DEVICES,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,

                )
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnapShareTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = colorPrimaryDark
                )
                Box(Modifier.fillMaxSize()) {
                    val activity = LocalContext.current as Activity
                    var isAllPermissionGranted by remember { mutableStateOf(false) }
                    val dialogQueue = snapShareViewModel.visiblePermissionDialogQueue
                    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { perms ->
                            permissionsToRequest.forEach { permission ->
                                snapShareViewModel.onPermissionResult(
                                    permission = permission,
                                    isGranted = perms[permission] == true
                                )
                            }

                            Log.d("Permissione", perms.toString())
                            isAllPermissionGranted = permissionsToRequest.all { perms[it] == true }
                            Log.d("isAllPermissionGrated", isAllPermissionGranted.toString())
                        }
                    )

                    LaunchedEffect(key1 = "PermissionHandler") {
                        delay(2000)
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                        delay(8000)
                        if (isAllPermissionGranted) {
                            checkAndEnableGPS(activity)
                        }

                    }







                    dialogQueue
                        .reversed()
                        .forEach { permission ->


                            PermissionDialog(
                                permissionTextProvider =
                                when (permission) {
                                    Manifest.permission.CAMERA -> {
                                        CameraPermissionTextProvider()
                                    }

                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                        -> {
                                        LocationPermissionTextProvider()

                                    }

                                    else -> return@forEach
                                },
                                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                    permission
                                ),
                                onDismiss = snapShareViewModel::dismissDialog,
                                onOkClick = {
                                    snapShareViewModel.dismissDialog()
                                    multiplePermissionResultLauncher.launch(
                                        arrayOf(permission)
                                    )
                                },
                                onGoToAppSettingsClick = ::openAppSettings
                            )
                        }

                }

                AppNavigation(snapShareViewModel = snapShareViewModel)
            }

        }
    }


    private fun checkAndEnableGPS(activity: Activity) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
                setMinUpdateIntervalMillis(5000)
            }.build()

        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
            setAlwaysShow(true)
        }

        val settingsClient = LocationServices.getSettingsClient(activity)
        val task =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())

        task.addOnSuccessListener {
            Toast.makeText(
                activity,
                "Location services are enabled (GPS is ON)",
                Toast.LENGTH_SHORT
            ).show()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity, 1001)

                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }

            } else {
                Toast.makeText(
                    activity,
                    "Location permissions denied or GPS is disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}









