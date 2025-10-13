package com.example.rampacashmobile

import android.app.Application
import com.example.rampacashmobile.utils.TimberConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RampaCashApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber logging
        TimberConfig.init()
    }
}