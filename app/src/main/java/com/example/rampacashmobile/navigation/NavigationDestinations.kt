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
    object Home : NavigationDestination("dashboard", "Home", customIconRes = R.drawable.ic_nav_home)
    object Transfers : NavigationDestination("transfers", "Transfers", customIconRes = R.drawable.ic_nav_transfers)
    object Send : NavigationDestination("send", "Send", customIconRes = R.drawable.ic_nav_send)
    object Investment : NavigationDestination("investment", "Invest", customIconRes = R.drawable.ic_nav_investment)
    object Learn : NavigationDestination("learn", "Learn", customIconRes = R.drawable.ic_nav_learn)
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