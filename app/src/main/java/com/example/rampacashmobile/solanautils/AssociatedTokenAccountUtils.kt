package com.example.rampacashmobile.solanautils

import android.net.Uri
import com.example.rampacashmobile.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import com.solana.publickey.SolanaPublicKey
import com.solana.rpccore.JsonRpc20Request
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.add
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

object AssociatedTokenAccountUtils {
    // 1. DERIVE ATA ADDRESS
    fun deriveAssociatedTokenAccount(
        owner: SolanaPublicKey,  // User's wallet address
        mint: SolanaPublicKey    // Token mint address (USDC, EURC, etc.)
    ): SolanaPublicKey {

        android.util.Log.d("ATAUtils", "=== Deriving ATA Address ===")
        android.util.Log.d("ATAUtils", "Owner: ${owner.base58()}")
        android.util.Log.d("ATAUtils", "Mint: ${mint.base58()}")
        android.util.Log.d("ATAUtils", "Token Program: ${ProgramIds.TOKEN_PROGRAM}")
        android.util.Log.d("ATAUtils", "ATA Program: ${ProgramIds.ASSOCIATED_TOKEN_PROGRAM}")

        // Create seeds for Program Derived Address (PDA)
        val seeds = listOf(
            owner.bytes,                                                // User's wallet
            SolanaPublicKey.from(ProgramIds.TOKEN_PROGRAM).bytes,      // SPL Token Program
            mint.bytes                                                  // Token mint
        )

        android.util.Log.d("ATAUtils", "Seeds created: ${seeds.size} seeds")
        seeds.forEachIndexed { index, seed ->
            android.util.Log.d("ATAUtils", "  Seed [$index]: ${seed.size} bytes - ${seed.take(8).joinToString { "%02x".format(it) }}...")
        }

        // Generate deterministic address that has no private key
        val derivedAddress = try {
            findProgramAddress(
                seeds,
                SolanaPublicKey.from(ProgramIds.ASSOCIATED_TOKEN_PROGRAM)
            )
        } catch (e: Exception) {
            android.util.Log.e("ATAUtils", "❌ PDA derivation failed: ${e.message}", e)
            throw e
        }
        
        android.util.Log.d("ATAUtils", "✅ Derived ATA: ${derivedAddress.base58()}")
        return derivedAddress
    }

    // Custom PDA derivation function since SolanaPublicKey doesn't have findProgramAddress
    private fun findProgramAddress(seeds: List<ByteArray>, programId: SolanaPublicKey): SolanaPublicKey {
        android.util.Log.d("ATAUtils", "Finding program address with ${seeds.size} seeds for program ${programId.base58()}")
        
        for (nonce in 255 downTo 1) {
            try {
                val address = createProgramAddress(seeds + listOf(byteArrayOf(nonce.toByte())), programId)
                android.util.Log.d("ATAUtils", "✅ Found valid PDA with nonce $nonce: ${address.base58()}")
                return address
            } catch (e: Exception) {
                // Continue trying with next nonce
                if (nonce == 1) {
                    android.util.Log.d("ATAUtils", "Tried nonce $nonce, continuing...")
                }
            }
        }
        android.util.Log.e("ATAUtils", "❌ Unable to find a viable program address nonce")
        throw IllegalStateException("Unable to find a viable program address nonce")
    }

    private fun createProgramAddress(seeds: List<ByteArray>, programId: SolanaPublicKey): SolanaPublicKey {
        val sha256 = MessageDigest.getInstance("SHA-256")
        
        // Add all seeds to the hash
        seeds.forEach { seed ->
            sha256.update(seed)
        }
        
        // Add program ID
        sha256.update(programId.bytes)
        
        // Add the PDA marker
        sha256.update("ProgramDerivedAddress".toByteArray(StandardCharsets.UTF_8))
        
        val hash = sha256.digest()
        
        // Check if the hash is on the curve (for Ed25519, we need to ensure it's a valid point)
        if (isOnCurve(hash)) {
            throw IllegalArgumentException("Invalid seeds, address is on curve")
        }
        
        return SolanaPublicKey(hash)
    }

    private fun isOnCurve(pubkeyBytes: ByteArray): Boolean {
        return (pubkeyBytes[31].toInt() and 0xE0) != 0
    }

    // 2. CHECK IF ATA EXISTS
    suspend fun checkAccountExists(rpcUri: Uri, accountAddress: SolanaPublicKey): Boolean {
        return try {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val request = JsonRpc20Request(
                method = "getAccountInfo",
                params = buildJsonArray {
                    add(accountAddress.base58())
                },
                UUID.randomUUID().toString()
            )

            val response = rpc.makeRequest(request, JsonElement.serializer())
            // If account exists, result won't be null
            response.result != JsonNull
        } catch (e: Exception) {
            false
        }
    }

    // 3. CREATE ATA INSTRUCTION (if needed)
    fun createAssociatedTokenAccountInstruction(
        payer: SolanaPublicKey,      // Who pays for account creation
        owner: SolanaPublicKey,      // Who owns the token account
        mint: SolanaPublicKey        // Which token
    ): TransactionInstruction {
        val associatedTokenAccount = deriveAssociatedTokenAccount(owner, mint)

        return TransactionInstruction(
            SolanaPublicKey.from(ProgramIds.ASSOCIATED_TOKEN_PROGRAM),
            listOf(
                AccountMeta(payer, isSigner = true, isWritable = true),                    // Payer (signer, writable)
                AccountMeta(associatedTokenAccount, isSigner = false, isWritable = true),  // New ATA (writable)
                AccountMeta(owner, isSigner = false, isWritable = false),                  // Owner (read-only)
                AccountMeta(mint, isSigner = false, isWritable = false),                   // Token mint (read-only)
                AccountMeta(SolanaPublicKey.from(ProgramIds.SYSTEM_PROGRAM),
                    isSigner = false,
                    isWritable = false
                ), // System Program
                AccountMeta(SolanaPublicKey.from(ProgramIds.TOKEN_PROGRAM),
                    isSigner = false,
                    isWritable = false
                ), // Token Program
            ),
            byteArrayOf() // No instruction data needed
        )
    }

    // 4. SPL TOKEN TRANSFER UTILITIES

    /**
     * Creates instruction data for SPL token transfer
     * Format: [instruction_type: u8][amount: u64]
     */
    fun createSplTransferInstructionData(amount: Long): ByteArray {
        val buffer = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(3.toByte())  // Transfer instruction type
        buffer.putLong(amount)   // Amount
        return buffer.array()
    }

    /**
     * Creates an SPL token transfer instruction
     */
    fun createSplTransferInstruction(
        fromTokenAccount: SolanaPublicKey,  // Source token account
        toTokenAccount: SolanaPublicKey,    // Destination token account  
        ownerAddress: SolanaPublicKey,      // Owner of source account
        amount: Long                        // Amount (consider token decimals)
    ): TransactionInstruction {
        android.util.Log.d("ATAUtils", "=== Creating SPL Transfer Instruction ===")
        android.util.Log.d("ATAUtils", "From token account: ${fromTokenAccount.base58()}")
        android.util.Log.d("ATAUtils", "To token account: ${toTokenAccount.base58()}")
        android.util.Log.d("ATAUtils", "Owner address: ${ownerAddress.base58()}")
        android.util.Log.d("ATAUtils", "Amount: $amount")
        android.util.Log.d("ATAUtils", "Token program: ${ProgramIds.TOKEN_PROGRAM}")
        
        val accounts = listOf(
            AccountMeta(fromTokenAccount, isSigner = false, isWritable = true), // Source token account
            AccountMeta(toTokenAccount, isSigner = false, isWritable = true),   // Dest token account
            AccountMeta(ownerAddress, isSigner = true, isWritable = false)      // Owner (signer)
        )
        
        android.util.Log.d("ATAUtils", "Account metas:")
        accounts.forEachIndexed { index, account ->
            android.util.Log.d("ATAUtils", "  [$index] ${account.publicKey.base58()} (signer=${account.isSigner}, writable=${account.isWritable})")
        }
        
        val instructionData = createSplTransferInstructionData(amount)
        android.util.Log.d("ATAUtils", "Instruction data: ${instructionData.joinToString { "%02x".format(it) }}")
        
        return TransactionInstruction(
            SolanaPublicKey.from(ProgramIds.TOKEN_PROGRAM),
            accounts,
            instructionData
        )
    }

    /**
     * Helper function to create SPL transfer instruction with automatic ATA derivation
     */
    fun createSplTransferInstructionWithAta(
        fromOwner: SolanaPublicKey,     // Owner of source tokens
        toOwner: SolanaPublicKey,       // Owner of destination tokens  
        mint: SolanaPublicKey,          // Token mint address
        amount: Long                    // Amount to transfer
    ): TransactionInstruction {
        android.util.Log.d("ATAUtils", "=== Creating SPL Transfer with ATA Derivation ===")
        android.util.Log.d("ATAUtils", "From owner: ${fromOwner.base58()}")
        android.util.Log.d("ATAUtils", "To owner: ${toOwner.base58()}")
        android.util.Log.d("ATAUtils", "Mint: ${mint.base58()}")
        
        val fromTokenAccount = deriveAssociatedTokenAccount(fromOwner, mint)
        val toTokenAccount = deriveAssociatedTokenAccount(toOwner, mint)
        
        android.util.Log.d("ATAUtils", "Derived from ATA: ${fromTokenAccount.base58()}")
        android.util.Log.d("ATAUtils", "Derived to ATA: ${toTokenAccount.base58()}")
        
        return createSplTransferInstruction(fromTokenAccount, toTokenAccount, fromOwner, amount)
    }

    /**
     * Check if both ATAs exist for a transfer, returns list of missing ATA creation instructions
     */
    suspend fun getRequiredAtaCreationInstructions(
        rpcUri: Uri,
        fromOwner: SolanaPublicKey,
        toOwner: SolanaPublicKey,
        mint: SolanaPublicKey,
        payer: SolanaPublicKey
    ): List<TransactionInstruction> {
        val instructions = mutableListOf<TransactionInstruction>()
        
        val fromAta = deriveAssociatedTokenAccount(fromOwner, mint)
        val toAta = deriveAssociatedTokenAccount(toOwner, mint)
        
        // Check if fromAta exists (should exist for sender)
        if (!checkAccountExists(rpcUri, fromAta)) {
            instructions.add(createAssociatedTokenAccountInstruction(payer, fromOwner, mint))
        }
        
        // Check if toAta exists (might need to be created for recipient)
        if (!checkAccountExists(rpcUri, toAta)) {
            instructions.add(createAssociatedTokenAccountInstruction(payer, toOwner, mint))
        }
        
        return instructions
    }
}