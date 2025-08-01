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
        // Using current free tier endpoints (no API key required)
        private const val JUPITER_TOKEN_API_BASE = "https://lite-api.jup.ag/token/v2/search"
        private const val JUPITER_PRICE_API_BASE = "https://lite-api.jup.ag/price/v2/price"
        
        // Tokenized stocks addresses
        val TOKENIZED_STOCKS = mapOf(
            "AMZN" to TokenizedStock("AMZNx", "Amazon xStock", "Xs3eBt7uRfJX8QUs4suhyU8p2M6DoUDrJyWBa8LLZsg"),
            "AAPL" to TokenizedStock("AAPLx", "Apple xStock", "XsbEhLAtcf6HdfpFZ5xEMdqW8nfAvcsP5bdudRLJzJp"),
            "GOOGL" to TokenizedStock("GOOGLx", "Alphabet xStock", "XsCPL9dNWBMvFtTmwcCA5v3xWPSMEBCszbQdiLLq6aN"),
            "META" to TokenizedStock("METAx", "Meta xStock", ""), // Address not provided, will be fetched via search
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
    data class TokenSearchResponse(
        val results: List<TokenSearchResult>? = null
    )
    
    @Serializable
    data class TokenSearchResult(
        val symbol: String? = null,
        val name: String? = null,
        val mint: String? = null,
        val verified: Boolean? = false
    )
    
    @Serializable
    data class PriceResponseV6(
        val data: Map<String, PriceDataV6>? = null,
        val timeTaken: Double? = null
    )
    
    @Serializable
    data class PriceDataV6(
        val id: String,
        val mintSymbol: String? = null,
        val vsToken: String? = null,
        val vsTokenSymbol: String? = null,
        val price: Double
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
            // Get all addresses (excluding META which needs to be searched)
            val knownAddresses = TOKENIZED_STOCKS.values
                .filter { it.address.isNotEmpty() }
                .map { it.address }
            
            // Fetch price data for known addresses
            val priceDataJob = async { fetchPriceData(knownAddresses) }
            
            // Search for META address if needed
            val metaSearchJob = async { searchTokenAddress("META") }
            
            val priceData = priceDataJob.await()
            val metaAddress = metaSearchJob.await()
            
            // If META was found, fetch its price data too
            val metaPriceData = if (metaAddress.isNotEmpty()) {
                fetchPriceData(listOf(metaAddress))
            } else {
                emptyMap()
            }
            
            // Combine all price data
            val allPriceData = priceData + metaPriceData
            
            // Build result list
            val results = mutableListOf<InvestmentTokenInfo>()
            
            TOKENIZED_STOCKS.forEach { (key, stock) ->
                val address = if (key == "META" && metaAddress.isNotEmpty()) metaAddress else stock.address
                
                if (address.isNotEmpty()) {
                    val priceInfo = allPriceData[address]
                    if (priceInfo != null) {
                        // Generate mock 24h change data for demo purposes
                        // In a real app, you'd get this from a separate API call
                        val mockChange24h = Random.nextDouble(-5.0, 5.0)
                        val mockChangePercent = (mockChange24h / priceInfo.price) * 100.0
                        
                        results.add(
                            InvestmentTokenInfo(
                                symbol = stock.symbol,
                                name = stock.name,
                                address = address,
                                price = priceInfo.price,
                                priceChange24h = mockChange24h,
                                priceChangePercentage24h = mockChangePercent
                            )
                        )
                    } else {
                        Log.w(TAG, "⚠️ No price data found for ${stock.symbol} (${address})")
                    }
                } else {
                    Log.w(TAG, "⚠️ No address found for ${stock.symbol}")
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
    
    private suspend fun searchTokenAddress(symbol: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Searching for $symbol address...")
            
            val url = URL("$JUPITER_TOKEN_API_BASE?query=$symbol")
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
                
                val searchResponse = Json { ignoreUnknownKeys = true }.decodeFromString<TokenSearchResponse>(response)
                
                // Look for verified META token
                val metaToken = searchResponse.results?.find { result ->
                    result.symbol?.equals("META", ignoreCase = true) == true &&
                    result.verified == true
                }
                
                metaToken?.mint ?: ""
            } else {
                Log.e(TAG, "❌ Search API returned code: $responseCode")
                ""
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error searching for $symbol: ${e.message}", e)
            ""
        }
    }
    
    private suspend fun fetchPriceData(addresses: List<String>): Map<String, PriceDataV6> = withContext(Dispatchers.IO) {
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
                
                val priceResponse = Json { ignoreUnknownKeys = true }.decodeFromString<PriceResponseV6>(response)
                priceResponse.data ?: emptyMap()
            } else {
                Log.e(TAG, "❌ Price API returned code: $responseCode")
                emptyMap()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching price data: ${e.message}", e)
            emptyMap()
        }
    }
}