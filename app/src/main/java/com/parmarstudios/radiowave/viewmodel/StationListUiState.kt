package com.parmarstudios.radiowave.viewmodel

import com.parmarstudios.radiowave.data.RadioStation

sealed class StationListUiState {
    object Loading : StationListUiState()
    data class Success(val stations: List<RadioStation>) : StationListUiState()
    object Empty : StationListUiState()
    data class Error(val message: String) : StationListUiState()
}