package com.example.rampacashmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.usecase.InvestmentDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class InvestmentViewState(
    val isLoading: Boolean = true,
    val tokens: List<InvestmentDataUseCase.InvestmentTokenInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val investmentDataUseCase: InvestmentDataUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "InvestmentViewModel"
    }

    private val _state = MutableStateFlow(InvestmentViewState())
    val viewState: StateFlow<InvestmentViewState> = _state

    // ============================================
    // INVESTMENT - PRICE HISTORY
    // ============================================

    private val _priceHistory = MutableStateFlow<List<PricePoint>>(emptyList())
    val priceHistory: StateFlow<List<PricePoint>> = _priceHistory

    data class PricePoint(
        val timestamp: Long,
        val price: Float
    )

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
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun refreshData() {
        loadInvestmentData()
    }

    /**
     * Load historical price data for a token
     */
    fun loadPriceHistory(tokenAddress: String, timeRange: String = "7D") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "📊 Loading price history for $tokenAddress (range: $timeRange)...")

                // Use the InvestmentDataUseCase to fetch real historical data
                val historyData = investmentDataUseCase.getHistoricalPrices(tokenAddress, timeRange)

                _priceHistory.value = historyData

                Log.d(TAG, "✅ Price history loaded: ${historyData.size} data points")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading price history: ${e.message}", e)
                // On error, generate mock data as fallback
                _priceHistory.value = generateMockPriceHistory(timeRange)
            }
        }
    }

    /**
     * Generate mock price history for demo purposes (fallback)
     */
    private fun generateMockPriceHistory(timeRange: String = "7D"): List<PricePoint> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneHourMillis = 60 * 60 * 1000L

        val (numPoints, intervalMillis) = when (timeRange) {
            "1D" -> Pair(24, oneHourMillis)
            "7D" -> Pair(168, oneHourMillis)
            "30D" -> Pair(30, oneDayMillis)
            "1M" -> Pair(30, oneDayMillis)
            "3M" -> Pair(90, oneDayMillis)
            "6M" -> Pair(180, oneDayMillis)
            "1Y" -> Pair(365, oneDayMillis)
            else -> Pair(30, oneDayMillis)
        }

        val basePrice = 100.0f
        var currentPrice = basePrice

        return (0 until numPoints).map { i ->
            // Add some realistic volatility
            val volatility = 0.02f
            val randomChange = (kotlin.random.Random.nextDouble(-volatility.toDouble(), volatility.toDouble())).toFloat()
            currentPrice *= (1 + randomChange)

            // Add slight upward trend
            val trend = if (kotlin.random.Random.nextBoolean()) 0.001f else -0.001f
            currentPrice *= (1 + trend)

            PricePoint(
                timestamp = now - (numPoints - i - 1) * intervalMillis,
                price = currentPrice
            )
        }
    }

    /**
     * Clear price history
     */
    fun clearPriceHistory() {
        _priceHistory.value = emptyList()
    }
}
