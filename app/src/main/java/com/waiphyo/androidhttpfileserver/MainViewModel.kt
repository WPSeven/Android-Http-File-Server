package com.waiphyo.androidhttpfileserver

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private var httpServer: HttpServer? = null


    private val _serverState = MutableStateFlow(false)
    val serverState = _serverState.asStateFlow()

    private val _uploadState = MutableStateFlow("Waiting for upload file...")
    val uploadState = _uploadState.asStateFlow()

    private val _downloadState = MutableStateFlow("")
    val downloadState = _downloadState.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _port = MutableStateFlow(8080)
    val port = _port.asStateFlow()

    fun updateServerState(isStarted: Boolean) {
        viewModelScope.launch {
            _serverState.value = isStarted
        }
    }

    fun updatePort(port: Int) {
        viewModelScope.launch {
            _port.value = port
        }
    }

    fun updateUploadState(state: String) {
        viewModelScope.launch {
            _uploadState.value = state
        }
    }

    fun updateDownloadState(state: String) {
        viewModelScope.launch {
            _downloadState.value = state
        }
    }

    fun updateProgress(progress: Int) {
        viewModelScope.launch {
            _progress.value = progress
        }
    }




    fun startServer(context: Context) {
        try {
            httpServer = HttpServer(context, port.value).apply {
                setOnStatusUpdateListener(object : HttpServer.OnStatusUpdateListener {
                    override fun onUploadingProgressUpdate(progress: Int) {
                        updateProgress(progress)
                    }

                    override fun onUploadingFile(file: File, done: Boolean) {
                        updateUploadState(
                            if (done) "Upload file ${file.name} done!"
                            else "Uploading file ${file.name}..."
                        )
                        if (!done) updateProgress(0)
                    }

                    override fun onDownloadingFile(file: File, done: Boolean) {
                        updateDownloadState(
                            if (done) "Download file ${file.name} done!"
                            else "Downloading file ${file.name}..."
                        )
                    }
                })
                start()
            }
            updateServerState(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopServer() {
        httpServer?.stop()
        httpServer = null
        updateServerState(false)
    }

    override fun onCleared() {
        stopServer()
        super.onCleared()
    }

}