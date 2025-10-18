package com.example.rampacashmobile.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Base ViewModel class that provides common functionality for all ViewModels
 * Includes error management and other shared patterns
 */
abstract class BaseViewModel : ViewModel() {
    
    /**
     * Clear error state - to be implemented by subclasses
     * Each ViewModel should override this to clear its specific error state
     */
    abstract fun clearError()
    
    /**
     * Common error clearing pattern for ViewModels with error state
     * This can be used by ViewModels that have a standard error field
     */
    protected fun <T> clearErrorInState(
        stateFlow: MutableStateFlow<T>,
        errorField: (T) -> T
    ) {
        stateFlow.update { currentState ->
            errorField(currentState)
        }
    }
    
    /**
     * Log error clearing for debugging
     */
    protected fun logErrorClearing(viewModelName: String) {
        Timber.d("$viewModelName", "clearError() called")
    }
}
