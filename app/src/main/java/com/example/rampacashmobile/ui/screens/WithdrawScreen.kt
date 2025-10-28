package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Screen state
enum class WithdrawScreenState {
    AMOUNT_INPUT,
    COUNTRY_SELECTION
}

@Composable
fun WithdrawScreen(
    navController: NavController
) {
    var screenState by remember { mutableStateOf(WithdrawScreenState.AMOUNT_INPUT) }
    var amount by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    val quickAmounts = listOf("50", "100", "500")
    
    // Calculate fees
    val fees = if (amount.isNotEmpty()) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        amountValue * 0.0005 // 0.05% fee
    } else {
        0.0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (screenState) {
            WithdrawScreenState.AMOUNT_INPUT -> {
                AmountInputScreen(
                    navController = navController,
                    amount = amount,
                    onAmountChange = { amount = it },
                    quickAmounts = quickAmounts,
                    fees = fees,
                    onContinue = { screenState = WithdrawScreenState.COUNTRY_SELECTION }
                )
            }
            WithdrawScreenState.COUNTRY_SELECTION -> {
                CountrySelectionScreen(
                    navController = navController,
                    selectedCountry = selectedCountry,
                    onCountryChange = { selectedCountry = it },
                    countryDropdownExpanded = countryDropdownExpanded,
                    onDropdownExpandedChange = { countryDropdownExpanded = it },
                    onBackToAmount = { screenState = WithdrawScreenState.AMOUNT_INPUT }
                )
            }
        }
    }
}

@Composable
private fun AmountInputScreen(
    navController: NavController,
    amount: String,
    onAmountChange: (String) -> Unit,
    quickAmounts: List<String>,
    fees: Double,
    onContinue: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .offset(x = 16.dp, y = 77.dp)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 145.dp, end = 16.dp)
        ) {
            // Large amount display with input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "€",
                    fontSize = 52.sp,
                    fontWeight = FontWeight(500), // Medium
                    color = Color(0xFF23D3D5), // --flow-aqua
                    letterSpacing = 0.sp
                )
                VerticalDivider(
                    color = Color(0xFF62696F), // --outline/outline-i
                    modifier = Modifier.height(70.dp)
                )
                // Editable amount field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (amount.isEmpty()) {
                        Text(
                            text = "0.00",
                            fontSize = 52.sp,
                            fontWeight = FontWeight(500), // Medium
                            color = Color(0xFF23D3D5).copy(alpha = 0.3f), // --flow-aqua
                            letterSpacing = 0.sp
                        )
                    }
                    // Hidden text field for input
                    BasicTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 52.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF23D3D5),
                            letterSpacing = 0.sp
                        ),
                        singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fees display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fees:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFDF8)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "0.05%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFF23D3D5) // --flow-aqua
                        )
                        Text(
                            text = "€${String.format("%.2f", fees)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFDF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick amount buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                quickAmounts.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .background(
                                Color(0xFF26292C), // --background/dim
                                RoundedCornerShape(99.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF62696F), // --outline/outline-i
                                shape = RoundedCornerShape(99.dp)
                            )
                            .clickable { onAmountChange(amt) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "€$amt",
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFDF8),
                            letterSpacing = 0.sp
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .background(
                            Color(0xFF26292C),
                            RoundedCornerShape(99.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF62696F),
                            shape = RoundedCornerShape(99.dp)
                        )
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Use Max",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFDF8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            Button(
                onClick = onContinue,
                enabled = amount.isNotEmpty() && amount.toDoubleOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amount.isNotEmpty()) Color(0xFF23D3D5) else Color(0xFF26292C), // Non-active
                    disabledContainerColor = Color(0xFF26292C)
                ),
                shape = RoundedCornerShape(99.dp)
            ) {
                Text(
                    text = "Continue to Bank Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8)
                )
            }
        }
    }
}

@Composable
private fun CountrySelectionScreen(
    navController: NavController,
    selectedCountry: String?,
    onCountryChange: (String) -> Unit,
    countryDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onBackToAmount: () -> Unit
) {
    val countries = listOf(
        "United Kingdom",
        "Spain",
        "Germany",
        "France",
        "Netherlands",
        "Belgium",
        "Portugal",
        "Italy"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button - goes back to amount input screen
        IconButton(
            onClick = { onBackToAmount() },
            modifier = Modifier
                .offset(x = 16.dp, y = 77.dp)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 90.dp, end = 16.dp)
        ) {
            // Header
        Text(
                text = "Choose Destination",
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFDF8),
            modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(55.dp))

            // Country selection section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select your payout country",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8)
                )

                // Country dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onDropdownExpandedChange(true) }
                        .padding(horizontal = 12.dp, vertical = 18.dp)
                ) {
                    Row(
            modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFFFFFDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = selectedCountry ?: "Country",
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFFFDF8)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFFFFFDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = countryDropdownExpanded,
                    onDismissRequest = { onDropdownExpandedChange(false) }
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country, color = Color.White) },
                            onClick = {
                                onCountryChange(country)
                                onDropdownExpandedChange(false)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Withdraw button
            Button(
                onClick = { /* Navigate to bank details */ },
                enabled = selectedCountry != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCountry != null) Color(0xFF23D3D5) else Color(0xFF26292C),
                    disabledContainerColor = Color(0xFF26292C)
                ),
                shape = RoundedCornerShape(99.dp)
            ) {
        Text(
                    text = "Withdraw",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
