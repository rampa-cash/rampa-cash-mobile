package com.example.rampacashmobile.usecase

import android.net.Uri
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.Message
import com.solana.transaction.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SplTokenTransferUseCase {

    /**
     * Transfer SPL tokens between wallets with automatic ATA handling
     *
     * This method automatically:
     * - Derives Associated Token Accounts (ATAs) for both sender and recipient
     * - Creates ATAs if they don't exist
     * - Builds the complete transfer transaction
     *
     * @param rpcUri Solana RPC endpoint
     * @param fromWallet Sender's wallet address
     * @param toWallet Recipient's wallet address
     * @param mint Token mint address (e.g., USDC, EURC)
     * @param amount Token amount in smallest units (e.g., 1000000 = 1 USDC for 6-decimal token)
     * @param payer Who pays for ATA creation fees (defaults to sender)
     * @return Complete transaction ready to be signed and sent
     */
    suspend fun transfer(
        rpcUri: Uri,
        fromWallet: SolanaPublicKey,
        toWallet: SolanaPublicKey,
        mint: SolanaPublicKey,
        amount: Long,
        payer: SolanaPublicKey? = null
    ): Transaction = withContext(Dispatchers.IO) {
        val actualPayer = payer ?: fromWallet
        val instructions = mutableListOf<com.solana.transaction.TransactionInstruction>()

        // Check if we need to create any ATAs first
        val ataCreationInstructions =
            AssociatedTokenAccountUtils.getRequiredAtaCreationInstructions(
                rpcUri = rpcUri,
                fromOwner = fromWallet,
                toOwner = toWallet,
                mint = mint,
                payer = actualPayer
            )

        instructions.addAll(ataCreationInstructions)

        // Add the transfer instruction
        val transferInstruction = AssociatedTokenAccountUtils.createSplTransferInstructionWithAta(
            fromOwner = fromWallet, toOwner = toWallet, mint = mint, amount = amount
        )
        instructions.add(transferInstruction)

        // Get latest blockhash
        val blockhash = RecentBlockhashUseCase(rpcUri)

        // Build transaction message
        val messageBuilder = Message.Builder()
        instructions.forEach { instruction ->
            messageBuilder.addInstruction(instruction)
        }

        val transferMessage = messageBuilder.setRecentBlockhash(blockhash.base58()).build()

        return@withContext Transaction(transferMessage)
    }

    /**
     * Low-level transfer method for when you already know the exact token account addresses
     * Most users should use the `transfer()` method instead
     */
    private suspend fun transferBetweenAccounts(
        rpcUri: Uri,
        fromTokenAccount: SolanaPublicKey,
        toTokenAccount: SolanaPublicKey,
        ownerAddress: SolanaPublicKey,
        amount: Long
    ): Transaction = withContext(Dispatchers.IO) {

        // Get latest blockhash
        val blockhash = RecentBlockhashUseCase(rpcUri)

        // Create transfer instruction using the utility function
        val transferInstruction = AssociatedTokenAccountUtils.createSplTransferInstruction(
            fromTokenAccount = fromTokenAccount,
            toTokenAccount = toTokenAccount,
            ownerAddress = ownerAddress,
            amount = amount
        )

        // Build transaction message
        val transferMessage = Message.Builder().addInstruction(transferInstruction)
            .setRecentBlockhash(blockhash.base58()).build()

        return@withContext Transaction(transferMessage)
    }
}