package com.streamplayer.tv.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamplayer.tv.data.ChannelRepository
import com.streamplayer.tv.model.Channel
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val channels: List<Channel>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        loadChannels()
    }

    fun loadChannels() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            val result = ChannelRepository.fetchChannels()
            result.fold(
                onSuccess = { _uiState.value = HomeUiState.Success(it) },
                onFailure = { _uiState.value = HomeUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }
}
