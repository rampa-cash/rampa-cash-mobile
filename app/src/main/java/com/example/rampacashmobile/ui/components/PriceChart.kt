package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.rampacashmobile.viewmodel.InvestmentViewModel

@Composable
fun PriceChart(
    pricePoints: List<InvestmentViewModel.PricePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4CAF50)
) {
    if (pricePoints.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val minPrice = pricePoints.minOf { it.price }
        val maxPrice = pricePoints.maxOf { it.price }
        val priceRange = maxPrice - minPrice

        if (priceRange == 0f) return@Canvas

        val path = Path()

        pricePoints.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (pricePoints.size - 1)) * (width - 2 * padding)
            val y = height - padding - ((point.price - minPrice) / priceRange) * (height - 2 * padding)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )
    }
}
