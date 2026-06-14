package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.domain.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NutritionSummaryViewModel(
    private val repository: ApiRepository
) : ViewModel() {
    private val _dailySummary = MutableStateFlow<DailySummaryResponse?>(null)
    val dailySummary: StateFlow<DailySummaryResponse?> = _dailySummary

    private val _weeklySummary = MutableStateFlow<WeeklySummaryResponse?>(null)
    val weeklySummary: StateFlow<WeeklySummaryResponse?> = _weeklySummary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchDailySummary() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getSummaryToday()) {
                is AppResult.Success -> _dailySummary.value = result.data
                is AppResult.Error -> {
                    _dailySummary.value = null
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun fetchWeeklySummary() {
        viewModelScope.launch {
            when (val result = repository.getSummaryWeek()) {
                is AppResult.Success -> _weeklySummary.value = result.data
                is AppResult.Error -> {
                    _weeklySummary.value = null
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun clearData() {
        _dailySummary.value = null
        _weeklySummary.value = null
        _isLoading.value = false
        _errorMessage.value = null
    }
}
