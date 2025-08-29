package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.TopNavBar

@Composable
fun AboutScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        // Top Navigation
        TopNavBar(
            title = "About rampa",
            navController = navController,
            showBackButton = false,
            showProfileButton = false,
            showChatButton = false
        )
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 90.dp), // Add bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // About rampa Section
            AboutSection()
            
            // Legal Information Section  
            LegalInformationSection()
        }
    }
}

@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "About rampa",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "rampa is an On-chain remittance platform bringing Web3 and decentralized apps to real people, making it so simple that they don't realize they're using them.",
                color = Color(0xFFD1D5DB),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )
            
            Text(
                text = "Our mission is to turn stablecoin transfers into shared prosperity, empowering families to thrive trough financial education and a tokenized investment portfolio, so the money they send home today grows into collective wealth.",
                color = Color(0xFFD1D5DB),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )
            
            // Version
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Version",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "1.0.0",
                    color = Color(0xFFD1D5DB),
                    fontSize = 18.sp
                )
            }
            
            // Contact
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Contact",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "team@rampa.cash",
                    color = Color(0xFFD1D5DB),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LegalInformationSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Legal Information",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Terms of Service
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Terms of Service",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "By using rampa, you agree to our terms of service, which can be found on our website.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }
            
            // Privacy Policy
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Privacy Policy",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "We value your privacy. Please review our privacy policy to understand how we collect, use and protect your personal information.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }
            
            // Licenses
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Licenses",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "rampa uses open-source libraries and components under various licenses, including MIT, Apache 2.0 and BSD.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
} 