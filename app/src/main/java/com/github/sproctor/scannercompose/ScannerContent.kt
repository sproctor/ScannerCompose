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

@Composable
fun ScannerContent(driverLicense: DriverLicense?, barcodeScanner: DCSScannerInfo?, sdkHandler: SDKHandler) {
    Box(modifier = Modifier.fillMaxSize()) {
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
    }
}