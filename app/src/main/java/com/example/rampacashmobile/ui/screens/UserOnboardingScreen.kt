package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.data.model.OnboardingData
import com.example.rampacashmobile.ui.theme.RampaColors
import com.example.rampacashmobile.viewmodel.MainViewModel

@Composable
fun UserOnboardingScreen(
    navController: NavController,
    viewModel: MainViewModel,
    authProvider: String, // "sms", "google", "apple"
    existingEmail: String = "",
    existingPhone: String = ""
) {
    var onboardingData by remember {
        mutableStateOf(
            OnboardingData(
                email = existingEmail,
                phoneNumber = existingPhone,
                authProvider = authProvider
            )
        )
    }

    var isSubmitting by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Title Section
            Text(
                text = "Complete your profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Help us personalize your Rampa experience",
                fontSize = 16.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // First Name
                    OutlinedTextField(
                        value = onboardingData.firstName,
                        onValueChange = { onboardingData = onboardingData.copy(firstName = it) },
                        label = { Text("First Name", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9945FF),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF9945FF)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Last Name
                    OutlinedTextField(
                        value = onboardingData.lastName,
                        onValueChange = { onboardingData = onboardingData.copy(lastName = it) },
                        label = { Text("Last Name", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9945FF),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF9945FF)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Email (if signed up with phone) or Phone (if signed up with email/social)
                    if (authProvider == "sms" && existingEmail.isEmpty()) {
                        OutlinedTextField(
                            value = onboardingData.email,
                            onValueChange = { onboardingData = onboardingData.copy(email = it) },
                            label = { Text("Email Address (Optional)", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF9945FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF9945FF)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if ((authProvider == "google" || authProvider == "apple") && existingPhone.isEmpty()) {
                        OutlinedTextField(
                            value = onboardingData.phoneNumber,
                            onValueChange = { onboardingData = onboardingData.copy(phoneNumber = it) },
                            label = { Text("Phone Number (Optional)", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("+1-2345678901", color = Color(0xFF94A3B8)) }, // Made placeholder more visible
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            },
                            supportingText = {
                                Text(
                                    "Format: +[country code]-[number]",
                                    color = Color(0xFF94A3B8), // Changed from dark Color(0xFF64748B) to lighter Color(0xFF94A3B8)
                                    fontSize = 12.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF9945FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF9945FF)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = {
                    if (onboardingData.firstName.isNotBlank() && onboardingData.lastName.isNotBlank()) {
                        isSubmitting = true
                        // TODO: Need to implement completeUserOnboarding method in MainViewModel
                        // viewModel.completeUserOnboarding(onboardingData)
                        navController.navigate("dashboard") {
                            popUpTo("user_onboarding/{authProvider}/{existingEmail}/{existingPhone}") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9945FF),
                    disabledContainerColor = Color(0xFF9945FF).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSubmitting && onboardingData.firstName.isNotBlank() && onboardingData.lastName.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "Continue to Rampa",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
