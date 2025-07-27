package com.example.rampacashmobile.di

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val solanaUri = Uri.parse("https://solana.com")
val iconUri = Uri.parse("favicon.ico")
val identityName = "Solana"

@Module
@InstallIn(SingletonComponent::class)
object RampaCashMobileModule {
    @Provides
    @Singleton
    fun providesSharedPrefs(@ApplicationContext ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences("scaffold_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesMobileWalletAdapter(): MobileWalletAdapter {
        return MobileWalletAdapter(connectionIdentity = ConnectionIdentity(
            identityUri = solanaUri,
            iconUri = iconUri,
            identityName = identityName
        ))
    }

    // Removed Web3Auth from DI - will be created lazily in ViewModel
} 