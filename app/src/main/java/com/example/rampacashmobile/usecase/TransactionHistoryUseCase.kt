package com.example.rampacashmobile.usecase

import android.util.Log
import com.example.rampacashmobile.R
import com.example.rampacashmobile.ui.screens.Transaction
import com.example.rampacashmobile.ui.screens.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionHistoryUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "TransactionHistoryUseCase"
        private const val RPC_URL = "https://api.devnet.solana.com"
    }
    
    suspend fun getTransactionHistory(walletAddress: String): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching transaction history for: ${walletAddress.take(8)}...")
            Log.d(TAG, "🔍 Full wallet address: $walletAddress")
            
            // Get confirmed signatures for the address
            val signatures = getConfirmedSignatures(walletAddress)
            Log.d(TAG, "📝 Found ${signatures.size} signatures")
            
            // Special debugging for the problematic transaction
            val problematicTxSignature = "5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh"
            if (signatures.contains(problematicTxSignature)) {
                Log.d(TAG, "🔍 Found problematic transaction in signatures!")
            } else {
                Log.d(TAG, "⚠️ Problematic transaction NOT found in signatures for $walletAddress")
                // Try to fetch this specific transaction directly
                try {
                    val specificTx = getTransactionDetails(problematicTxSignature, walletAddress)
                    if (specificTx != null) {
                        Log.d(TAG, "🔍 Successfully parsed problematic transaction directly: ${specificTx.amount} ${specificTx.tokenSymbol} ${specificTx.transactionType}")
                    } else {
                        Log.d(TAG, "⚠️ Could not parse problematic transaction directly")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "⚠️ Error fetching problematic transaction: ${e.message}")
                }
            }
            
            // Get transaction details for each signature
            val transactions = mutableListOf<Transaction>()
            
            // If the problematic transaction isn't in signatures, add it manually for testing
            val allSignaturesToProcess = if (!signatures.contains(problematicTxSignature)) {
                Log.d(TAG, "🔧 Adding problematic transaction manually for testing")
                (signatures.take(19) + problematicTxSignature).toMutableList()
            } else {
                signatures.take(20).toMutableList()
            }
            
            allSignaturesToProcess.forEach { signature -> // Process up to 20 most recent
                try {
                    if (signature.startsWith("5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh")) {
                        Log.d(TAG, "🔍 Processing target transaction: ${signature.take(8)}...")
                    }
                    val transaction = getTransactionDetails(signature, walletAddress)
                    if (transaction != null) {
                        transactions.add(transaction)
                        if (signature.startsWith("5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh")) {
                            Log.d(TAG, "🔍 Target transaction parsed: ${transaction.amount} ${transaction.tokenSymbol} ${transaction.transactionType}")
                        }
                    } else {
                        if (signature.startsWith("5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh")) {
                            Log.d(TAG, "🔍 Target transaction returned NULL from parsing")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Failed to get details for signature $signature: ${e.message}")
                    if (signature.startsWith("5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh")) {
                        Log.w(TAG, "🔍 Target transaction failed to parse: ${e.message}")
                    }
                }
            }
            
            Log.d(TAG, "✅ Successfully parsed ${transactions.size} transactions")
            transactions.sortedByDescending { it.date.time }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch transaction history: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun getConfirmedSignatures(address: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "getSignaturesForAddress")
                put("params", JSONArray().apply {
                    put(address)
                    put(JSONObject().apply {
                        put("limit", 50) // Increased to catch more transactions
                    })
                })
            }
            
            val response = makeRpcCall(requestBody.toString())
            val jsonResponse = JSONObject(response)
            
            if (jsonResponse.has("error")) {
                Log.e(TAG, "RPC Error: ${jsonResponse.getJSONObject("error")}")
                return@withContext emptyList()
            }
            
            val result = jsonResponse.getJSONArray("result")
            val signatures = mutableListOf<String>()
            
            for (i in 0 until result.length()) {
                val signatureInfo = result.getJSONObject(i)
                val signature = signatureInfo.getString("signature")
                signatures.add(signature)
                
                // Log specific transaction if found
                if (signature.startsWith("5GoXQqV3T59LAh3FaCd7JSh7LPoyoy3BiDMkeWR3AVwaQsPJjvcsqFvcr5WCNn5EjvNXxbGmvQdnYY2u3ySHNbFh")) {
                    Log.d(TAG, "🔍 FOUND target transaction in signatures at position $i")
                }
            }
            
            // Log first few signatures for debugging
            Log.d(TAG, "📝 First 5 signatures:")
            signatures.take(5).forEachIndexed { index, sig ->
                Log.d(TAG, "  $index: ${sig.take(8)}...")
            }
            
            signatures
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get signatures: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun getTransactionDetails(signature: String, walletAddress: String): Transaction? = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "getTransaction")
                put("params", JSONArray().apply {
                    put(signature)
                    put(JSONObject().apply {
                        put("encoding", "json")
                        put("maxSupportedTransactionVersion", 0)
                        put("commitment", "confirmed")
                    })
                })
            }
            
            val response = makeRpcCall(requestBody.toString())
            val jsonResponse = JSONObject(response)
            
            if (jsonResponse.has("error")) {
                Log.w(TAG, "RPC Error for transaction $signature: ${jsonResponse.getJSONObject("error")}")
                return@withContext null
            }
            
            val result = jsonResponse.getJSONObject("result")
            if (result.isNull("transaction")) {
                return@withContext null
            }
            
            val transaction = result.getJSONObject("transaction")
            val message = transaction.getJSONObject("message")
            val accountKeys = message.getJSONArray("accountKeys")
            val instructions = message.getJSONArray("instructions")
            
            // Check for token balance changes in metadata
            val meta = if (result.has("meta") && !result.isNull("meta")) {
                result.getJSONObject("meta")
            } else null
            
            var detectedTokenFromMeta: String? = null
            var balanceChangeDirection: String? = null
            var actualTransactionAmount: Double? = null
            
            meta?.let { metadata ->
                if (metadata.has("postTokenBalances") && !metadata.isNull("postTokenBalances") &&
                    metadata.has("preTokenBalances") && !metadata.isNull("preTokenBalances")) {
                    
                    val preTokenBalances = metadata.getJSONArray("preTokenBalances")
                    val postTokenBalances = metadata.getJSONArray("postTokenBalances")
                    
                    // Track all balance changes for this wallet
                    var totalAmountChange = 0.0
                    var primaryDirection: String? = null
                    var primaryTokenType: String? = null
                    
                    // Find balance changes for our wallet
                    for (i in 0 until postTokenBalances.length()) {
                        val postBalance = postTokenBalances.getJSONObject(i)
                        if (postBalance.has("mint") && postBalance.has("owner")) {
                            val mint = postBalance.getString("mint")
                            val owner = postBalance.getString("owner")
                            
                            // Check if this balance belongs to our wallet
                            if (owner == walletAddress) {
                                val currentTokenType = when (mint) {
                                    "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU" -> {
                                        Log.d(TAG, "💰 Found USDC balance change")
                                        "USDC"
                                    }
                                    "HzwqbKZw8HxMN6bF2yFZNrht3c2iXXzpKcFu7uBEDKtr" -> {
                                        Log.d(TAG, "💰 Found EURC balance change")
                                        "EURC"
                                    }
                                    else -> null
                                }
                                
                                currentTokenType?.let { tokenType ->
                                    // Compare pre and post balances to determine direction
                                    try {
                                        val uiTokenAmount = postBalance.getJSONObject("uiTokenAmount")
                                        val postAmount = if (uiTokenAmount.isNull("uiAmount")) {
                                            Log.d(TAG, "⚠️ Post uiAmount is null, trying amount...")
                                            if (uiTokenAmount.has("amount")) {
                                                val rawAmount = uiTokenAmount.getString("amount").toDoubleOrNull() ?: 0.0
                                                val decimals = uiTokenAmount.optInt("decimals", 6)
                                                rawAmount / Math.pow(10.0, decimals.toDouble())
                                            } else {
                                                0.0
                                            }
                                        } else {
                                            uiTokenAmount.getDouble("uiAmount")
                                        }
                                        
                                        // Find corresponding pre balance
                                        for (j in 0 until preTokenBalances.length()) {
                                            val preBalance = preTokenBalances.getJSONObject(j)
                                            if (preBalance.has("owner") && preBalance.getString("owner") == owner &&
                                                preBalance.has("mint") && preBalance.getString("mint") == mint) {
                                                
                                                val preUiTokenAmount = preBalance.getJSONObject("uiTokenAmount")
                                                val preAmount = if (preUiTokenAmount.isNull("uiAmount")) {
                                                    Log.d(TAG, "⚠️ Pre uiAmount is null, trying amount...")
                                                    if (preUiTokenAmount.has("amount")) {
                                                        val rawAmount = preUiTokenAmount.getString("amount").toDoubleOrNull() ?: 0.0
                                                        val decimals = preUiTokenAmount.optInt("decimals", 6)
                                                        rawAmount / Math.pow(10.0, decimals.toDouble())
                                                    } else {
                                                        0.0
                                                    }
                                                } else {
                                                    preUiTokenAmount.getDouble("uiAmount")
                                                }
                                                val amountChange = Math.abs(postAmount - preAmount)
                                                
                                                if (amountChange > 0.0001) { // Significant change
                                                    val direction = if (postAmount > preAmount) "RECEIVED" else "SENT"
                                                    
                                                    Log.d(TAG, "📊 $tokenType balance change: $preAmount -> $postAmount ($direction) Amount: $amountChange")
                                                    
                                                    // Special logging for the problematic address
                                                    if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                                                        Log.d(TAG, "🔍 $tokenType $direction: $preAmount -> $postAmount, change: $amountChange, mint: $mint")
                                                    }
                                                    
                                                    // Use the largest amount change as the primary transaction
                                                    if (amountChange > totalAmountChange) {
                                                        totalAmountChange = amountChange
                                                        primaryDirection = direction
                                                        primaryTokenType = tokenType
                                                        
                                                        if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                                                            Log.d(TAG, "🔍 Setting as PRIMARY: $tokenType $direction $amountChange")
                                                        }
                                                    }
                                                }
                                                break
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Failed to parse balance amounts: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                    
                    // Set the final values based on the primary (largest) change
                    if (primaryTokenType != null && primaryDirection != null && totalAmountChange > 0) {
                        detectedTokenFromMeta = primaryTokenType
                        balanceChangeDirection = primaryDirection  
                        actualTransactionAmount = totalAmountChange
                        
                        Log.d(TAG, "✅ Final transaction: $primaryTokenType $primaryDirection $totalAmountChange")
                        
                        if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                            Log.d(TAG, "🔍 FINAL RESULT: $primaryTokenType $primaryDirection $totalAmountChange")
                        }
                    }
                } else {
                    // Fallback to simple detection without balance comparison
                    val postTokenBalances = metadata.getJSONArray("postTokenBalances")
                    for (i in 0 until postTokenBalances.length()) {
                        val balance = postTokenBalances.getJSONObject(i)
                        if (balance.has("mint")) {
                            val mint = balance.getString("mint")
                            when (mint) {
                                "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU" -> {
                                    detectedTokenFromMeta = "USDC"
                                    Log.d(TAG, "💰 Detected USDC from token balance metadata!")
                                }
                                "HzwqbKZw8HxMN6bF2yFZNrht3c2iXXzpKcFu7uBEDKtr" -> {
                                    detectedTokenFromMeta = "EURC"
                                    Log.d(TAG, "💰 Detected EURC from token balance metadata!")
                                }
                            }
                        }
                    }
                }
                
                // Also check for SOL balance changes
                if (metadata.has("preBalances") && !metadata.isNull("preBalances") &&
                    metadata.has("postBalances") && !metadata.isNull("postBalances") &&
                    metadata.has("accountKeys") && !metadata.isNull("accountKeys")) {
                    
                    val preBalances = metadata.getJSONArray("preBalances")
                    val postBalances = metadata.getJSONArray("postBalances")
                    val metaAccountKeys = metadata.getJSONArray("accountKeys")
                    
                    // Find our wallet in the account keys
                    for (i in 0 until metaAccountKeys.length()) {
                        if (metaAccountKeys.getString(i) == walletAddress) {
                            if (i < preBalances.length() && i < postBalances.length()) {
                                val preBalance = preBalances.getLong(i) / 1_000_000_000.0 // Convert lamports to SOL
                                val postBalance = postBalances.getLong(i) / 1_000_000_000.0
                                
                                if (Math.abs(preBalance - postBalance) > 0.0001) { // Significant SOL change
                                    detectedTokenFromMeta = "SOL"
                                    if (postBalance > preBalance) {
                                        balanceChangeDirection = "RECEIVED"
                                        actualTransactionAmount = postBalance - preBalance
                                    } else {
                                        balanceChangeDirection = "SENT"
                                        actualTransactionAmount = preBalance - postBalance
                                    }
                                    Log.d(TAG, "💰 Detected SOL transfer: $preBalance -> $postBalance (Amount: $actualTransactionAmount)")
                                    break
                                }
                            }
                            break
                        }
                    }
                }
            }
            
            // Debug logging (reduced verbosity)
            if (detectedTokenFromMeta != null) {
                Log.d(TAG, "📊 Transaction ${signature.take(8)} - Token: $detectedTokenFromMeta, Amount: $actualTransactionAmount, Direction: $balanceChangeDirection")
            }
            
            // Special debugging for the specific address mentioned by user
            if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                Log.d(TAG, "🔍 DEBUG for address DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG:")
                Log.d(TAG, "🔍 - Signature: $signature")
                Log.d(TAG, "🔍 - DetectedToken: $detectedTokenFromMeta")
                Log.d(TAG, "🔍 - ActualAmount: $actualTransactionAmount") 
                Log.d(TAG, "🔍 - Direction: $balanceChangeDirection")
                
                // Log all token balance changes for this address
                meta?.let { metadata ->
                    if (metadata.has("preTokenBalances") && metadata.has("postTokenBalances")) {
                        val preTokenBalances = metadata.getJSONArray("preTokenBalances")
                        val postTokenBalances = metadata.getJSONArray("postTokenBalances")
                        
                        Log.d(TAG, "🔍 - PreTokenBalances count: ${preTokenBalances.length()}")
                        Log.d(TAG, "🔍 - PostTokenBalances count: ${postTokenBalances.length()}")
                        
                        for (i in 0 until postTokenBalances.length()) {
                            val postBalance = postTokenBalances.getJSONObject(i)
                            if (postBalance.has("owner") && postBalance.getString("owner") == walletAddress) {
                                val mint = if (postBalance.has("mint")) postBalance.getString("mint") else "unknown"
                                val postAmount = if (postBalance.has("uiTokenAmount")) 
                                    postBalance.getJSONObject("uiTokenAmount").getDouble("uiAmount") else 0.0
                                
                                // Find corresponding pre balance
                                var preAmount = 0.0
                                for (j in 0 until preTokenBalances.length()) {
                                    val preBalance = preTokenBalances.getJSONObject(j)
                                    if (preBalance.has("owner") && preBalance.getString("owner") == walletAddress &&
                                        preBalance.has("mint") && preBalance.getString("mint") == mint) {
                                        preAmount = preBalance.getJSONObject("uiTokenAmount").getDouble("uiAmount")
                                        break
                                    }
                                }
                                
                                val change = Math.abs(postAmount - preAmount)
                                Log.d(TAG, "🔍 - Token $mint: $preAmount -> $postAmount (change: $change)")
                            }
                        }
                    }
                }
            }
            
            // Parse transaction details
            parseTransaction(signature, accountKeys, instructions, walletAddress, result, detectedTokenFromMeta, balanceChangeDirection, actualTransactionAmount)
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse transaction $signature: ${e.message}")
            null
        }
    }
    
    private fun parseTransaction(
        signature: String, 
        accountKeys: JSONArray, 
        instructions: JSONArray, 
        walletAddress: String,
        transactionResult: JSONObject,
        detectedTokenFromMeta: String? = null,
        balanceChangeDirection: String? = null,
        actualTransactionAmount: Double? = null
    ): Transaction? {
        try {
            // Get block time for date
            val blockTime = if (transactionResult.has("blockTime") && !transactionResult.isNull("blockTime")) {
                transactionResult.getLong("blockTime") * 1000 // Convert to milliseconds
            } else {
                System.currentTimeMillis() // Fallback to current time
            }
            val date = Date(blockTime)
            
            // Find wallet address index (check both main wallet and potential involvement)
            var walletIndex = -1
            for (i in 0 until accountKeys.length()) {
                if (accountKeys.getString(i) == walletAddress) {
                    walletIndex = i
                    break
                }
            }
            
            // If wallet not found directly but we have metadata indicating involvement, continue parsing
            if (walletIndex == -1) {
                if (detectedTokenFromMeta != null && actualTransactionAmount != null && actualTransactionAmount > 0) {
                    Log.d(TAG, "🔍 Wallet not in accounts but metadata shows involvement - continuing with metadata-only parsing")
                    // We'll use metadata-only parsing for this transaction
                } else {
                    Log.w(TAG, "Wallet address not found in transaction accounts and no metadata - skipping")
                    return null
                }
            }
            
            // Look through ALL instructions to find token transfers (prioritize SPL over SOL)
            var foundSolTransfer: (() -> Transaction?)? = null
            
            for (i in 0 until instructions.length()) {
                val instruction = instructions.getJSONObject(i)
                val programIdIndex = instruction.getInt("programIdIndex")
                val programId = accountKeys.getString(programIdIndex)
                val accounts = instruction.getJSONArray("accounts")
                val instructionData = if (instruction.has("data")) instruction.getString("data") else ""
                
                // Check if this is a SOL transfer (system program) or SPL token transfer
                when (programId) {
                    "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" -> {
                        // SPL Token Program - PRIORITIZE this over SOL transfers
                        return parseSplTokenTransfer(signature, accountKeys, accounts, walletIndex, walletAddress, date, accountKeys, detectedTokenFromMeta, balanceChangeDirection, instructionData, actualTransactionAmount)
                    }
                    "11111111111111111111111111111112" -> {
                        // System Program - Store this but don't return yet (might be rent/fees for token transfer)
                        Log.d(TAG, "📍 Found SOL transfer instruction (storing as backup)")
                        if (foundSolTransfer == null) {
                            foundSolTransfer = { parseSolTransfer(signature, accountKeys, accounts, walletIndex, walletAddress, date, instructionData, actualTransactionAmount) }
                        }
                    }
                    "ComputeBudget111111111111111111111111111111" -> {
                        // Compute Budget Program - skip this, it's just for fees
                        Log.d(TAG, "⚡ Skipping ComputeBudget instruction")
                        continue
                    }
                    else -> {
                        Log.d(TAG, "❓ Unknown program: $programId")
                        continue
                    }
                }
            }
            
            // If we found a SOL transfer but no SPL token transfer, use the SOL transfer
            foundSolTransfer?.let { 
                Log.d(TAG, "📍 Using stored SOL transfer (no SPL found)")
                return it() 
            }
            
            // Fallback - check if this might be a token transaction based on account keys
            Log.d(TAG, "🔍 No token program found, checking for mint addresses in fallback...")
            
            // Check if any known mint addresses are present
            for (i in 0 until accountKeys.length()) {
                val address = accountKeys.getString(i)
                when (address) {
                    "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU" -> {
                        Log.d(TAG, "💰 Found USDC mint in fallback")
                        return Transaction(
                            id = signature.take(8),
                            recipient = "Unknown",
                            sender = "Unknown",
                            amount = 1.0,
                            date = date,
                            description = "USDC Transaction", 
                            currency = "USDC",
                            transactionType = TransactionType.SENT,
                            tokenSymbol = "USDC",
                            tokenIcon = R.drawable.usdc_logo,
                            tokenName = "USD Coin"
                        )
                    }
                    "HzwqbKZw8HxMN6bF2yFZNrht3c2iXXzpKcFu7uBEDKtr" -> {
                        Log.d(TAG, "💰 Found EURC mint in fallback")
                        return Transaction(
                            id = signature.take(8),
                            recipient = "Unknown",
                            sender = "Unknown",
                            amount = 1.0,
                            date = date,
                            description = "EURC Transaction",
                            currency = "EURC", 
                            transactionType = TransactionType.SENT,
                            tokenSymbol = "EURC",
                            tokenIcon = R.drawable.eurc_logo,
                            tokenName = "Euro Coin"
                        )
                    }
                }
            }
            
            // Final fallback - generic SOL transaction
            Log.d(TAG, "💰 Creating fallback SOL transaction")
            return Transaction(
                id = signature.take(8),
                recipient = "Unknown",
                sender = "Unknown", 
                amount = 0.01,
                date = date,
                description = "SOL Transaction",
                currency = "SOL",
                transactionType = TransactionType.SENT,
                tokenSymbol = "SOL",
                tokenIcon = R.drawable.solana_logo,
                tokenName = "Solana"
            )
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse transaction details: ${e.message}")
            return null
        }
    }
    
    private fun parseSolTransfer(
        signature: String,
        accountKeys: JSONArray,
        accounts: JSONArray,
        walletIndex: Int,
        walletAddress: String,
        date: Date,
        instructionData: String = "",
        actualTransactionAmount: Double? = null
    ): Transaction? {
        try {
            // For SOL transfers: accounts[0] = from, accounts[1] = to
            if (accounts.length() >= 2) {
                val fromIndex = accounts.getInt(0)
                val toIndex = accounts.getInt(1)
                
                val fromAddress = accountKeys.getString(fromIndex)
                val toAddress = accountKeys.getString(toIndex)
                
                val transactionType = if (fromAddress == walletAddress) {
                    TransactionType.SENT
                } else {
                    TransactionType.RECEIVED
                }
                
                // Use actual amount from metadata if available, otherwise try parsing instruction data
                val amount = actualTransactionAmount?.also {
                    Log.d(TAG, "✅ Using actual SOL amount from metadata: $it SOL")
                } ?: run {
                    val parsedAmount = parseAmountFromInstructionData(instructionData, false)
                    val fallbackAmount = if (parsedAmount > 0.0) parsedAmount else 0.001
                    Log.d(TAG, "⚠️ Using ${if (parsedAmount > 0.0) "parsed" else "fallback"} SOL amount: $fallbackAmount SOL")
                    fallbackAmount
                }
                
                return Transaction(
                    id = signature.take(8),
                    recipient = toAddress,
                    sender = fromAddress,
                    amount = amount,
                    date = date,
                    description = if (transactionType == TransactionType.SENT) "Sent SOL" else "Received SOL",
                    currency = "SOL",
                    transactionType = transactionType,
                    tokenSymbol = "SOL",
                    tokenIcon = R.drawable.solana_logo,
                    tokenName = "Solana"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse SOL transfer: ${e.message}")
        }
        return null
    }
    
    private fun parseSplTokenTransfer(
        signature: String,
        accountKeys: JSONArray,
        accounts: JSONArray,
        walletIndex: Int,
        walletAddress: String,
        date: Date,
        allAccountKeys: JSONArray,
        detectedTokenFromMeta: String? = null,
        balanceChangeDirection: String? = null,
        instructionData: String = "",
        actualTransactionAmount: Double? = null
    ): Transaction? {
                        try {
            
            // Look for token type - prioritize metadata detection
            var mintAddress: String? = null
            var tokenSymbol = "TOKEN"
            var tokenIcon = R.drawable.usdc_logo
            var tokenName = "Token"
            
            // PRIORITY 1: Use metadata detection if available
            when (detectedTokenFromMeta) {
                "USDC" -> {
                    tokenSymbol = "USDC"
                    tokenIcon = R.drawable.usdc_logo
                    tokenName = "USD Coin"
                    Log.d(TAG, "✅ Using USDC from metadata")
                }
                "EURC" -> {
                    tokenSymbol = "EURC"
                    tokenIcon = R.drawable.eurc_logo
                    tokenName = "Euro Coin"
                    Log.d(TAG, "✅ Using EURC from metadata")
                }
                else -> {
                    // PRIORITY 2: Check account keys for mint addresses as fallback
                    Log.d(TAG, "🔍 No metadata token detected, checking account keys...")
                    for (i in 0 until allAccountKeys.length()) {
                        val address = allAccountKeys.getString(i)
                        when (address) {
                            "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU" -> {
                                mintAddress = address
                                tokenSymbol = "USDC"
                                tokenIcon = R.drawable.usdc_logo
                                tokenName = "USD Coin"
                                Log.d(TAG, "💰 Detected USDC mint address (fallback)")
                                break
                            }
                            "HzwqbKZw8HxMN6bF2yFZNrht3c2iXXzpKcFu7uBEDKtr" -> {
                                mintAddress = address
                                tokenSymbol = "EURC"
                                tokenIcon = R.drawable.eurc_logo
                                tokenName = "Euro Coin" 
                                Log.d(TAG, "💰 Detected EURC mint address (fallback)")
                                break
                            }
                        }
                    }
                    
                    // If still TOKEN, log for debugging
                    if (tokenSymbol == "TOKEN") {
                        Log.w(TAG, "⚠️ Could not determine token type, defaulting to TOKEN")
                        for (i in 0 until allAccountKeys.length()) {
                            Log.d(TAG, "🔍 Account key $i: ${allAccountKeys.getString(i)}")
                        }
                    }
                }
            }
            
            // Determine transaction direction based on wallet involvement
            var transactionType = TransactionType.RECEIVED // Default to received
            var fromAddress = "Unknown"
            var toAddress = "Unknown"
            
            // First, try to extract addresses from instruction accounts if available
            if (accounts.length() >= 2) {
                val sourceIndex = accounts.getInt(0)
                val destIndex = accounts.getInt(1)
                
                if (sourceIndex < allAccountKeys.length()) {
                    fromAddress = allAccountKeys.getString(sourceIndex)
                }
                if (destIndex < allAccountKeys.length()) {
                    toAddress = allAccountKeys.getString(destIndex)
                }
                Log.d(TAG, "🔍 Extracted addresses - From: ${fromAddress.take(8)}..., To: ${toAddress.take(8)}...")
            }
            
            // PRIORITY 1: Use balance change direction if available (most reliable)
            if (!balanceChangeDirection.isNullOrEmpty()) {
                transactionType = when (balanceChangeDirection) {
                    "SENT" -> TransactionType.SENT
                    "RECEIVED" -> TransactionType.RECEIVED
                    else -> TransactionType.RECEIVED
                }
                Log.d(TAG, "✅ Using balance change direction: $balanceChangeDirection")
            } else if (actualTransactionAmount != null && actualTransactionAmount > 0) {
                // If we have an actual amount from metadata but no direction, try to infer
                // This is a fallback when metadata parsing partially worked
                transactionType = TransactionType.RECEIVED // Default to received when we have amount but unclear direction
                Log.d(TAG, "🔍 Have amount ($actualTransactionAmount) but no direction, defaulting to RECEIVED")
            } else {
                // PRIORITY 3: Try to determine from instruction accounts if available
                if (accounts.length() >= 2) {
                    // Check if wallet is directly involved in instruction accounts
                    var walletFoundIndex = -1
                    for (i in 0 until allAccountKeys.length()) {
                        if (allAccountKeys.getString(i) == walletAddress) {
                            walletFoundIndex = i
                            break
                        }
                    }
                    
                    if (walletFoundIndex != -1) {
                        // Check if wallet appears in the instruction accounts (indicates involvement)
                        var walletInvolvedAsIndex = -1
                        for (i in 0 until accounts.length()) {
                            if (accounts.getInt(i) == walletFoundIndex) {
                                walletInvolvedAsIndex = i
                                break
                            }
                        }
                        
                        // For SPL token transfers, be more conservative about direction detection
                        // Only mark as SENT if we're very confident (position 0 = source authority)
                        if (walletInvolvedAsIndex == 0) {
                            transactionType = TransactionType.SENT
                            Log.d(TAG, "🔍 Wallet found as source authority at position $walletInvolvedAsIndex -> SENT")
                        } else {
                            // Default to RECEIVED for positions 1, 2, or other positions
                            transactionType = TransactionType.RECEIVED
                            Log.d(TAG, "🔍 Wallet found at position $walletInvolvedAsIndex -> RECEIVED (conservative)")
                        }
                    } else {
                        // Wallet not found in accounts - use default RECEIVED
                        transactionType = TransactionType.RECEIVED
                        Log.d(TAG, "🔍 Wallet not found in accounts -> RECEIVED (fallback)")
                    }
                }
            }
            
            // Fix addresses based on transaction type and wallet involvement
            if (transactionType == TransactionType.RECEIVED) {
                // For received transactions, the wallet is the recipient
                toAddress = walletAddress
                // If we still don't have a clear sender, use a placeholder
                if (fromAddress == "Unknown") {
                    fromAddress = "External Sender"
                }
            } else if (transactionType == TransactionType.SENT) {
                // For sent transactions, the wallet is the sender
                fromAddress = walletAddress
                // If we still don't have a clear recipient, use a placeholder
                if (toAddress == "Unknown") {
                    toAddress = "External Recipient"
                }
            }
            
            Log.d(TAG, "🔍 Final addresses - From: ${fromAddress.take(8)}..., To: ${toAddress.take(8)}...")
            
            Log.d(TAG, "✅ $tokenSymbol ${transactionType.name}")
            
            // Use actual amount from metadata if available, otherwise try parsing instruction data
            val amount = actualTransactionAmount?.also {
                Log.d(TAG, "✅ Using actual amount from metadata: $it $tokenSymbol")
                if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                    Log.d(TAG, "🔍 DEBUG: Using metadata amount $it for $tokenSymbol")
                }
            } ?: run {
                val parsedAmount = parseAmountFromInstructionData(instructionData, true)
                val fallbackAmount = if (parsedAmount > 0.0) parsedAmount else 1.0
                Log.d(TAG, "⚠️ Using ${if (parsedAmount > 0.0) "parsed" else "fallback"} amount: $fallbackAmount $tokenSymbol")
                if (walletAddress == "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG") {
                    Log.d(TAG, "🔍 DEBUG: No metadata amount, using ${if (parsedAmount > 0.0) "parsed" else "fallback"} $fallbackAmount for $tokenSymbol")
                    Log.d(TAG, "🔍 DEBUG: detectedTokenFromMeta=$detectedTokenFromMeta, balanceChangeDirection=$balanceChangeDirection")
                }
                fallbackAmount
            }
            
            return Transaction(
                id = signature.take(8),
                recipient = toAddress,
                sender = fromAddress,
                amount = amount,
                date = date,
                description = if (transactionType == TransactionType.SENT) "Sent $tokenSymbol" else "Received $tokenSymbol",
                currency = tokenSymbol,
                transactionType = transactionType,
                tokenSymbol = tokenSymbol,
                tokenIcon = tokenIcon,
                tokenName = tokenName
            )
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse SPL token transfer: ${e.message}")
        }
        return null
    }
    
    private fun decodeBase58(input: String): ByteArray {
        return try {
            val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
            
            // Simple base58 decoder - might not be perfect but should work for our use case
            var num = java.math.BigInteger.ZERO
            val base = java.math.BigInteger.valueOf(58)
            
            for (char in input) {
                val index = alphabet.indexOf(char)
                if (index == -1) return byteArrayOf() // Invalid character
                num = num.multiply(base).add(java.math.BigInteger.valueOf(index.toLong()))
            }
            
            val bytes = num.toByteArray()
            
            // Count leading zeros in the original string
            var leadingZeros = 0
            for (char in input) {
                if (char == '1') leadingZeros++ else break
            }
            
            // Add leading zeros to the result
            val result = ByteArray(leadingZeros + bytes.size)
            bytes.copyInto(result, leadingZeros)
            
            result
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode base58: ${e.message}")
            byteArrayOf()
        }
    }

    private fun parseAmountFromInstructionData(data: String, isTokenTransfer: Boolean = true): Double {
        return try {
            if (data.isEmpty()) return 1.0
            
            // Decode base58 data (Solana uses base58 encoding)
            val decodedData = decodeBase58(data)
            
            if (isTokenTransfer && decodedData.size >= 9) {
                // For SPL token transfers, amount is typically at bytes 1-8 (little endian)
                // First byte is the instruction type (3 for Transfer)
                if (decodedData[0] == 3.toByte()) {
                    var amount = 0L
                    for (i in 1..8) {
                        if (i < decodedData.size) {
                            amount = amount or ((decodedData[i].toLong() and 0xFF) shl ((i - 1) * 8))
                        }
                    }
                    // Convert from lamports to decimal (assuming 6 decimals for USDC/EURC)
                    return amount.toDouble() / 1_000_000.0
                }
            } else if (!isTokenTransfer && decodedData.size >= 12) {
                // For SOL transfers, amount is typically at bytes 4-11 (little endian)
                if (decodedData.size >= 12) {
                    var amount = 0L
                    for (i in 4..11) {
                        amount = amount or ((decodedData[i].toLong() and 0xFF) shl ((i - 4) * 8))
                    }
                    // Convert from lamports to SOL (9 decimals)
                    return amount.toDouble() / 1_000_000_000.0
                }
            }
            
            Log.w(TAG, "⚠️ Could not parse amount from instruction data")
            1.0 // Fallback
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse instruction data: ${e.message}")
            1.0 // Fallback
        }
    }

    private fun makeRpcCall(requestBody: String): String {
        val url = URL(RPC_URL)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }
        
        // Write request body
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody)
            writer.flush()
        }
        
        // Read response
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.use { it.readText() }
        
        connection.disconnect()
        return response
    }
} 