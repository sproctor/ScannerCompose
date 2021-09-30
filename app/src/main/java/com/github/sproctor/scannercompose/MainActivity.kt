package com.github.sproctor.scannercompose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.sproctor.scannercompose.ui.theme.ScannerComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.zebra.scannercontrol.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdkHandler = SDKHandler(this, true)
        setContent {
            ScannerComposeTheme {
                ScannerContent(sdkHandler)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerContent(sdkHandler: SDKHandler) {
    var barcodeScanner by remember { mutableStateOf<DCSScannerInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(true) }
    val driverLicenseState = remember { mutableStateOf<DriverLicense?>(null)}

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Loading"
            )
        } else {
            val permissionState =
                rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionRequired(
                permissionState = permissionState,
                permissionNotGrantedContent = {
                    LaunchedEffect(Unit) {
                        permissionState.launchPermissionRequest()
                    }
                },
                permissionNotAvailableContent = {
                    Text("Access fine location permission denied")
                }
            ) {
                if (barcodeScanner == null) {
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = { showDialog = true }
                    ) {
                        Text("PAIR")
                    }
                } else {
                    val driverLicense = driverLicenseState.value
                    if (driverLicense != null) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Family name: ${driverLicense.familyName}\n" +
                                    "First name: ${driverLicense.firstName}"
                        )
                    } else {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Scan license"
                        )
                    }
                }
                if (showDialog) {
                    PairDialog(sdkHandler = sdkHandler, hideDialog = { showDialog = false })
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // Set scanner/barcode listener
        sdkHandler.dcssdkSetDelegate(object : IDcsSdkApiDelegate {
            override fun dcssdkEventBarcode(
                barcodeData: ByteArray?,
                barcodeType: Int,
                scannerId: Int
            ) {
                if (barcodeType == 17 && barcodeData != null) {
                    parseAAMVA(String(barcodeData, Charsets.ISO_8859_1))
                        ?.let { driverLicenseState.value = it }
                }
            }
            override fun dcssdkEventCommunicationSessionEstablished(scanner: DCSScannerInfo?) {
                barcodeScanner = scanner
                showDialog = false
            }
            override fun dcssdkEventCommunicationSessionTerminated(scannerId: Int) {
                if (scannerId == barcodeScanner?.scannerID) {
                    barcodeScanner = null
                }
            }
            override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) {}
            override fun dcssdkEventScannerDisappeared(p0: Int) {}
            override fun dcssdkEventImage(p0: ByteArray?, p1: Int) {}
            override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) {}
            override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) {}
            override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) {}
            override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {}
        })

        // Tell the SDK what we want to listen for
        sdkHandler.dcssdkSubsribeForEvents(
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value // for barcodes
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value // notice when a scanner connects
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value // notice when a scanner disconnects
        )

        // Allow our scanner to be discovered (when scanning the pairing barcode)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)

        // If there is already an active scanner, use it
        barcodeScanner = sdkHandler.dcssdkGetActiveScannersList()?.firstOrNull()
        loading = false
    }
}

