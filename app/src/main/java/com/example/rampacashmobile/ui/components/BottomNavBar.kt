package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.rampacashmobile.navigation.NavigationDestination
import com.example.rampacashmobile.navigation.bottomNavigationItems

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main Navigation Bar
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            containerColor = Color(0xFF1F2937), // Dark background matching React version
            contentColor = Color(0xFFD1D5DB), // Light content color
            tonalElevation = 8.dp
        ) {
            bottomNavigationItems.forEach { destination ->
                if (destination == NavigationDestination.Send) {
                    // Empty space for the floating send button
                    Box(
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (destination.route == "dashboard") {
                                // Navigate to dashboard and clear the entire back stack
                                navController.navigate("dashboard") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                // For other destinations, navigate normally
                                navController.navigate(destination.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = destination.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981), // Active green color
                            selectedTextColor = Color(0xFF10B981),
                            unselectedIconColor = Color(0xFFD1D5DB), // Light gray for inactive
                            unselectedTextColor = Color(0xFFD1D5DB),
                            indicatorColor = Color.Transparent // Remove default indicator
                        )
                    )
                }
            }
        }

        // Floating Send Button positioned above the navigation bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp), // Match React version's top: '-20px'
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("send") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.size(60.dp), // Match React version's 60px
                containerColor = Color(0xFF10B981), // Green color matching React version
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Send",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 