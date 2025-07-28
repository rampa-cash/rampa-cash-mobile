package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            // Empty title for cleaner look
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        actions = {
            if (showChatButton) {
                IconButton(
                    onClick = {
                        // Navigate to chat screen when implemented
                        // navController.navigate("chat")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Chat Support",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (showProfileButton) {
                IconButton(
                    onClick = {
                        navController.navigate("profile")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(20.dp)
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