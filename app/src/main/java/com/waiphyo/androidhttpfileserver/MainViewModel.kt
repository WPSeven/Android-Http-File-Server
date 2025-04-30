package com.waiphyo.androidhttpfileserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {


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




}