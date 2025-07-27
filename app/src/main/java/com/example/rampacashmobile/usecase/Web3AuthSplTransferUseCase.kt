package com.example.rampacashmobile.usecase

import android.net.Uri
import android.util.Log
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.solana.publickey.SolanaPublicKey
import org.sol4k.Keypair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Web3Auth SPL Token Transfer Use Case
 * 
 * Handles SPL token transfers for Web3Auth users by:
 * 1. Building transactions using existing use cases
 * 2. Signing transactions locally with Web3Auth private key
 * 3. Submitting transactions directly to RPC endpoint
 */
object Web3AuthSplTransferUseCase {
    
    private const val TAG = "Web3AuthSplTransfer"
    
    /**
     * Transfer SPL tokens for Web3Auth users
     * 
     * @param rpcUri Solana RPC endpoint
     * @param web3AuthPrivateKey Ed25519 private key from Web3Auth (hex string)
     * @param fromWallet Sender's wallet address (should match Web3Auth public key)
     * @param toWallet Recipient's wallet address
     * @param mint Token mint address (e.g., USDC, EURC)
     * @param amount Token amount in smallest units (e.g., 1000000 = 1 USDC for 6-decimal token)
     * @return Transaction signature if successful
     */
    suspend fun transfer(
        rpcUri: Uri,
        web3AuthPrivateKey: String,
        fromWallet: SolanaPublicKey,
        toWallet: SolanaPublicKey,
        mint: SolanaPublicKey,
        amount: Long
    ): String = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "=== Web3Auth SPL Token Transfer ===")
        Log.d(TAG, "From: ${fromWallet.base58()}")
        Log.d(TAG, "To: ${toWallet.base58()}")
        Log.d(TAG, "Mint: ${mint.base58()}")
        Log.d(TAG, "Amount: $amount")
        
        try {
            // 1. Create Solana keypair from Web3Auth private key
            val keypair = createKeypairFromWeb3AuthKey(web3AuthPrivateKey)
            Log.d(TAG, "✅ Created keypair from Web3Auth private key")
            
            // Verify the keypair matches the expected public key
            val derivedPublicKey = SolanaPublicKey.from(keypair.publicKey.toBase58())
            if (derivedPublicKey.base58() != fromWallet.base58()) {
                throw IllegalArgumentException("Web3Auth private key doesn't match sender wallet address")
            }
            
            // 2. Build transaction using existing manual transfer logic
            Log.d(TAG, "Building transaction...")
            val transactionBytes = ManualSplTokenTransferUseCase.transfer(
                rpcUri = rpcUri,
                fromWallet = fromWallet,
                toWallet = toWallet,
                mint = mint,
                amount = amount,
                payer = fromWallet
            )
            Log.d(TAG, "✅ Transaction built successfully")
            
            // 3. Sign the transaction
            Log.d(TAG, "Signing transaction...")
            val signedTransaction = signTransaction(transactionBytes, keypair)
            Log.d(TAG, "✅ Transaction signed successfully")
            
            // 4. Submit transaction to RPC
            Log.d(TAG, "Submitting transaction to RPC...")
            val signature = submitTransaction(rpcUri, signedTransaction)
            Log.d(TAG, "✅ Transaction submitted successfully: $signature")
            
            Log.d(TAG, "=== End Web3Auth SPL Token Transfer ===")
            return@withContext signature
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Web3Auth SPL transfer failed: ${e.message}", e)
            throw RuntimeException("Web3Auth SPL transfer failed: ${e.message}", e)
        }
    }
    
    /**
     * Create Solana keypair from Web3Auth Ed25519 private key
     */
    private fun createKeypairFromWeb3AuthKey(web3AuthPrivateKey: String): Keypair {
        return try {
            // Convert hex string to byte array
            val privateKeyBytes = web3AuthPrivateKey.hexToByteArray()
            
            // Create keypair using sol4k
            Keypair.fromSecretKey(privateKeyBytes)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create keypair from Web3Auth key: ${e.message}", e)
            throw IllegalArgumentException("Invalid Web3Auth private key: ${e.message}", e)
        }
    }
    
    /**
     * Sign a transaction with the provided keypair
     * 
     * The transaction bytes from ManualSplTokenTransferUseCase contain:
     * - Signature count + signature placeholders (zeros)
     * - Message (accounts, blockhash, instructions)
     * 
     * We need to:
     * 1. Extract the message portion
     * 2. Sign the message
     * 3. Replace signature placeholders with actual signatures
     */
    private fun signTransaction(transactionBytes: ByteArray, keypair: Keypair): ByteArray {
        return try {
            Log.d(TAG, "🔏 Signing transaction of ${transactionBytes.size} bytes")
            
            // Parse transaction structure
            var offset = 0
            
            // 1. Read signature count (compact-u16)
            val (signatureCount, sigCountBytes) = readCompactU16(transactionBytes, offset)
            offset += sigCountBytes
            Log.d(TAG, "Signature count: $signatureCount")
            
            // 2. Skip signature placeholders (64 bytes each)
            val signatureStartOffset = offset
            offset += signatureCount * 64
            
            // 3. The message starts after all signatures
            val messageStartOffset = offset
            val messageBytes = transactionBytes.sliceArray(messageStartOffset until transactionBytes.size)
            
            Log.d(TAG, "Message starts at offset $messageStartOffset, length: ${messageBytes.size}")
            
            // 4. Sign the message
            val signature = keypair.sign(messageBytes)
            Log.d(TAG, "✅ Message signed, signature length: ${signature.size}")
            
            // 5. Replace signature placeholder with actual signature
            val signedTransaction = transactionBytes.copyOf()
            
            // Copy the signature to the first signature slot (we assume single signer for SPL transfers)
            System.arraycopy(signature, 0, signedTransaction, signatureStartOffset, 64)
            
            Log.d(TAG, "✅ Transaction signed successfully")
            signedTransaction
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sign transaction: ${e.message}", e)
            throw RuntimeException("Transaction signing failed: ${e.message}", e)
        }
    }
    
    /**
     * Submit signed transaction to RPC endpoint
     */
    private suspend fun submitTransaction(rpcUri: Uri, signedTransaction: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📡 Submitting transaction to RPC: $rpcUri")
            
            // Encode transaction as base64
            val transactionBase64 = android.util.Base64.encodeToString(signedTransaction, android.util.Base64.NO_WRAP)
            
            // Create RPC request
            val requestBody = buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "sendTransaction")
                putJsonArray("params") {
                    add(transactionBase64)
                    addJsonObject {
                        put("encoding", "base64")
                        put("skipPreflight", false)
                        put("preflightCommitment", "confirmed")
                        put("maxRetries", 3)
                    }
                }
            }
            
            // Submit transaction
            val response = makeRpcCall(rpcUri, requestBody)
            
            // Extract transaction signature
            val signature = response["result"]?.jsonPrimitive?.content
                ?: throw RuntimeException("No transaction signature in response")
            
            Log.d(TAG, "✅ Transaction submitted successfully: $signature")
            return@withContext signature
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to submit transaction: ${e.message}", e)
            throw RuntimeException("Transaction submission failed: ${e.message}", e)
        }
    }
    
    /**
     * Make RPC call and return JSON response
     */
    private suspend fun makeRpcCall(rpcUri: Uri, requestBody: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 Making RPC call to: $rpcUri")
            
            val url = URL(rpcUri.toString())
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "RampaCash-Mobile/1.0")
            connection.connectTimeout = 10000  // 10 seconds
            connection.readTimeout = 15000     // 15 seconds
            connection.doOutput = true
            
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
                os.flush()
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "📡 Response code: $responseCode")
            
            if (responseCode != 200) {
                val errorResponse = try {
                    connection.errorStream?.use { it.readBytes().toString(Charsets.UTF_8) } ?: "No error details"
                } catch (e: Exception) {
                    "Could not read error: ${e.message}"
                }
                Log.e(TAG, "❌ RPC call failed with code $responseCode: $errorResponse")
                throw RuntimeException("RPC call failed with code: $responseCode, error: $errorResponse")
            }
            
            val response = connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            Log.d(TAG, "📨 Response received")
            
            val jsonResponse = Json.parseToJsonElement(response).jsonObject
            
            // Check for RPC errors
            if (jsonResponse.containsKey("error")) {
                val error = jsonResponse["error"]!!.jsonObject
                val errorMessage = error["message"]?.jsonPrimitive?.content ?: "Unknown RPC error"
                Log.e(TAG, "❌ RPC returned error: $errorMessage")
                throw RuntimeException("RPC error: $errorMessage")
            }
            
            return@withContext jsonResponse
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ RPC call exception: ${e.message}", e)
            throw RuntimeException("RPC call failed: ${e.message}", e)
        }
    }
    
    /**
     * Read compact-u16 value from byte array
     * Returns (value, bytesRead)
     */
    private fun readCompactU16(bytes: ByteArray, offset: Int): Pair<Int, Int> {
        val firstByte = bytes[offset].toInt() and 0xFF
        
        return when {
            firstByte < 128 -> {
                // Single byte value
                Pair(firstByte, 1)
            }
            firstByte < 0xC0 -> {
                // Two byte value
                val secondByte = bytes[offset + 1].toInt() and 0xFF
                val value = ((firstByte and 0x7F) or (secondByte shl 7))
                Pair(value, 2)
            }
            else -> {
                // Three byte value (rarely used)
                val secondByte = bytes[offset + 1].toInt() and 0xFF
                val thirdByte = bytes[offset + 2].toInt() and 0xFF
                val value = ((firstByte and 0x7F) or ((secondByte and 0x7F) shl 7) or (thirdByte shl 14))
                Pair(value, 3)
            }
        }
    }
}

/**
 * Extension function to convert hex string to byte array
 */
private fun String.hexToByteArray(): ByteArray {
    return this.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
} 