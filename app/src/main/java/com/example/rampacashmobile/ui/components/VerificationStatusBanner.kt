package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Verification status banner that shows profile completion reminders
 * 
 * This banner appears when users have incomplete profiles and need to complete
 * their verification to access financial operations.
 */
@Composable
fun VerificationStatusBanner(
    verificationStatus: String?,
    userStatus: String?,
    onCompleteProfileClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    // Only show banner for users who need verification
    if (verificationStatus != "PENDING_VERIFICATION" || userStatus == "SUSPENDED") {
        return
    }

    var isDismissed by remember { mutableStateOf(false) }
    
    if (isDismissed) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD) // Light yellow background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Verification required",
                tint = Color(0xFF856404), // Dark yellow
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Complete Your Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Complete your profile to access all features including transactions and wallet operations.",
                    fontSize = 14.sp,
                    color = Color(0xFF856404),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCompleteProfileClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF856404)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Complete Profile",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    TextButton(
                        onClick = { isDismissed = true; onDismiss() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF856404)
                        )
                    ) {
                        Text(
                            text = "Later",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact verification status indicator for the app bar
 */
@Composable
fun VerificationStatusIndicator(
    verificationStatus: String?,
    userStatus: String?,
    onCompleteProfileClick: () -> Unit
) {
    if (verificationStatus != "PENDING_VERIFICATION" || userStatus == "SUSPENDED") {
        return
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCompleteProfileClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Verification required",
                tint = Color(0xFF856404),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Complete profile to access all features",
                fontSize = 12.sp,
                color = Color(0xFF856404),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
