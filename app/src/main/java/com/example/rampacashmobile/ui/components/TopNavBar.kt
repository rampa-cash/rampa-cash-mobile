package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.painterResource
import com.example.rampacashmobile.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TopNavBar(
    navController: NavController,
    showBackButton: Boolean = false,
    onProfileClick: () -> Unit = { navController.navigate("profile") }
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(77.dp), // Status bar space
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Left: Profile icon or Back button
        if (showBackButton) {
            // Back button with circular background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF26292C), // --background/dim
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF62696F), // --outline/outline-i
                        shape = CircleShape
                    )
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.account_avatar_icon),
                    contentDescription = "Back",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Profile icon with circular background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF26292C), // --background/dim
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF62696F), // --outline/outline-i
                        shape = CircleShape
                    )
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.account_avatar_icon),
                    contentDescription = "Profile",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Right: Search icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF26292C), // --background/dim
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF62696F), // --outline/outline-i
                    shape = CircleShape
                )
                .clickable { /* TODO: Open search */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFFFFFDF8), // --text/normal
                modifier = Modifier.size(16.dp)
            )
        }
    }
    }
}