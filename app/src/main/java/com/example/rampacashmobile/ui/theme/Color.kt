package com.example.rampacashmobile.ui.theme

import androidx.compose.ui.graphics.Color

object RampaColors {
    // Background colors (matching Figma design system)
    val CarbonBase = Color(0xFF0C0C0C)          // Carbon base - main background from Figma
    
    val Background = Color(0xFF0C0C0C)        // Carbon base - main background
    val Surface = Color(0xFF1a1a1a)            // Slightly lighter surface
    val SurfaceVariant = Color(0xFF262626)    // Card/surface variant

    // Text colors (matching Figma design)
    val OnBackground = Color(0xFFfcfcfd)       // Very light text (background/variant)
    val OnSurface = Color(0xFFfcfcfd)         // Light text on surfaces
    val OnSurfaceVariant = Color(0xFFa1a1aa)  // Gray text for secondary info

    // Gradient colors (for green blur effect across the app)
    // Radial gradient: Green blur for a subtle background effect
    val GradientGreenCenter = Color(0xFF16F096).copy(alpha = 0.15f)  // Green at 15% opacity from center
    val GradientGreenEdges = Color(0xFF16F096).copy(alpha = 0.0f)    // Green at 0% opacity at edges (transparent)
    val GradientGreenFull = Color(0xFF16F096)   // Full opacity green for reference
    
    // Legacy purple gradient (kept for backward compatibility)
    val GradientPurple = Color(0xFF9A46FF).copy(alpha = 0.2f)
    val GradientPurpleFull = Color(0xFF9A46FF)

    // Primary colors
    val Primary = Color(0xFF6366F1)          // indigo-500
    val PrimaryVariant = Color(0xFF4F46E5)   // indigo-600

    // Accent colors
    val Success = Color(0xFF22C55E)          // green-500
    val Info = Color(0xFF3B82F6)             // blue-500
    val Warning = Color(0xFFF59E0B)          // amber-500

    // Token colors
    val Solana = Color(0xFF9945FF)           // Solana brand purple
    val EURC = Color(0xFF22C55E)             // Green
    val USDC = Color(0xFF3B82F6)             // Blue

    // Border colors
    val Border = Color(0xFF374151)           // border-gray-600
    val BorderLight = Color(0xFF4B5563)      // border-gray-500
    
    // Helper function to get the radial gradient colors for green blur effect
    fun getBlurGradientColors(): Pair<Color, Color> {
        return Pair(GradientGreenCenter, GradientGreenEdges)
    }
}