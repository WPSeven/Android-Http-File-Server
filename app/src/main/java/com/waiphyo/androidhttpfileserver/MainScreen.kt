package com.waiphyo.androidhttpfileserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    serverState: Boolean,
    uploadState: String,
    downloadState: String,
    progress: Int,
    port: Int,
    ipAddress: String,
    onPortChange: (Int) -> Unit,
    onServerToggle: (Boolean) -> Unit,
    onSelectFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))


        // Server Controls
        ServerControls(
            isStarted = serverState,
            port = port,
            ipAddress = ipAddress,
            onPortChange = onPortChange,
            onServerToggle = onServerToggle
        )


        // Upload Controls
        /*UploadControls(
            uploadState = uploadState,
            progress = progress,
            onSelectFile = onSelectFile
        )*/

        // Download Info
        if (downloadState.isNotEmpty()) {
            Text(
                text = downloadState,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Upload Info
        Text(
            text = uploadState,
            modifier = Modifier.fillMaxWidth()
        )

        // Progress Bar
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Download Info
        Text(
            text = downloadState,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(30.dp))


        // Server Info
        Text(
            text = buildString {
                append("HTTP Server Address: http://")
                append(ipAddress)
                append(":8080\n")
                append("OR\n")
                append("Upload file using curl command:\n")
                append("Using \"curl -F file=@myfile http://")
                append(ipAddress)
                append(":8080\" to upload file")
            }
        )
    }
}




@Composable
fun ServerControls(
    isStarted: Boolean,
    port: Int,
    ipAddress: String,
    onPortChange: (Int) -> Unit,
    onServerToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Server Address
        Text(
            text = "HTTP Server Address: $ipAddress:$port",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Port Input
            OutlinedTextField(
                value = port.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onPortChange(it) }
                },
                label = { Text("Port") },
                enabled = !isStarted,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Server Toggle Button
            Button(
                onClick = { onServerToggle(isStarted) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStarted)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isStarted) "Stop Server" else "Start Server")
            }
        }
    }
}

@Composable
fun UploadControls(
    uploadState: String,
    progress: Int,
    onSelectFile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select File to Upload")
        }

        Text(
            text = uploadState,
            style = MaterialTheme.typography.bodyMedium
        )

        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(
            ipAddress = "192.168.1.100",
            uploadState = "Uploading file test.txt...",
            downloadState = "Download complete!",
            progress = 75,
            port = 8080,
            serverState = true,
            onPortChange = {},
            onServerToggle = {},
            onSelectFile = {}
        )
    }
}