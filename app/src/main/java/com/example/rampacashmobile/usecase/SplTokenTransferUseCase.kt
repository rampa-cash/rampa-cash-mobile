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

        android.util.Log.d("SplTransferUseCase", "=== Building SPL Transfer Transaction ===")
        android.util.Log.d("SplTransferUseCase", "From wallet: ${fromWallet.base58()}")
        android.util.Log.d("SplTransferUseCase", "To wallet: ${toWallet.base58()}")
        android.util.Log.d("SplTransferUseCase", "Mint: ${mint.base58()}")
        android.util.Log.d("SplTransferUseCase", "Amount: $amount")

        val actualPayer = payer ?: fromWallet
        android.util.Log.d("SplTransferUseCase", "Payer: ${actualPayer.base58()}")
        
        val instructions = mutableListOf<com.solana.transaction.TransactionInstruction>()

        // Check if we need to create any ATAs first
        android.util.Log.d("SplTransferUseCase", "Checking for required ATA creation instructions...")
        val ataCreationInstructions = AssociatedTokenAccountUtils.getRequiredAtaCreationInstructions(
            rpcUri = rpcUri,
            fromOwner = fromWallet,
            toOwner = toWallet,
            mint = mint,
            payer = actualPayer
        )
        
        android.util.Log.d("SplTransferUseCase", "ATA creation instructions needed: ${ataCreationInstructions.size}")
        ataCreationInstructions.forEachIndexed { index, instruction ->
            android.util.Log.d("SplTransferUseCase", "ATA instruction [$index]: ${instruction.accounts.size} accounts")
        }
        
        instructions.addAll(ataCreationInstructions)

        // Add the transfer instruction
        android.util.Log.d("SplTransferUseCase", "Adding transfer instruction...")
        val transferInstruction = AssociatedTokenAccountUtils.createSplTransferInstructionWithAta(
            fromOwner = fromWallet,
            toOwner = toWallet,
            mint = mint,
            amount = amount
        )
        instructions.add(transferInstruction)

        android.util.Log.d("SplTransferUseCase", "Total instructions: ${instructions.size}")
        instructions.forEachIndexed { index, instruction ->
            android.util.Log.d("SplTransferUseCase", "Instruction [$index]: Program=${instruction.programId.base58()}, Accounts=${instruction.accounts.size}")
            instruction.accounts.forEachIndexed { accountIndex, account ->
                android.util.Log.d("SplTransferUseCase", "  Account [$accountIndex]: ${account.publicKey.base58()} (signer=${account.isSigner}, writable=${account.isWritable})")
            }
        }

        // Get latest blockhash
        val blockhash = RecentBlockhashUseCase(rpcUri)
        android.util.Log.d("SplTransferUseCase", "Latest blockhash: ${blockhash.base58()}")

        // Build transaction message
        android.util.Log.d("SplTransferUseCase", "Building transaction message...")
        val messageBuilder = Message.Builder()
        instructions.forEach { instruction ->
            messageBuilder.addInstruction(instruction)
        }
        
        val transferMessage = messageBuilder
            .setRecentBlockhash(blockhash.base58())
            .build()

        android.util.Log.d("SplTransferUseCase", "Transaction message built successfully")
        android.util.Log.d("SplTransferUseCase", "Message accounts: ${transferMessage.accounts.size}")
        transferMessage.accounts.forEachIndexed { index, account ->
            android.util.Log.d("SplTransferUseCase", "  Message account [$index]: ${account.base58()}")
        }

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
        val transferMessage = Message.Builder()
            .addInstruction(transferInstruction)
            .setRecentBlockhash(blockhash.base58())
            .build()

        return@withContext Transaction(transferMessage)
    }
}