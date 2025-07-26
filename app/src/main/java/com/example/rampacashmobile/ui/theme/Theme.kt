// File: app/src/main/java/com/example/rampacashmobile/ui/theme/Theme.kt
package com.example.rampacashmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define your actual colors here (matching your React design)
private val RampaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),        // Indigo-500 (your primary button color)
    secondary = Color(0xFF22C55E),      // Green-500 (your success color)
    tertiary = Color(0xFF3B82F6),       // Blue-500 (your info color)

    // Background colors
    background = Color(0xFF111827),     // Your dark background
    surface = Color(0xFF1F2937),        // Your card/surface color
    surfaceVariant = Color(0xFF374151), // Your border color

    // Text colors
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,         // White text on dark background
    onSurface = Color.White,            // White text on surfaces
    onSurfaceVariant = Color(0xFF9CA3AF), // Gray text for secondary info

    // Other colors
    error = Color(0xFFEF4444),          // Red-500 for errors
    onError = Color.White,
    outline = Color(0xFF374151),        // Border color
    outlineVariant = Color(0xFF4B5563)  // Lighter border
)

private val RampaLightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),        // Keep same primary
    secondary = Color(0xFF22C55E),      // Keep same secondary
    tertiary = Color(0xFF3B82F6),       // Keep same tertiary

    // Light theme backgrounds (optional - you can use dark theme always)
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFE7E0EC),

    // Light theme text
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),

    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun RampaCashMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // You can disable it by setting this to false
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Force dark theme for your app (since your React app is dark)
        else -> RampaDarkColorScheme

        // Alternative: Support both themes
        // darkTheme -> RampaDarkColorScheme
        // else -> RampaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}