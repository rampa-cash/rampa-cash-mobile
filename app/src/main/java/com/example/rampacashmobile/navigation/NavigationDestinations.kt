package com.example.rampacashmobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rampacashmobile.R

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val customIconRes: Int? = null
) {
    object Home : NavigationDestination("dashboard", "Home", customIconRes = R.drawable.rampa_white_translucid)
    object Transfers : NavigationDestination("transfers", "Transfers", Icons.AutoMirrored.Filled.List)
    object Send : NavigationDestination("send", "Send", Icons.AutoMirrored.Filled.Send)
    object Investment : NavigationDestination("investment", "Invest", customIconRes = R.drawable.chart_icon)
    object Learn : NavigationDestination("learn", "Learn", customIconRes = R.drawable.bonk1_logo)
    object Card : NavigationDestination("card", "Card", Icons.Default.AccountBox)
    object Recharge : NavigationDestination("recharge", "Recharge", Icons.Default.Add)
    object Profile : NavigationDestination("profile", "Profile", Icons.Default.AccountBox)
}

val bottomNavigationItems = listOf(
    NavigationDestination.Home,
    NavigationDestination.Transfers,
    NavigationDestination.Send,
    NavigationDestination.Investment,
    NavigationDestination.Learn
)