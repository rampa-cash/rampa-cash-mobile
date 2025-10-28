package com.example.rampacashmobile.ui.theme

import androidx.compose.ui.graphics.Color

object RampaColors {
    // Gradient colors (for green blur effect across the app)
    // Radial gradient: Green blur for a subtle background effect
    val GradientGreenCenter = Color(0xFF16F096).copy(alpha = 0.15f)  // Green at 15% opacity from center
    val GradientGreenEdges = Color(0xFF16F096).copy(alpha = 0.0f)    // Green at 0% opacity at edges (transparent)
    val GradientGreenFull = Color(0xFF16F096)   // Full opacity green for reference
    
    // Vertical gradient colors for splash/onboarding
    val GradientPurpleTop = Color(0xFF9A46FF).copy(alpha = 0.2f)    // Purple at top (20% opacity = 33 hex)
    val GradientBottom = Color(0xFF0E0F10).copy(alpha = 0.01f)

    // Token colors
    val Solana = Color(0xFF9945FF)           // Solana brand purple
    val EURC = Color(0xFF22C55E)             // Green
    val USDC = Color(0xFF3B82F6)             // Blue
}