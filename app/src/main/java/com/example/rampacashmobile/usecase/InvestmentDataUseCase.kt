package com.example.rampacashmobile.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class InvestmentDataUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "InvestmentDataUseCase"
        // Using current free tier endpoints (no API key required) - V3 format
        private const val JUPITER_TOKEN_API_BASE = "https://lite-api.jup.ag/tokens/v2/search"
        private const val JUPITER_PRICE_API_BASE = "https://lite-api.jup.ag/price/v3"
        
        // Tokenized stocks addresses
        val TOKENIZED_STOCKS = mapOf(
            "AMZN" to TokenizedStock("AMZNx", "Amazon xStock", "Xs3eBt7uRfJX8QUs4suhyU8p2M6DoUDrJyWBa8LLZsg"),
            "AAPL" to TokenizedStock("AAPLx", "Apple xStock", "XsbEhLAtcf6HdfpFZ5xEMdqW8nfAvcsP5bdudRLJzJp"),
            "GOOGL" to TokenizedStock("GOOGLx", "Alphabet xStock", "XsCPL9dNWBMvFtTmwcCA5v3xWPSMEBCszbQdiLLq6aN"),
            "META" to TokenizedStock("METAx", "Meta xStock", "Xsa62P5mvPszXL1krVUnU5ar38bBSVcWAB6fmPCo5Zu"),
            "NVDA" to TokenizedStock("NVDAx", "NVIDIA xStock", "Xsc9qvGR1efVDFGLrVsmkzv3qi45LTBjeUKSPmx9qEh"),
            "TSLA" to TokenizedStock("TSLAx", "Tesla xStock", "XsDoVfqeBukxuZHWhdvWHBhgEHjGNst4MLodqsJHzoB"),
            "SPY" to TokenizedStock("SPYx", "S&P 500 xStock", "XsoCS1TfEyfFhfvj8EtZ528L3CaKBDBRqRapnBbDF2W")
        )
    }
    
    data class TokenizedStock(
        val symbol: String,
        val name: String,
        val address: String
    )
    
    @Serializable
    data class TokenSearchResult(
        val id: String,
        val symbol: String? = null,
        val name: String? = null,
        val icon: String? = null,
        val decimals: Int? = null,
        val isVerified: Boolean? = false,
        val usdPrice: Double? = null,
        val stats24h: TokenStats24h? = null
    )
    
    @Serializable
    data class TokenStats24h(
        val priceChange: Double? = null
    )
    
    @Serializable
    data class PriceDataV3(
        val usdPrice: Double,
        val blockId: Long? = null,
        val decimals: Int? = null,
        val priceChange24h: Double? = null
    )
    
    data class InvestmentTokenInfo(
        val symbol: String,
        val name: String,
        val address: String,
        val price: Double,
        val priceChange24h: Double,
        val priceChangePercentage24h: Double
    )
    
    suspend fun getInvestmentTokensData(): List<InvestmentTokenInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Starting to fetch investment tokens data...")
            
            // Try to fetch real data first, fall back to mock data if APIs fail
            val realData = tryFetchRealData()
            
            if (realData.isNotEmpty()) {
                Log.d(TAG, "✅ Successfully fetched ${realData.size} tokens from APIs")
                return@withContext realData
            }
            
            // Fallback to mock data for demo purposes
            Log.w(TAG, "⚠️ APIs unavailable, using mock data for demo")
            return@withContext getMockInvestmentData()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching investment tokens data: ${e.message}", e)
            Log.w(TAG, "🔄 Falling back to mock data")
            return@withContext getMockInvestmentData()
        }
    }
    
    private suspend fun tryFetchRealData(): List<InvestmentTokenInfo> = withContext(Dispatchers.IO) {
        try {
            // Get all addresses - all are now known including META
            val allAddresses = TOKENIZED_STOCKS.values.map { it.address }
            
            // Fetch price data for all addresses in one batch call
            val allPriceData = fetchPriceData(allAddresses)
            
            // Build result list
            val results = mutableListOf<InvestmentTokenInfo>()
            
            TOKENIZED_STOCKS.forEach { (_, stock) ->
                val priceInfo = allPriceData[stock.address]
                if (priceInfo != null) {
                    // V3 API provides real 24h change data!
                    val priceChange24h = priceInfo.priceChange24h ?: 0.0
                    val priceChangePercent = if (priceInfo.usdPrice > 0) {
                        (priceChange24h / priceInfo.usdPrice) * 100.0
                    } else 0.0
                    
                    results.add(
                        InvestmentTokenInfo(
                            symbol = stock.symbol,
                            name = stock.name,
                            address = stock.address,
                            price = priceInfo.usdPrice,
                            priceChange24h = priceChange24h,
                            priceChangePercentage24h = priceChangePercent
                        )
                    )
                } else {
                    Log.w(TAG, "⚠️ No price data found for ${stock.symbol} (${stock.address})")
                }
            }
            
            results
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in tryFetchRealData: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun getMockInvestmentData(): List<InvestmentTokenInfo> {
        Log.d(TAG, "📊 Generating mock investment data for demo")
        
        // Realistic mock prices based on current stock market values (tokenized equivalents)
        val mockPrices = mapOf(
            "AMZN" to 185.50,
            "AAPL" to 228.75,
            "GOOGL" to 175.25,
            "META" to 542.81,
            "NVDA" to 303.65,
            "TSLA" to 248.98,
            "SPY" to 583.12
        )
        
        return TOKENIZED_STOCKS.map { (key, stock) ->
            val basePrice = mockPrices[key] ?: 100.0
            val priceVariation = Random.nextDouble(-0.05, 0.05) // ±5% variation
            val currentPrice = basePrice * (1.0 + priceVariation)
            
            val change24h = Random.nextDouble(-15.0, 15.0)
            val changePercent = (change24h / currentPrice) * 100.0
            
            InvestmentTokenInfo(
                symbol = stock.symbol,
                name = stock.name,
                address = stock.address.ifEmpty { "DemoAddr${key}1234567890" },
                price = currentPrice,
                priceChange24h = change24h,
                priceChangePercentage24h = changePercent
            )
        }
    }
    
    // Search function removed - all addresses are now hardcoded
    
    private suspend fun fetchPriceData(addresses: List<String>): Map<String, PriceDataV3> = withContext(Dispatchers.IO) {
        try {
            if (addresses.isEmpty()) return@withContext emptyMap()
            
            Log.d(TAG, "💰 Fetching price data for ${addresses.size} tokens...")
            
            val addressesParam = addresses.joinToString(",")
            val url = URL("$JUPITER_PRICE_API_BASE?ids=$addressesParam")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = connection.inputStream.use { 
                    BufferedReader(InputStreamReader(it)).readText() 
                }
                
                Log.d(TAG, "📊 Price API response: ${response.take(200)}...")
                
                // V3 API returns the data directly as a Map<String, PriceDataV3>
                Json { ignoreUnknownKeys = true }.decodeFromString<Map<String, PriceDataV3>>(response)
            } else {
                Log.e(TAG, "❌ Price API returned code: $responseCode")
                emptyMap()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching price data: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Fetch historical price data for a token
     * Returns a list of PricePoint objects with timestamp and price
     */
    suspend fun getHistoricalPrices(
        tokenAddress: String,
        timeRange: String = "7D"
    ): List<com.example.rampacashmobile.viewmodel.InvestmentViewModel.PricePoint> = withContext(Dispatchers.IO) {
        try {
            // Note: Jupiter API doesn't provide historical price data in the free tier
            // For now, we'll generate mock data based on current price
            // In a production app, you'd need to use a service like Birdeye, CoinGecko, etc.

            Log.d(TAG, "📈 Generating historical price data for $tokenAddress (range: $timeRange)")

            // First, get current price
            val currentPriceData = fetchPriceData(listOf(tokenAddress))
            val currentPrice = currentPriceData[tokenAddress]?.usdPrice?.toFloat() ?: 100f

            // Generate mock historical data based on current price
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

            var price = currentPrice * 0.95f // Start from 95% of current price

            (0 until numPoints).map { i ->
                // Add realistic volatility
                val volatility = 0.02f
                val randomChange = Random.nextDouble(-volatility.toDouble(), volatility.toDouble()).toFloat()
                price *= (1 + randomChange)

                // Add slight upward trend to reach current price
                val trend = 0.0005f
                price *= (1 + trend)

                com.example.rampacashmobile.viewmodel.InvestmentViewModel.PricePoint(
                    timestamp = now - (numPoints - i - 1) * intervalMillis,
                    price = price
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating historical prices: ${e.message}", e)
            emptyList()
        }
    }
}