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
            
            // Get confirmed signatures for the address
            val signatures = getConfirmedSignatures(walletAddress)
            Log.d(TAG, "📝 Found ${signatures.size} signatures")
            
            // Get transaction details for each signature
            val transactions = mutableListOf<Transaction>()
            signatures.take(10).forEach { signature -> // Limit to 10 most recent
                try {
                    val transaction = getTransactionDetails(signature, walletAddress)
                    transaction?.let { transactions.add(it) }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Failed to get details for signature $signature: ${e.message}")
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
                        put("limit", 10) // Reduced from 20 to 10
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
            
            meta?.let { metadata ->
                if (metadata.has("postTokenBalances") && !metadata.isNull("postTokenBalances") &&
                    metadata.has("preTokenBalances") && !metadata.isNull("preTokenBalances")) {
                    
                    val preTokenBalances = metadata.getJSONArray("preTokenBalances")
                    val postTokenBalances = metadata.getJSONArray("postTokenBalances")
                    
                    // Find balance changes for our wallet
                    for (i in 0 until postTokenBalances.length()) {
                        val postBalance = postTokenBalances.getJSONObject(i)
                        if (postBalance.has("mint") && postBalance.has("owner")) {
                            val mint = postBalance.getString("mint")
                            val owner = postBalance.getString("owner")
                            
                            // Check if this balance belongs to our wallet
                            if (owner == walletAddress) {
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
                                
                                // Compare pre and post balances to determine direction
                                try {
                                    val postAmount = postBalance.getJSONObject("uiTokenAmount").getDouble("uiAmount")
                                    
                                    // Find corresponding pre balance
                                    for (j in 0 until preTokenBalances.length()) {
                                        val preBalance = preTokenBalances.getJSONObject(j)
                                        if (preBalance.has("owner") && preBalance.getString("owner") == owner &&
                                            preBalance.has("mint") && preBalance.getString("mint") == mint) {
                                            
                                            val preAmount = preBalance.getJSONObject("uiTokenAmount").getDouble("uiAmount")
                                            
                                            if (postAmount > preAmount) {
                                                balanceChangeDirection = "RECEIVED"
                                                Log.d(TAG, "📈 Balance increased: $preAmount -> $postAmount (RECEIVED)")
                                            } else if (postAmount < preAmount) {
                                                balanceChangeDirection = "SENT"
                                                Log.d(TAG, "📉 Balance decreased: $preAmount -> $postAmount (SENT)")
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
            }
            
            // Debug logging (reduced verbosity)
            if (detectedTokenFromMeta != null) {
                Log.d(TAG, "📊 Transaction ${signature.take(8)} - Token: $detectedTokenFromMeta")
            }
            
            // Parse transaction details
            parseTransaction(signature, accountKeys, instructions, walletAddress, result, detectedTokenFromMeta, balanceChangeDirection)
            
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
        balanceChangeDirection: String? = null
    ): Transaction? {
        try {
            // Get block time for date
            val blockTime = if (transactionResult.has("blockTime") && !transactionResult.isNull("blockTime")) {
                transactionResult.getLong("blockTime") * 1000 // Convert to milliseconds
            } else {
                System.currentTimeMillis() // Fallback to current time
            }
            val date = Date(blockTime)
            
            // Find wallet address index
            var walletIndex = -1
            for (i in 0 until accountKeys.length()) {
                if (accountKeys.getString(i) == walletAddress) {
                    walletIndex = i
                    break
                }
            }
            
            if (walletIndex == -1) {
                Log.w(TAG, "Wallet address not found in transaction accounts")
                return null
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
                        return parseSplTokenTransfer(signature, accountKeys, accounts, walletIndex, walletAddress, date, accountKeys, detectedTokenFromMeta, balanceChangeDirection, instructionData)
                    }
                    "11111111111111111111111111111112" -> {
                        // System Program - Store this but don't return yet (might be rent/fees for token transfer)
                        Log.d(TAG, "📍 Found SOL transfer instruction (storing as backup)")
                        if (foundSolTransfer == null) {
                            foundSolTransfer = { parseSolTransfer(signature, accountKeys, accounts, walletIndex, walletAddress, date, instructionData) }
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
        instructionData: String = ""
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
                
                val parsedAmount = parseAmountFromInstructionData(instructionData, false)
                val amount = if (parsedAmount > 0.0) parsedAmount else 0.001 // Fallback to small amount if parsing fails
                
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
        instructionData: String = ""
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
                }
                "EURC" -> {
                    tokenSymbol = "EURC"
                    tokenIcon = R.drawable.eurc_logo
                    tokenName = "Euro Coin"
                }
                else -> {
                    // PRIORITY 2: Check account keys for mint addresses as fallback
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
                }
            }
            
            // Determine transaction direction based on wallet involvement
            var transactionType = TransactionType.RECEIVED // Default to received
            var fromAddress = "Unknown"
            var toAddress = "Unknown"
            
            // For SPL transfers, look for source and destination token accounts
            if (accounts.length() >= 2) {
                val sourceIndex = accounts.getInt(0)
                val destIndex = accounts.getInt(1)
                
                if (sourceIndex < allAccountKeys.length()) {
                    fromAddress = allAccountKeys.getString(sourceIndex)
                }
                if (destIndex < allAccountKeys.length()) {
                    toAddress = allAccountKeys.getString(destIndex)
                }
                
                // Improved direction detection: check if wallet is the authority/signer
                var walletFoundIndex = -1
                for (i in 0 until allAccountKeys.length()) {
                    if (allAccountKeys.getString(i) == walletAddress) {
                        walletFoundIndex = i
                        break
                    }
                }
                
                                // PRIORITY 1: Use balance change direction if available (most reliable)
                if (!balanceChangeDirection.isNullOrEmpty()) {
                    transactionType = when (balanceChangeDirection) {
                        "SENT" -> TransactionType.SENT
                        "RECEIVED" -> TransactionType.RECEIVED
                        else -> TransactionType.RECEIVED
                    }
                    Log.d(TAG, "✅ Using balance change direction: $balanceChangeDirection")
                } else if (walletFoundIndex != -1) {
                    // PRIORITY 2: Check if wallet appears in the instruction accounts (indicates involvement)
                    var walletInvolvedAsIndex = -1
                    for (i in 0 until accounts.length()) {
                        if (accounts.getInt(i) == walletFoundIndex) {
                            walletInvolvedAsIndex = i
                            break
                        }
                    }
                    
                    // For SPL token transfers:
                    // - If wallet is the authority (usually position 0 or 2), it's likely SENT
                    // - Position 0 is often the source authority/owner
                    // - Position 2 is often the authority for the source account
                    if (walletInvolvedAsIndex == 0 || walletInvolvedAsIndex == 2) {
                        transactionType = TransactionType.SENT
                        Log.d(TAG, "🔍 Wallet found as authority at position $walletInvolvedAsIndex -> SENT")
                    } else {
                        transactionType = TransactionType.RECEIVED
                        Log.d(TAG, "🔍 Wallet found at position $walletInvolvedAsIndex -> RECEIVED")
                    }
                } else {
                    // PRIORITY 3: Fallback - if wallet not found directly, assume RECEIVED
                    transactionType = TransactionType.RECEIVED
                    Log.d(TAG, "🔍 Wallet not found in accounts -> RECEIVED (fallback)")
                }
            }
            
            Log.d(TAG, "✅ $tokenSymbol ${transactionType.name}")
            
            val parsedAmount = parseAmountFromInstructionData(instructionData, true)
            val amount = if (parsedAmount > 0.0) parsedAmount else 1.0 // Fallback to 1.0 if parsing fails
            
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
            val decoded = mutableListOf<Byte>()
            
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