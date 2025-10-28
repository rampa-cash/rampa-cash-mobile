package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
import com.example.rampacashmobile.ui.components.TopNavBar
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
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(true) }

    // Get user info
    val userName = viewState?.userLabel ?: "Maria Martinez"
    val userEmail = "sample@mail.com"
    val userPhone = "+44 (123) 456 7890"
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Top Navigation
            Spacer(modifier = Modifier.height(24.dp))
            
            // Page Title
            Text(
                text = "My Account",
                fontSize = 16.sp,
                fontWeight = FontWeight(400), // Regular
                color = Color(0xFFFFFDF8),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.account_avatar_icon),
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Name
            Text(
                text = userName,
                fontSize = 32.sp,
                fontWeight = FontWeight(500), // Medium
                color = Color(0xFFFFFDF8),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contact Info Card
            ContactInfoCard(
                email = userEmail,
                phone = userPhone
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Referrals Card
            ReferralsCard()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings Section
    Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Support",
                    onClick = { /* TODO: Navigate to support */ }
                )
                
                SettingsItemWithToggle(
                    icon = Icons.Default.Notifications,
                    title = "Notification",
                    isEnabled = notificationsEnabled,
                    onToggle = { notificationsEnabled = !notificationsEnabled }
                )
                
                SettingsItemWithToggle(
                    icon = Icons.Default.Settings,
                    title = "Dark Theme",
                    isEnabled = darkThemeEnabled,
                    onToggle = { darkThemeEnabled = !darkThemeEnabled }
                )
                
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Terms & Privacy",
                    onClick = { /* TODO: Navigate to terms */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Rampa",
                    onClick = { navController.navigate("about") }
                )
                
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete my account",
                    onClick = { /* TODO: Show delete confirmation */ }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            Button(
                onClick = {
                    if (web3AuthManager != null && web3AuthCallback != null) {
                        web3AuthManager.logout(web3AuthCallback)
                    } else {
                        viewModel?.disconnect()
                    }
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26292C), // --background/non-active
                    contentColor = Color(0xFFFFFDF8) // --text/normal
                ),
                shape = RoundedCornerShape(99.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "Log out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    lineHeight = (16 * 1.5).sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "support@rampa.cash",
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFFF1F2F3), // --text/normal-2
                    lineHeight = (14 * 1.14).sp
                )
                Text(
                    text = "App version 1.0.0 (beta)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFFF1F2F3), // --text/normal-2
                    lineHeight = (14 * 1.14).sp
                )
            }
            
            Spacer(modifier = Modifier.height(90.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun ContactInfoCard(
    email: String,
    phone: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFF1F2F3),
                            lineHeight = (14 * 1.4).sp
                        )
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFDF8),
                            lineHeight = (14 * 1.4).sp
                        )
                    }
                }
            Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Action",
                    tint = Color(0xFFFFFDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Phone row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
            Text(
                            text = "Phone",
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFF1F2F3),
                            lineHeight = (14 * 1.4).sp
                        )
            Text(
                            text = phone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFDF8),
                            lineHeight = (14 * 1.4).sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Action",
                    tint = Color(0xFFFFFDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReferralsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { /* TODO: Navigate to referrals */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Referrals",
                tint = Color(0xFFFFFDF8),
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Referrals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFF1F2F3),
                    lineHeight = (14 * 1.4).sp
                )
                Text(
                    text = "Invite & earn rewards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8),
                    lineHeight = (14 * 1.4).sp
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFFFFDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFDF8),
                lineHeight = (16 * 1.14).sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SettingsItemWithToggle(
    icon: ImageVector,
    title: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon container
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8),
                    lineHeight = (16 * 1.14).sp)
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF23D3D5)
                )
            )
        }
    }
}