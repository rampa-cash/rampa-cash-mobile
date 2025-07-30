package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.res.painterResource
import com.example.rampacashmobile.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(
    title: String,
    navController: NavController,
    showBackButton: Boolean = false,
    showProfileButton: Boolean = true,
    showChatButton: Boolean = true
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigate("dashboard") }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back",
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (showProfileButton) {
                IconButton(
                    onClick = {
                        navController.navigate("profile")
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.account_avatar_icon),
                        contentDescription = "Profile",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        actions = {
            if (showChatButton) {
                IconButton(
                    onClick = {
                        // Navigate to chat screen when implemented
                        // navController.navigate("chat")
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Chat Support",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            if (showProfileButton) {
                IconButton(
                    onClick = {
                        navController.navigate("card")
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_credit_card),
                        contentDescription = "Card",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF111827)
        ),
        windowInsets = WindowInsets(0.dp), // Remove default window insets
        modifier = Modifier.padding(0.dp), // Remove any extra padding
        scrollBehavior = null // Remove any scroll behavior padding
    )
} 