package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.TopNavBar
import com.example.rampacashmobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Navigation with Profile Button
        TopNavBar(
            title = "Learn & Earn",
            navController = navController,
            showBackButton = false,
            showProfileButton = true,
            showChatButton = false
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 90.dp), // Add bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Bonks Earned Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Bonks earned: 450",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // Toggle switch placeholder
                    Card(
                        modifier = Modifier.size(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {}
                }
            }

            // Earn BONK Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Earn BONK!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "for every module",
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.bonk1_logo),
                        contentDescription = "BONK Logo",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Learning Modules
            LearnModuleCard(
                title = "Basics of Investing",
                bonkReward = 50,
                progress = 0.6f,
                isCompleted = false,
                submodules = listOf(
                    "What is Investing?",
                    "Types of Investments", 
                    "Setting Investment Goals"
                )
            )

            LearnModuleCard(
                title = "Risk Management",
                bonkReward = 50,
                progress = 0f,
                isCompleted = false,
                submodules = listOf(
                    "Understanding Risk vs Reward",
                    "Risk Assessment Tools",
                    "Portfolio Risk Management"
                )
            )

            LearnModuleCard(
                title = "Diversification",
                bonkReward = 50,
                progress = 0f,
                isCompleted = false,
                submodules = listOf(
                    "Asset Class Diversification",
                    "Geographic Diversification",
                    "Time Diversification"
                )
            )

            // Exchange Anytime Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Exchange anytime",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bonk1_logo),
                            contentDescription = "BONK Logo",
                            modifier = Modifier.size(48.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Exchange arrow",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.usdc_logo),
                            contentDescription = "USDC Logo",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnModuleCard(
    title: String,
    bonkReward: Int,
    progress: Float,
    isCompleted: Boolean,
    submodules: List<String>
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$bonkReward BONK",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF374151),
            )
            
            if (isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        fontSize = 16.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Completed",
                        fontSize = 14.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
            
            // Submodules section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "Submodules:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    submodules.forEach { submodule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { /* Handle submodule click */ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•",
                                fontSize = 16.sp,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = submodule,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Expand/Collapse indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isExpanded) "Tap to collapse" else "Tap to view submodules",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}