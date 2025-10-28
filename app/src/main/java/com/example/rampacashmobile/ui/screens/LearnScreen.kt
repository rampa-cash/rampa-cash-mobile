package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.ui.components.TopNavBar
import com.example.rampacashmobile.R
import com.example.rampacashmobile.viewmodel.LearnViewModel
import com.example.rampacashmobile.data.LearnModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    navController: NavController,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val modules by viewModel.modules.collectAsState()
    val totalBonksEarned by viewModel.totalBonksEarned.collectAsState()
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
                        text = "Total Bonks earned: $totalBonksEarned",
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

            // Learning Modules - Rendered dynamically from ViewModel
            modules.forEach { module ->
                LearnModuleCard(
                    module = module,
                    navController = navController
                )
            }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
    module: LearnModule,
    navController: NavController
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (module.submodules.isNotEmpty()) {
                    navController.navigate("submodules/${module.id}")
                }
            },
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
                    text = module.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${module.bonkReward} BONK",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { module.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF374151),
            )
            
            if (module.isCompleted) {
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
                    module.submodules.forEach { submodule ->
                        val isSubmoduleCompleted = module.completedSubmodules.contains(submodule.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    onClick = {
                                        if (submodule.lessons.isNotEmpty()) {
                                            navController.navigate("lesson/${module.id}/${submodule.id}")
                                        }
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSubmoduleCompleted) "✓" else "•",
                                fontSize = 16.sp,
                                color = if (isSubmoduleCompleted) Color(0xFF10B981) else Color(0xFF6B7280),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = submodule.title,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Expand/Collapse indicator
            if (module.submodules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
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
}