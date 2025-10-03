package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MainViewModel? = null,
    web3AuthManager: Web3AuthManager? = null,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback? = null
) {
    val viewState = viewModel?.viewState?.collectAsState()?.value

    // Create display user info from available data sources
    val displayUserInfo = remember(viewState) {
        when {
            // If we have a complete user profile, use it
            viewState?.userAddress != null -> UserDisplayInfo(
                name = viewState.userLabel ?: "Rampa User",
                email = "Not available", // Remove reference to non-existent email property
                phone = "Not available", // Remove reference to non-existent phoneNumber property
                walletAddress = viewState.fullAddressForCopy ?: viewState.userAddress,
                authProvider = "none" // Remove reference to non-existent authProvider property
            )
            // Fallback for edge cases
            else -> UserDisplayInfo(
                name = "Rampa User",
                email = "Not available",
                phone = "Not available",
                walletAddress = "Not connected",
                authProvider = "none"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Rampa Account",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827)
            )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 90.dp), // Add bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Information Section
            UserInformationSection(userInfo = displayUserInfo)

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 40.dp),
                thickness = 1.dp,
                color = Color(0xFF334155)
            )
            
            // Action Buttons Section
            ActionButtonsSection(
                navController = navController,
                viewModel = viewModel,
                web3AuthManager = web3AuthManager,
                web3AuthCallback = web3AuthCallback,
                viewState = viewState
            )
        }
    }
}

// Updated data class for user display information
data class UserDisplayInfo(
    val name: String,
    val email: String,
    val phone: String,
    val walletAddress: String,
    val authProvider: String
)

@Composable
private fun UserInformationSection(userInfo: UserDisplayInfo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User Name
        UserInfoItem(
            icon = Icons.Default.Person,
            label = "Name",
            value = userInfo.name
        )
        
        // Wallet Address (most important for crypto users)
        if (userInfo.walletAddress.isNotBlank() && userInfo.walletAddress != "Not connected") {
            UserInfoItem(
                icon = Icons.Default.AccountBox, // Using AccountBox instead - it's available
                label = "Wallet Address",
                value = if (userInfo.walletAddress.length > 20) {
                    "${userInfo.walletAddress.take(8)}...${userInfo.walletAddress.takeLast(8)}"
                } else {
                    userInfo.walletAddress
                }
            )
        }

        // Phone Number (if available)
        if (userInfo.phone.isNotBlank() && userInfo.phone != "Not available") {
            UserInfoItem(
                icon = Icons.Default.Phone,
                label = "Mobile Number",
                value = userInfo.phone
            )
        }

        // Email (if available)
        if (userInfo.email.isNotBlank() && userInfo.email != "Not available") {
            UserInfoItem(
                icon = Icons.Default.Email,
                label = "Email Address",
                value = userInfo.email
            )
        }

        // Authentication Method
        UserInfoItem(
            icon = Icons.Default.Lock, // Changed from Security
            label = "Authentication",
            value = when (userInfo.authProvider) {
                "google" -> "Google Sign-In"
                "apple" -> "Apple Sign-In"
                "sms" -> "Phone Number (SMS)"
                "web3auth" -> "Web3Auth"
                "wallet" -> "Mobile Wallet"
                else -> "Unknown"
            }
        )
    }
}

@Composable
private fun UserInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Info content
        Column {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    navController: NavController,
    viewModel: MainViewModel?,
    web3AuthManager: Web3AuthManager?,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback?,
    viewState: Any?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // About Button
        ActionButton(
            icon = Icons.Default.Info,
            text = "About rampa",
            onClick = {
                navController.navigate("about")
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sign Out Button
        ActionButton(
            icon = Icons.AutoMirrored.Filled.ExitToApp, // Changed from Icons.Default.ExitToApp
            text = "Sign Out",
            textColor = Color(0xFFEF4444),
            iconTint = Color(0xFFEF4444),
            onClick = {
                // Handle logout/disconnect
                if (web3AuthManager != null && web3AuthCallback != null) {
                    web3AuthManager.logout(web3AuthCallback)
                } else {
                    viewModel?.disconnect()
                }
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.White,
    iconTint: Color = Color(0xFF94A3B8),
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(
            horizontal = 0.dp,
            vertical = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon container
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Button text
            Text(
                text = text,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
