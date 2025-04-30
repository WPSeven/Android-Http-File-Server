package com.waiphyo.androidhttpfileserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.waiphyo.androidhttpfileserver.ui.theme.AndroidHttpFileServerTheme


import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.widget.ProgressBar
import android.widget.TextView

import java.io.File
import java.io.IOException
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        serverState = viewModel.serverState.collectAsState().value,
                        uploadState = viewModel.uploadState.collectAsState().value,
                        downloadState = viewModel.downloadState.collectAsState().value,
                        progress = viewModel.progress.collectAsState().value,
                        port = viewModel.port.collectAsState().value,
                        ipAddress = getWifiIpAddress(),
                        onPortChange = viewModel::updatePort,
                        onServerToggle = { isStarted ->
                            if (isStarted) viewModel.stopServer()
                            else viewModel.startServer(this)
                        },
                        onSelectFile = { startFilePicker() }
                    )
                }
            }
        }

        //setupHttpServer()
    }


    private fun startFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select File"),
            REQUEST_CODE_FILE_PICKER
        )
    }



    companion object {
        private const val REQUEST_CODE_FILE_PICKER = 1001
    }

    private fun getWifiIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return android.text.format.Formatter.formatIpAddress(info.ipAddress)
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1234
            )
        }
    }

    /*override fun onDestroy() {
        httpServer?.stop()
        super.onDestroy()
    }*/

    override fun onDestroy() {
        viewModel.stopServer()
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidHttpFileServerTheme {
        Greeting("Android")
    }
}