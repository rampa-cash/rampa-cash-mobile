package com.example.rampacashmobile.utils

import android.util.Log
import com.example.rampacashmobile.BuildConfig
import timber.log.Timber

/**
 * Timber configuration for the application
 * 
 * This class sets up Timber logging with appropriate configurations
 * for debug and release builds
 */
object TimberConfig {
    
    /**
     * Initialize Timber with appropriate tree for the current build type
     */
    fun init() {
        if (BuildConfig.DEBUG) {
            // Debug build: Use DebugTree for detailed logging
            Timber.plant(DebugTree())
        } else {
            // Release build: Use ReleaseTree for minimal logging
            Timber.plant(ReleaseTree())
        }
    }
    
    /**
     * Debug tree that logs everything with detailed information
     */
    private class DebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            return "RampaCash[${element.methodName}]"
        }
        
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Add emoji prefixes for better visual identification
            val emojiMessage = when (priority) {
                Log.VERBOSE -> "🔍 $message"
                Log.DEBUG -> "🔧 $message"
                Log.INFO -> "ℹ️ $message"
                Log.WARN -> "⚠️ $message"
                Log.ERROR -> "❌ $message"
                else -> message
            }
            
            super.log(priority, tag, emojiMessage, t)
        }
    }
    
    /**
     * Release tree that only logs errors and warnings
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Only log errors and warnings in release builds
            if (priority >= Log.WARN) {
                when (priority) {
                    Log.WARN -> android.util.Log.w(tag, "⚠️ $message", t)
                    Log.ERROR -> android.util.Log.e(tag, "❌ $message", t)
                    Log.ASSERT -> android.util.Log.wtf(tag, "💥 $message", t)
                }
            }
        }
    }
}
