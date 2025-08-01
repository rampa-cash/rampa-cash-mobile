package com.example.rampacashmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.usecase.InvestmentDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentViewState(
    val isLoading: Boolean = true,
    val tokens: List<InvestmentDataUseCase.InvestmentTokenInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val investmentDataUseCase: InvestmentDataUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(InvestmentViewState())
    val viewState: StateFlow<InvestmentViewState> = _state
    
    init {
        loadInvestmentData()
    }
    
    fun loadInvestmentData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val tokens = investmentDataUseCase.getInvestmentTokensData()
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        tokens = tokens,
                        error = null
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load investment data: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun refreshData() {
        loadInvestmentData()
    }
}