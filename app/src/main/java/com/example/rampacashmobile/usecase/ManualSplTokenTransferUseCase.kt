package com.example.rampacashmobile.usecase

import android.net.Uri
import android.util.Base64
import com.funkatronics.encoders.Base58
import android.util.Log
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.solana.publickey.SolanaPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Manual SPL Token Transfer Use Case
 * 
 * This implementation bypasses the web3-solana library's transaction serialization
 * to fix the "transaction cannot be simulated" issue in Solflare wallet.
 * 
 * Uses direct RPC calls and manual transaction building instead of the library's
 * Message.Builder() which has serialization bugs for Mobile Wallet Adapter.
 */
object ManualSplTokenTransferUseCase {
    
    private const val TAG = "ManualSplTransfer"
    
    /**
     * Create SPL token transfer transaction using manual transaction building
     * 
     * This method:
     * 1. Creates ATA accounts if needed using direct RPC
     * 2. Builds transfer instruction manually  
     * 3. Constructs raw transaction bytes
     * 4. Returns serialized transaction ready for MWA
     */
    suspend fun transfer(
        rpcUri: Uri,
        fromWallet: SolanaPublicKey,
        toWallet: SolanaPublicKey,
        mint: SolanaPublicKey,
        amount: Long,
        payer: SolanaPublicKey? = null
    ): ByteArray = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "=== Manual SPL Token Transfer ===")
        Log.d(TAG, "From: ${fromWallet.base58()}")
        Log.d(TAG, "To: ${toWallet.base58()}")
        Log.d(TAG, "Mint: ${mint.base58()}")
        Log.d(TAG, "Amount: $amount")
        
        val actualPayer = payer ?: fromWallet
        
        // 1. Get recent blockhash
        Log.d(TAG, "Step 1: Getting recent blockhash from RPC...")
        val recentBlockhash = try {
            getRecentBlockhash(rpcUri)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get recent blockhash: ${e.message}", e)
            throw IllegalStateException("Failed to get recent blockhash: ${e.message}", e)
        }
        Log.d(TAG, "✅ Recent blockhash: $recentBlockhash")
        
        // 2. Derive ATAs
        Log.d(TAG, "Step 2: Deriving Associated Token Accounts...")
        val senderAta = try {
            AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(fromWallet, mint)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to derive sender ATA: ${e.message}", e)
            throw IllegalStateException("Failed to derive sender ATA: ${e.message}", e)
        }
        val recipientAta = try {
            AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(toWallet, mint)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to derive recipient ATA: ${e.message}", e)
            throw IllegalStateException("Failed to derive recipient ATA: ${e.message}", e)
        }
        
        Log.d(TAG, "✅ Sender ATA: ${senderAta.base58()}")
        Log.d(TAG, "✅ Recipient ATA: ${recipientAta.base58()}")
        
        // 3. Check if ATAs exist (with delay to avoid rapid RPC calls)
        Log.d(TAG, "Step 3: Checking ATA existence...")
        
        Log.d(TAG, "🔍 Checking sender ATA existence...")
        val senderAtaExists = try {
            checkAccountExists(rpcUri, senderAta)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check sender ATA existence: ${e.message}", e)
            // Assume sender ATA exists if check fails (safer for transaction building)
            Log.w(TAG, "⚠️ Assuming sender ATA exists due to check failure")
            true
        }
        
        // Add small delay to avoid rapid successive calls
        kotlinx.coroutines.delay(100)
        
        Log.d(TAG, "🔍 Checking recipient ATA existence...")
        val recipientAtaExists = try {
            checkAccountExists(rpcUri, recipientAta)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check recipient ATA existence: ${e.message}", e)
            // Assume recipient ATA doesn't exist if check fails (safer - we'll create it)
            Log.w(TAG, "⚠️ Assuming recipient ATA doesn't exist due to check failure")
            false
        }
        
        Log.d(TAG, "✅ Sender ATA exists: $senderAtaExists")
        Log.d(TAG, "✅ Recipient ATA exists: $recipientAtaExists")
        
        if (!senderAtaExists) {
            throw IllegalStateException("Sender ATA does not exist. Please get some tokens first.")
        }
        
        // 4. Build instructions
        val instructions = mutableListOf<TransactionInstruction>()
        
        // Create recipient ATA if needed
        if (!recipientAtaExists) {
            Log.d(TAG, "Creating recipient ATA instruction")
            val createAtaInstruction = createAssociatedTokenAccountInstruction(
                payer = actualPayer,
                owner = toWallet,
                mint = mint
            )
            instructions.add(createAtaInstruction)
        }
        
        // Add transfer instruction
        Log.d(TAG, "Creating transfer instruction")
        val transferInstruction = createTransferInstruction(
            fromTokenAccount = senderAta,
            toTokenAccount = recipientAta,
            owner = fromWallet,
            amount = amount
        )
        instructions.add(transferInstruction)
        
        // 5. Build transaction manually
        Log.d(TAG, "Step 5: Building transaction with ${instructions.size} instructions...")
        val transaction = try {
            buildTransaction(
                instructions = instructions,
                recentBlockhash = recentBlockhash,
                feePayer = actualPayer
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to build transaction: ${e.message}", e)
            throw IllegalStateException("Failed to build transaction: ${e.message}", e)
        }
        
        Log.d(TAG, "✅ Transaction built successfully, size: ${transaction.size} bytes")
        Log.d(TAG, "=== End Manual SPL Token Transfer ===")
        
        return@withContext transaction
    }
    
    /**
     * Get latest blockhash via direct RPC call (using newer RPC method)
     */
    private suspend fun getRecentBlockhash(rpcUri: Uri): String = withContext(Dispatchers.IO) {
        val requestBody = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getLatestBlockhash")  // Updated to newer RPC method
            putJsonArray("params") {
                addJsonObject {
                    put("commitment", "confirmed")
                }
            }
        }
        
        val response = makeRpcCall(rpcUri, requestBody)
        return@withContext response["result"]!!.jsonObject["value"]!!.jsonObject["blockhash"]!!.jsonPrimitive.content
    }
    
    /**
     * Check if account exists via direct RPC call
     */
    private suspend fun checkAccountExists(rpcUri: Uri, account: SolanaPublicKey): Boolean = withContext(Dispatchers.IO) {
        val requestBody = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getAccountInfo")
            putJsonArray("params") {
                add(account.base58())
                addJsonObject {
                    put("encoding", "base64")
                    put("commitment", "confirmed")
                }
            }
        }
        
        val response = makeRpcCall(rpcUri, requestBody)
        return@withContext response["result"]!!.jsonObject["value"] != JsonNull
    }
    
    /**
     * Make RPC call and return JSON response
     */
    private suspend fun makeRpcCall(rpcUri: Uri, requestBody: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 Making RPC call to: $rpcUri")
            Log.d(TAG, "📝 Request: ${requestBody.toString().take(100)}...")
            
            val url = URL(rpcUri.toString())
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "RampaCash-Mobile/1.0")
            connection.connectTimeout = 5000  // 5 seconds (shorter timeout)
            connection.readTimeout = 8000     // 8 seconds (shorter timeout)
            connection.doOutput = true
            connection.useCaches = false      // Don't use cache
            connection.setRequestProperty("Connection", "close") // Close connection after use
            
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
                connection.disconnect()
                throw RuntimeException("RPC call failed with code: $responseCode, error: $errorResponse")
            }
            
            val response = connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            Log.d(TAG, "📨 Response: ${response.take(200)}...")
            
            // Explicitly disconnect after reading response
            connection.disconnect()
            
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
     * Create ATA creation instruction manually
     */
    private fun createAssociatedTokenAccountInstruction(
        payer: SolanaPublicKey,
        owner: SolanaPublicKey,
        mint: SolanaPublicKey
    ): TransactionInstruction {
        
        val associatedTokenAccount = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(owner, mint)
        
        return TransactionInstruction(
            programId = SolanaPublicKey.from("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL"), // Associated Token Program
            accounts = listOf(
                AccountMeta(payer, isSigner = true, isWritable = true),                    // Payer
                AccountMeta(associatedTokenAccount, isSigner = false, isWritable = true),  // Associated token account
                AccountMeta(owner, isSigner = false, isWritable = false),                 // Owner
                AccountMeta(mint, isSigner = false, isWritable = false),                  // Mint
                AccountMeta(SolanaPublicKey.from("11111111111111111111111111111112"), isSigner = false, isWritable = false), // System Program
                AccountMeta(SolanaPublicKey.from("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), isSigner = false, isWritable = false), // Token Program
            ),
            data = byteArrayOf() // ATA creation has no instruction data
        )
    }
    
    /**
     * Create SPL token transfer instruction manually
     */
    private fun createTransferInstruction(
        fromTokenAccount: SolanaPublicKey,
        toTokenAccount: SolanaPublicKey,
        owner: SolanaPublicKey,
        amount: Long
    ): TransactionInstruction {
        
        // Transfer instruction data (instruction type 3 + amount as little-endian u64)
        val data = ByteArray(9)
        data[0] = 3.toByte() // Transfer instruction type
        
        // Convert amount to little-endian bytes
        val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(amount)
        System.arraycopy(buffer.array(), 0, data, 1, 8)
        
        return TransactionInstruction(
            programId = SolanaPublicKey.from("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), // Token Program
            accounts = listOf(
                AccountMeta(fromTokenAccount, isSigner = false, isWritable = true),  // Source account
                AccountMeta(toTokenAccount, isSigner = false, isWritable = true),    // Destination account
                AccountMeta(owner, isSigner = true, isWritable = false)             // Owner
            ),
            data = data
        )
    }
    
    /**
     * Build complete transaction manually (this is the key part that bypasses web3-solana bugs)
     */
    private fun buildTransaction(
        instructions: List<TransactionInstruction>,
        recentBlockhash: String,
        feePayer: SolanaPublicKey
    ): ByteArray {
        
        // Collect all unique accounts with their metadata
        val accountMap = mutableMapOf<SolanaPublicKey, AccountInfo>()
        
        // Fee payer is always first and is signer + writable
        accountMap[feePayer] = AccountInfo(feePayer, isSigner = true, isWritable = true)
        
        // Add accounts from instructions
        instructions.forEach { instruction ->
            instruction.accounts.forEach { meta ->
                val existing = accountMap[meta.publicKey]
                if (existing != null) {
                    // Merge permissions (if any instruction needs writable/signer, keep it)
                    accountMap[meta.publicKey] = AccountInfo(
                        meta.publicKey,
                        isSigner = existing.isSigner || meta.isSigner,
                        isWritable = existing.isWritable || meta.isWritable
                    )
                } else {
                    accountMap[meta.publicKey] = AccountInfo(meta.publicKey, meta.isSigner, meta.isWritable)
                }
            }
            
            // Programs are always read-only, non-signers
            if (!accountMap.containsKey(instruction.programId)) {
                accountMap[instruction.programId] = AccountInfo(instruction.programId, isSigner = false, isWritable = false)
            }
        }
        
        // Sort accounts according to Solana requirements:
        // 1. Signers (writable first, then read-only)
        // 2. Non-signers writable
        // 3. Non-signers read-only
        val accountsList = accountMap.values.sortedWith(compareBy<AccountInfo> { 
            when {
                it.isSigner && it.isWritable -> 0  // Signer + writable (fee payer first)
                it.isSigner && !it.isWritable -> 1 // Signer + read-only
                !it.isSigner && it.isWritable -> 2 // Non-signer + writable  
                else -> 3                           // Non-signer + read-only (programs)
            }
        }.thenBy { 
            // Within each category, maintain fee payer first
            if (it.publicKey == feePayer) 0 else 1
        }).map { it.publicKey }
        
        val accountToIndex = accountsList.mapIndexed { index, account -> account to index }.toMap()
        
        Log.d(TAG, "Transaction accounts (${accountsList.size}):")
        accountsList.forEachIndexed { index, account ->
            val info = accountMap[account]!!
            val flags = buildString {
                if (info.isSigner) append("S")
                if (info.isWritable) append("W")
                if (!info.isSigner && !info.isWritable) append("R")
            }
            Log.d(TAG, "  $index: ${account.base58()} [$flags]")
        }
        
        // Calculate message header counts from the sorted account list
        var numRequiredSignatures = 0
        var numReadOnlySignedAccounts = 0  
        var numReadOnlyUnsignedAccounts = 0
        
        // Count account types based on the sorted account list
        accountsList.forEach { account ->
            val accountInfo = accountMap[account]!!
            if (accountInfo.isSigner) {
                numRequiredSignatures++
                if (!accountInfo.isWritable) {
                    numReadOnlySignedAccounts++
                }
            } else if (!accountInfo.isWritable) {
                numReadOnlyUnsignedAccounts++
            }
        }
        
        Log.d(TAG, "Message header: signatures=$numRequiredSignatures, readOnlySigned=$numReadOnlySignedAccounts, readOnlyUnsigned=$numReadOnlyUnsignedAccounts")
        
        // Build complete transaction (signatures + message) for MWA
        val output = ByteArrayOutputStream()
        
        // PART 1: TRANSACTION SIGNATURES
        // 1. Signature count (compact-u16)
        writeCompactU16(output, numRequiredSignatures)
        
        // 2. Signature placeholders (64 bytes each, all zeros)
        repeat(numRequiredSignatures) {
            output.write(ByteArray(64)) // 64 zero bytes for each signature placeholder
        }
        
        // PART 2: TRANSACTION MESSAGE
        // 3. Required signatures count (1 byte)
        output.write(numRequiredSignatures)        // Total signatures required
        output.write(numReadOnlySignedAccounts)   // Read-only signed accounts  
        output.write(numReadOnlyUnsignedAccounts) // Read-only unsigned accounts
        
        // 4. Account keys array
        writeCompactU16(output, accountsList.size) // Account count (compact-u16)
        accountsList.forEach { account ->
            output.write(account.bytes) // Each account is 32 bytes
        }
        
        // 5. Recent blockhash (32 bytes)
        val blockhashBytes = Base58.decode(recentBlockhash)
        output.write(blockhashBytes)
        
        // 6. Instructions array
        writeCompactU16(output, instructions.size) // Instruction count (compact-u16)
        
        instructions.forEachIndexed { instructionIndex, instruction ->
            // Program ID index
            val programIndex = accountToIndex[instruction.programId]!!
            output.write(programIndex)
            
            Log.d(TAG, "Instruction $instructionIndex: program=${instruction.programId.base58()} (index $programIndex)")
            
            // Account indices
            writeCompactU16(output, instruction.accounts.size) // Account count (compact-u16)
            instruction.accounts.forEachIndexed { accountIndex, meta ->
                val accountIndexInTransaction = accountToIndex[meta.publicKey]!!
                output.write(accountIndexInTransaction)
                
                val flags = buildString {
                    if (meta.isSigner) append("S")
                    if (meta.isWritable) append("W")
                    if (!meta.isSigner && !meta.isWritable) append("R")
                }
                Log.d(TAG, "  Account $accountIndex: ${meta.publicKey.base58()} -> tx_index $accountIndexInTransaction [$flags]")
            }
            
            // Instruction data
            writeCompactU16(output, instruction.data.size) // Data length (compact-u16)
            output.write(instruction.data)
            
            Log.d(TAG, "  Data: ${instruction.data.joinToString(" ") { "%02x".format(it) }}")
        }
        
        val transactionBytes = output.toByteArray()
        
        Log.d(TAG, "=== Complete Transaction Summary ===")
        Log.d(TAG, "Total size: ${transactionBytes.size} bytes")
        Log.d(TAG, "Signature placeholders: $numRequiredSignatures × 64 bytes = ${numRequiredSignatures * 64} bytes")
        Log.d(TAG, "Message size: ${transactionBytes.size - 1 - (numRequiredSignatures * 64)} bytes") // Subtract signature count + signatures
        Log.d(TAG, "Signatures required: $numRequiredSignatures")
        Log.d(TAG, "Accounts: ${accountsList.size}")
        Log.d(TAG, "Instructions: ${instructions.size}")
        Log.d(TAG, "Blockhash: $recentBlockhash")
        Log.d(TAG, "=== End Complete Transaction Summary ===")
        
        return transactionBytes
    }
    
    /**
     * Write compact-u16 encoding as used by Solana
     * - Values 0-127: single byte
     * - Values 128-16383: two bytes with high bit encoding
     */
    private fun writeCompactU16(output: ByteArrayOutputStream, value: Int) {
        when {
            value < 128 -> {
                output.write(value)
            }
            value < 16384 -> {
                output.write(0x80 or (value and 0x7f))
                output.write(value shr 7)
            }
            else -> {
                // For larger values (rarely used in transactions)
                output.write(0x80 or (value and 0x7f))
                output.write(0x80 or ((value shr 7) and 0x7f))
                output.write(value shr 14)
            }
        }
    }
    
    /**
     * Data classes for manual transaction building
     */
    data class TransactionInstruction(
        val programId: SolanaPublicKey,
        val accounts: List<AccountMeta>,
        val data: ByteArray
    )
    
    data class AccountMeta(
        val publicKey: SolanaPublicKey,
        val isSigner: Boolean,
        val isWritable: Boolean
    )
    
    data class AccountInfo(
        val publicKey: SolanaPublicKey,
        val isSigner: Boolean,
        val isWritable: Boolean
    )
} 