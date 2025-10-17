package com.github.sproctor.scannercompose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.SDKHandler

@Composable
fun PairingDialog(sdkHandler: SDKHandler, hideDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = hideDialog,
        confirmButton = {
            Button(onClick = hideDialog) {
                Text("Cancel")
            }
        },
        text = {
            BoxWithConstraints(modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)) {
                val density = LocalDensity.current
                val width = maxWidth
                val height = min(maxWidth / 3, maxHeight)
                AndroidView(
                    modifier = Modifier
                        .size(width = width, height = height)
                        .align(Alignment.Center),
                    factory = {
                        sdkHandler.dcssdkGetPairingBarcode(
                            DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE,
                            DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT
                        )
                            .also {
                                with(density) { it.setSize(width.toPx().toInt(), height.toPx().toInt()) }
                            }
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PairingDialog(SDKHandler(LocalContext.current, true)) {}
}
