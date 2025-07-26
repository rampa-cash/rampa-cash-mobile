package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SupportedToken(
    val name: String,
    val symbol: String,
    val mintAddress: String,
    val decimals: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenSelectionDropdown(
    selectedToken: SupportedToken,
    availableTokens: List<SupportedToken>,
    onTokenSelected: (SupportedToken) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selectedToken.symbol} - ${selectedToken.name}",
            onValueChange = { },
            readOnly = true,
            label = { Text("Select Token") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableTokens.forEach { token ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Token symbol as a simple colored circle
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        1.dp,
                                        when (token.symbol) {
                                            "EURC" -> Color(0xFF4CAF50)
                                            "USDC" -> Color(0xFF2196F3)
                                            else -> Color.Gray
                                        },
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = token.symbol.take(1),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (token.symbol) {
                                        "EURC" -> Color(0xFF4CAF50)
                                        "USDC" -> Color(0xFF2196F3)
                                        else -> Color.Gray
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${token.symbol} - ${token.name}")
                        }
                    },
                    onClick = {
                        onTokenSelected(token)
                        expanded = false
                    }
                )
            }
        }
    }
} 