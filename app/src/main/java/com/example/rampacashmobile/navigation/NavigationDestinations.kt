package com.example.rampacashmobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationDestination("dashboard", "Home", Icons.Default.Home)
    object Transfers : NavigationDestination("transfers", "Transfers", Icons.AutoMirrored.Filled.List)
    object Send : NavigationDestination("send", "Send", Icons.AutoMirrored.Filled.Send)
    object Rewards : NavigationDestination("rewards", "Rewards", Icons.Default.Star)
    object Card : NavigationDestination("card", "Card", Icons.Default.AccountBox)
}

val bottomNavigationItems = listOf(
    NavigationDestination.Home,
    NavigationDestination.Transfers,
    NavigationDestination.Send,
    NavigationDestination.Rewards,
    NavigationDestination.Card
) 