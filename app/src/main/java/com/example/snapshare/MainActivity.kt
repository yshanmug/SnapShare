package com.example.snapshare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.snapshare.navigation.AppNavigation
import com.example.snapshare.ui.components.CameraPermissionTextProvider
import com.example.snapshare.ui.components.LocationPermissionTextProvider
import com.example.snapshare.ui.components.PermissionDialog
import com.example.snapshare.ui.components.StoragePermissionTextProvider
import com.example.snapshare.ui.theme.SnapShareTheme
import com.example.snapshare.ui.theme.colorPrimaryDark
import com.example.snapshare.viewmodel.SnapShareViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
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
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
                    val localContext = LocalContext.current
                    val dialogQueue = snapShareViewModel.visiblePermissionDialogQueue
                    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { perms ->
                            var isLocationPermissionGranted = false
                            permissionsToRequest.forEach { permission ->
                                snapShareViewModel.onPermissionResult(
                                    permission = permission,
                                    isGranted = perms[permission] == true
                                )
                                if ((permission == Manifest.permission.ACCESS_FINE_LOCATION ||
                                    permission == Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                    perms[permission] == true) {
                                    isLocationPermissionGranted = true
                                }
                            }
                            if(isLocationPermissionGranted){
                                checkAndEnableGPS(activity)
                            }
                        }
                    )
                    LaunchedEffect(key1 = "PermissionHandler") {
                        delay(2000)
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                    }
                    dialogQueue
                        .reversed()
                        .forEach { permission ->

                            if ((permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE ||
                                        permission == Manifest.permission.READ_EXTERNAL_STORAGE ||
                                        permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) && !isStoragePermissionGranted()
                            ) {
                                PermissionDialog(
                                    permissionTextProvider = StoragePermissionTextProvider(),
                                    isPermanentlyDeclined = true,
                                    onDismiss = snapShareViewModel::dismissDialog,
                                    onOkClick = {
                                        snapShareViewModel.dismissDialog()
                                    },
                                    onGoToAppSettingsClick = { askPermissionStorage() }
                                )
                            }

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


    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun askPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                openAllFilesPermissionSetting()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    121
                )
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
                "Location settings are satisfied",
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

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.openAllFilesPermissionSetting() {
    Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


