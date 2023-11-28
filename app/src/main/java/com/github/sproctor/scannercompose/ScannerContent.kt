package com.github.sproctor.scannercompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.SDKHandler
import kotlinx.coroutines.launch

@Composable
fun ScannerContent(
    driverLicense: DriverLicense?,
    barcodeScanner: DCSScannerInfo?,
    sdkHandler: SDKHandler,
    initScanner: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        var isInitialized by remember {
            mutableStateOf(false)
        }
        if (isInitialized) {
            if (barcodeScanner != null) {
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
            } else {
                var showDialog by remember {
                    mutableStateOf(false)
                }
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = { showDialog = true }
                ) {
                    Text("Pair")
                }
                if (showDialog) {
                    PairingDialog(sdkHandler = sdkHandler) {
                        showDialog = false
                    }
                }
            }
        } else {
            val scope = rememberCoroutineScope()
            Button(onClick = {
                scope.launch {
                    initScanner()
                    isInitialized = true
                }
            }) {
                Text("Start")
            }
        }
    }
}
