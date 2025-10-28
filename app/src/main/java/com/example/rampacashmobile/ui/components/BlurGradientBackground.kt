package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.rampacashmobile.ui.theme.RampaColors

/**
 * Modifier extension for applying the green blur gradient background
 */
fun Modifier.blurGradient(
    center: Offset = Offset(0.5f, 0.3f),
    radius: Float = 1200f
): Modifier {
    return this.background(
        brush = Brush.radialGradient(
            colors = listOf(
                RampaColors.GradientGreenCenter,  // Green at 15% opacity from center
                RampaColors.GradientGreenEdges    // Transparent at edges
            ),
            center = center,
            radius = radius
        )
    )
}

/**
 * Wrapper composable that provides the standard Rampa background with gradient
 * for all app screens
 * 
 * Applies a vertical gradient (purple to dark grey) then adds the green blur effect
 */
@Composable
fun RampaScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background gradient layers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RampaColors.GradientPurpleTop,    // Purple at top (#9A46FF at 20% opacity)
                            RampaColors.GradientBottom        // Dark grey/black at bottom (#0E0F10)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurGradient() // Green blur radial gradient on top
        )
        
        // Content on top
        content()
    }
}
