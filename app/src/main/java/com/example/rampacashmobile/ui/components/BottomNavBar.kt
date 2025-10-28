package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

    // Aqua/Cyan color for active state
    val activeColor = Color(0xFF23d3d5) // flow-aqua
    val inactiveColor = Color(0xFFf1f2f3) // text normal
    // Match the theme gradient colors
    val backgroundDark = Color(0xFF000000) // Pure black to match gradient
    val borderDark = Color(0xFF1a1c1e) // outline background-outline
    val buttonBackgroundDark = Color(0xFF26292c) // background dim
    val buttonBorderDark = Color(0xFF323639) // outline ii

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundDark)
            .border(BorderStroke(1.dp, borderDark), RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavigationItems.forEach { destination ->
                val isSelected = currentRoute == destination.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (destination.route == "dashboard") {
                                navController.navigate("dashboard") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(destination.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                ) {
                    if (destination == NavigationDestination.Send) {
                        // Floating Send Button in the center
                        val isSendSelected = currentRoute == "send"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background based on selection
                                if (isSendSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                activeColor,
                                                shape = CircleShape
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(
                                                BorderStroke(1.dp, buttonBorderDark),
                                                CircleShape
                                            )
                                            .background(
                                                buttonBackgroundDark,
                                                shape = CircleShape
                                            )
                                    )
                                }

                                // Icon and label
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isSendSelected) Color.White else inactiveColor
                                    )
                                    Text(
                                        text = destination.title,
                                        fontSize = 12.sp,
                                        color = if (isSendSelected) Color.White else inactiveColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // Regular navigation items
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Top border for selected items
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.TopCenter)
                                        .background(activeColor)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (destination.customIconRes != null) {
                                        Icon(
                                            painter = painterResource(id = destination.customIconRes),
                                            contentDescription = destination.title,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) activeColor else inactiveColor
                                        )
                                    } else if (destination.icon != null) {
                                        Icon(
                                            imageVector = destination.icon!!,
                                            contentDescription = destination.title,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) activeColor else inactiveColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }
} 