package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.rampacashmobile.ui.theme.RampaColors

/**
 * Creates a radial gradient background with green blur effect.
 * 
 * The gradient transitions from green (#16F096 at 15% opacity) to transparent
 * creating a subtle green blur across the app.
 * 
 * @param center Fractional coordinates (0.0 to 1.0) for the gradient center. Default: slightly above center
 * @param radius Gradient radius in pixels. Default: 1200f for full screen coverage
 */
@Composable
fun BlurGradientBackground(
    modifier: Modifier = Modifier,
    center: Offset = Offset(0.5f, 0.3f),
    radius: Float = 1200f
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RampaColors.GradientGreenCenter,  // Green at 15% opacity from center
                        RampaColors.GradientGreenEdges    // Transparent at edges
                    ),
                    center = center,
                    radius = radius
                )
            )
    )
}

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
 */
@Composable
fun RampaScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RampaColors.CarbonBase) // Base carbon background first
            .blurGradient() // Gradient on top
    ) {
        content()
    }
}
