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
import org.sol4k.PublicKey
import java.util.UUID

object AssociatedTokenAccountUtils {
    fun deriveAssociatedTokenAccount(
        walletAddress: SolanaPublicKey,
        mintAddress: SolanaPublicKey
    ): SolanaPublicKey {
        // Use sol4k's built-in ATA derivation which is guaranteed to be correct
        val sol4kWalletKey = PublicKey(walletAddress.base58())
        val sol4kMintKey = PublicKey(mintAddress.base58())
        
        val (sol4kAta, _) = PublicKey.findProgramDerivedAddress(sol4kWalletKey, sol4kMintKey)
        
        // Convert back to SolanaPublicKey for compatibility with the rest of the codebase
        return SolanaPublicKey.from(sol4kAta.toBase58())
    }

    // 2. CHECK IF ATA EXISTS
    suspend fun checkAccountExists(rpcUri: Uri, accountAddress: SolanaPublicKey): Boolean {
        return try {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val request = JsonRpc20Request(
                method = "getAccountInfo", params = buildJsonArray {
                    add(accountAddress.base58())
                }, UUID.randomUUID().toString()
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
            SolanaPublicKey.from(ProgramIds.ASSOCIATED_TOKEN_PROGRAM), listOf(
                AccountMeta(payer, isSigner = true, isWritable = true), // Payer (signer, writable)
                AccountMeta(associatedTokenAccount, isSigner = false, isWritable = true),  // New ATA (writable)
                AccountMeta(owner, isSigner = false, isWritable = false), // Owner (read-only)
                AccountMeta(mint, isSigner = false, isWritable = false), // Token mint (read-only)
                AccountMeta(SolanaPublicKey.from(ProgramIds.SYSTEM_PROGRAM), isSigner = false, isWritable = false),
                AccountMeta(SolanaPublicKey.from(ProgramIds.TOKEN_PROGRAM), isSigner = false, isWritable = false),
            ), byteArrayOf()
        )
    }

    /**
     * Creates instruction data for SPL token transfer
     * Format: [instruction_type: u8][amount: u64]
     */
    fun createSplTransferInstructionData(amount: Long): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(9).order(java.nio.ByteOrder.LITTLE_ENDIAN)
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
        val accounts = listOf(
            AccountMeta(fromTokenAccount, isSigner = false, isWritable = true), // Source token account
            AccountMeta(toTokenAccount, isSigner = false, isWritable = true),   // Dest token account
            AccountMeta(ownerAddress, isSigner = true, isWritable = false)      // Owner (signer)
        )

        val instructionData = createSplTransferInstructionData(amount)

        return TransactionInstruction(SolanaPublicKey.from(ProgramIds.TOKEN_PROGRAM), accounts, instructionData)
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
        val fromTokenAccount = deriveAssociatedTokenAccount(fromOwner, mint)
        val toTokenAccount = deriveAssociatedTokenAccount(toOwner, mint)

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