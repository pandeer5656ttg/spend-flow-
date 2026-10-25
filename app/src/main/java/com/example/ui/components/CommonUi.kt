package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseCategory
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MetricSummaryCard(
  title: String,
  amount: Double,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  currency: String = "$",
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = String.format(Locale.US, "%s%.2f", currency, amount),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun ProBadge(
  isPro: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundBrush = if (isPro) {
    Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
  } else {
    Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(backgroundBrush)
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 5.dp)
  ) {
    Icon(
      imageVector = if (isPro) Icons.Default.WorkspacePremium else Icons.Default.Bolt,
      contentDescription = "Subscription status",
      tint = Color.White,
      modifier = Modifier.size(15.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = if (isPro) "PRO ACTIVE" else "GET PRO",
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
      letterSpacing = 0.5.sp
    )
  }
}

fun getCategoryIcon(categoryName: String): ImageVector {
  return when (categoryName.uppercase()) {
    "FOOD" -> Icons.Default.Restaurant
    "HOUSING" -> Icons.Default.Home
    "TRANSPORT" -> Icons.Default.DirectionsCar
    "SUBSCRIPTIONS" -> Icons.Default.Subscriptions
    "ENTERTAINMENT" -> Icons.Default.Movie
    "UTILITIES" -> Icons.Default.Bolt
    "HEALTHCARE" -> Icons.Default.FitnessCenter
    "SHOPPING" -> Icons.Default.ShoppingBag
    else -> Icons.Default.Category
  }
}

@Composable
fun CategoryIconBox(
  category: ExpenseCategory,
  size: Int = 42
) {
  Box(
    modifier = Modifier
      .size(size.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(category.color.copy(alpha = 0.15f))
      .border(1.dp, category.color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = getCategoryIcon(category.name),
      contentDescription = category.displayName,
      tint = category.color,
      modifier = Modifier.size((size * 0.52).dp)
    )
  }
}

@Composable
fun EmptyStateCard(
  message: String,
  tip: String,
  icon: ImageVector = Icons.Default.ReceiptLong,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = tip,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
