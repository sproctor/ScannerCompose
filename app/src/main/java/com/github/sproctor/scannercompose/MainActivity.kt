package com.github.sproctor.scannercompose

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.*

class MainActivity : ComponentActivity(), IDcsScannerEventsOnReLaunch {
    val tag = "MainActivity"

    private lateinit var sdkHandler: SDKHandler
    private var barcodeScanner by mutableStateOf<DCSScannerInfo?>(null)
    private var driverLicense by mutableStateOf<DriverLicense?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var loading by mutableStateOf(true)

        requestBluetoothPermission {
            Log.d(tag, "Permission granted")
            sdkHandler = SDKHandler(this, true)
            initBarcodeScanner()
            loading = false
        }

        setContent {
            if (loading) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Loading"
                    )
                }
            } else {
                if (checkPermissions()) {
                    ScannerContent(
                        driverLicense = driverLicense,
                        barcodeScanner = barcodeScanner,
                        sdkHandler = sdkHandler,
                        initScanner = {

                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Permission denied"
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return permissions
            .map { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .reduce { acc, b -> acc && b }
    }

    private fun requestBluetoothPermission(onGranted: () -> Unit) {
        // If you are using Android 12 and targetSdkVersion is 31 or later,
        // you have to request Bluetooth permission (Nearby devices permission) to use the Bluetooth printer.
        // https://developer.android.com/about/versions/12/features/bluetooth-permissions
        val neededPermissions = permissions.filter { checkSelfPermission(it) == PackageManager.PERMISSION_DENIED }
        if (neededPermissions.isNotEmpty()) {
            val requestPermissionsLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                    when {
                        results.values.contains(false) -> {
                            val text =
                                "Some permissions were denied and Bluetooth devices might not function properly"
                            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                        }

                        else -> onGranted()
                    }
                }
            requestPermissionsLauncher.launch(neededPermissions.toTypedArray())
        } else {
            onGranted()
        }
    }

    private fun initBarcodeScanner() {
        sdkHandler.setiDcsScannerEventsOnReLaunch(this)
        sdkHandler.dcssdkEnableAvailableScannersDetection(true)

        // Set scanner/barcode listener
        sdkHandler.dcssdkSetDelegate(object : IDcsSdkApiDelegate {
            override fun dcssdkEventBarcode(
                barcodeData: ByteArray?,
                barcodeType: Int,
                scannerId: Int
            ) {
                if (barcodeType == 17 && barcodeData != null) {
                    parseAAMVA(String(barcodeData, Charsets.ISO_8859_1))
                        ?.let { driverLicense = it }
                }
            }

            override fun dcssdkEventCommunicationSessionEstablished(scanner: DCSScannerInfo?) {
                Log.d(tag, "dcssdkEventCommunicationSessionEstablished($scanner)")
                if (scanner == null) return
                barcodeScanner = scanner
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
            override fun dcssdkEventConfigurationUpdate(p0: ConfigurationUpdateEvent?) {}
        })

        // Tell the SDK what we want to listen for
        sdkHandler.dcssdkSubsribeForEvents(
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value // for barcodes
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value // notice when a scanner connects
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value // notice when a scanner disconnects
        )

        // Allow our scanner to be discovered (when scanning the pairing barcode)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI)
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)
    }

    override fun onLastConnectedScannerDetect(p0: BluetoothDevice?): Boolean {
        return true
    }

    override fun onConnectingToLastConnectedScanner(p0: BluetoothDevice?) {
    }

    override fun onScannerDisconnect() {
    }
}

val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
} else {
    listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
}
