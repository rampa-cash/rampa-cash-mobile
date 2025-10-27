package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
import com.example.rampacashmobile.ui.components.TopNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(navController: NavController) {
    var expandedModules by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val modules = listOf(
        ModuleData(
            title = "Basic of Investing",
            bonkReward = 50,
            progress = 48f,
            progressPercent = 0.16f,
            submodules = listOf(
                "What is Investing?",
                "Type of Investments?",
                "Setting Investment Goals"
            )
        ),
        ModuleData(
            title = "Risk Management",
            bonkReward = 50,
            progress = 139f,
            progressPercent = 0.48f,
            submodules = listOf(
                "What is Investing?",
                "Type of Investments?",
                "Setting Investment Goals"
            )
        ),
        ModuleData(
            title = "Diversification",
            bonkReward = 50,
            progress = 48f,
            progressPercent = 0.16f,
            submodules = listOf(
                "What is Investing?",
                "Type of Investments?",
                "Setting Investment Goals"
            )
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 90.dp
            )
        ) {
            // Header with TopNavBar
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // TopNavBar
                TopNavBar(
                    navController = navController,
                    showBackButton = false
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Title and description (same item to reduce spacing)
            item {
                Text(
                    text = "Learn",
                    fontSize = 32.sp,
                    fontWeight = FontWeight(500), // Medium
                    color = Color(0xFFFFFDF8),
                    lineHeight = 32.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Empower your family's future with simple lessons on saving, investing, and stablecoins",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFFFFDF8),
                    lineHeight = (16 * 1.14).sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Total Bonks Earned Card
            item {
                TotalBonksCard(totalBonks = 374.10f)
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Learning Modules
            items(modules.size) { index ->
                val module = modules[index]
                LearnModuleCard(
                    module = module,
                    isExpanded = expandedModules.contains(module.title),
                    onToggleExpand = {
                        val newSet = expandedModules.toMutableSet()
                        if (newSet.contains(module.title)) {
                            newSet.remove(module.title)
                        } else {
                            newSet.add(module.title)
                        }
                        expandedModules = newSet.toSet()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Data classes
data class ModuleData(
    val title: String,
    val bonkReward: Int,
    val progress: Float,
    val progressPercent: Float,
    val submodules: List<String> = emptyList()
)

@Composable
private fun TotalBonksCard(totalBonks: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color(0xFF26292C), // --background/dim
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { /* TODO: Handle tap */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bonk logo
            Image(
                painter = painterResource(id = R.drawable.bonk1_logo),
                contentDescription = "Bonk Logo",
                modifier = Modifier.size(105.dp)
            )
            
            // Total Bonks info
    Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "£${"%.2f".format(totalBonks)}",
                    fontSize = 54.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFA9EABF), // --text/success
                    lineHeight = (54 * 1.4).sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Total Bonks earned",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFF1F2F3), // --text/normal-2
                    lineHeight = (16 * 1.14).sp
                )
            }
        }
    }
}

@Composable
private fun LearnModuleCard(
    module: ModuleData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onToggleExpand() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Bonk reward and View Submodules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bonk reward amount
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module.bonkReward.toString(),
                        fontSize = 52.sp,
                        fontWeight = FontWeight(500), // Medium
                        color = Color(0xFFFFFDF8),
                        lineHeight = 52.sp,
                        letterSpacing = 0.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "Bonk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = Color(0xFFFFFDF8),
                        lineHeight = (14 * 1.4).sp
                    )
                }
                
                // View Submodules button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Submodules",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = Color(0xFFFFFDF8),
                        lineHeight = (14 * 1.4).sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Module title and progress
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = module.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFFFFDF8),
                    lineHeight = (16 * 1.14).sp
                )
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFA3A8AE)) // --text/less-emphasis
                ) {
                    // Gradient progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(module.progressPercent)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF395DC0), // from
                                        Color(0xFF2A9ACB)  // to
                                    )
                                ),
                                shape = RoundedCornerShape(99.dp)
                            )
                    )
                }
            }
            
            // Expanded content (submodules)
            if (isExpanded && module.submodules.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Submodules:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = Color(0xFFFFFDF8),
                        lineHeight = (16 * 1.14).sp
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        module.submodules.forEach { submodule ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Check circle icon (14dp)
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Color(0xFFA9EABF), // Green check
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = submodule,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight(400), // Regular
                                    color = Color(0xFFFFFDF8),
                                    lineHeight = (14 * 1.4).sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}