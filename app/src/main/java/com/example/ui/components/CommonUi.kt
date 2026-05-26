package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.ClaySecondary
import com.example.ui.theme.WarmGold

import androidx.compose.foundation.BorderStroke

@Composable
fun HomePilotCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Custom thin border for Clean Minimalism if NOT the primary color
    val resolvedBorder = border ?: BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
    )

    if (onClick != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = resolvedBorder,
            content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = resolvedBorder,
            content = content
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority) {
        "High" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        "Medium" -> Pair(Color(0xFFFFF3CD), Color(0xFF856404))
        "Low" -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = priority,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CategoryIcon(category: String, size: Dp = 40.dp, contentColor: Color = MaterialTheme.colorScheme.onTertiary) {
    val (icon, bgColor) = when (category) {
        "Electricity" -> Pair(Icons.Default.Bolt, Color(0xFFFFD54F))
        "Water" -> Pair(Icons.Default.WaterDrop, Color(0xFF64B5F6))
        "Internet" -> Pair(Icons.Default.Wifi, Color(0xFF81C784))
        "Gas" -> Pair(Icons.Default.Propane, Color(0xFFFFB74D))
        "Rent" -> Pair(Icons.Default.Home, Color(0xFF90A4AE))
        "Subscriptions" -> Pair(Icons.Default.VideoCall, Color(0xFFE57373))
        "Insurance" -> Pair(Icons.Default.Shield, Color(0xFF4DB6AC))
        "Education" -> Pair(Icons.Default.School, Color(0xFFBA68C8) )
        "Groceries" -> Pair(Icons.Default.ShoppingCart, Color(0xFF81C784))
        "Fuel" -> Pair(Icons.Default.LocalGasStation, Color(0xFFFFB74D))
        "Medical" -> Pair(Icons.Default.MedicalServices, Color(0xFFF06292))
        "Shopping" -> Pair(Icons.Default.Celebration, Color(0xFFFF8A65))
        else -> Pair(Icons.Default.Folder, Color(0xFFCFD8DC))
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = Color(0xFF1E2421),
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/**
 * Custom Canvas drawing for a stunning, responsive spend pie chart
 */
@Composable
fun SpendPieChart(
    categories: List<String>,
    values: List<Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = values.sum()
    if (total <= 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data Available")
        }
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            var startAngle = -90f
            values.forEachIndexed { i, value ->
                val angle = (value / total * 360f).toFloat()
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = true
                )
                startAngle += angle
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            categories.forEachIndexed { i, cat ->
                val pct = if (total > 0) (values[i] / total * 100).toInt() else 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.getOrElse(i) { Color.Gray })
                    )
                    Text(
                        text = "$cat ($pct%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
