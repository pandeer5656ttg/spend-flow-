@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategorySpend
import com.example.model.DailySpend
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import java.util.Locale

@Composable
fun CategoryDonutChart(
  categorySpends: List<CategorySpend>,
  totalAmount: Double,
  currency: String = "$",
  modifier: Modifier = Modifier,
  chartSize: Dp = 180.dp,
  strokeWidth: Dp = 24.dp
) {
  var selectedCategory by remember { mutableStateOf<CategorySpend?>(null) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(categorySpends) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(chartSize)
        .testTag("donut_chart_container"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokePx = strokeWidth.toPx()
        val diameter = size.minDimension - strokePx
        val arcTopLeft = Offset(
          (size.width - diameter) / 2f,
          (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        if (categorySpends.isEmpty() || totalAmount <= 0.0) {
          // Empty placeholder ring
          drawArc(
            color = SlateBorder.copy(alpha = 0.4f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokePx)
          )
        } else {
          var startAngle = -90f
          for (item in categorySpends) {
            val sweepAngle = (item.percentage / 100f) * 360f * animationProgress.value
            if (sweepAngle > 0.5f) {
              drawArc(
                color = item.category.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
              )
              startAngle += (item.percentage / 100f) * 360f
            }
          }
        }
      }

      // Center summary label
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
      ) {
        val displayItem = selectedCategory
        if (displayItem != null) {
          Text(
            text = displayItem.category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
          Text(
            text = String.format(Locale.US, "%s%.2f", currency, displayItem.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = displayItem.category.color
          )
          Text(
            text = String.format(Locale.US, "%.1f%%", displayItem.percentage),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = "Total Spent",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format(Locale.US, "%s%.0f", currency, totalAmount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${categorySpends.size} categories",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Category Legend
    FlowRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      for (spend in categorySpends) {
        val isSelected = selectedCategory?.category == spend.category
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) spend.category.color.copy(alpha = 0.2f)
              else SlateSurfaceCard.copy(alpha = 0.6f)
            )
            .clickable {
              selectedCategory = if (isSelected) null else spend
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(spend.category.color)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = spend.category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = String.format(Locale.US, "%.0f%%", spend.percentage),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = spend.category.color
          )
        }
      }
    }
  }
}

@Composable
fun DailySpendBarChart(
  dailySpends: List<DailySpend>,
  currency: String = "$",
  modifier: Modifier = Modifier
) {
  val maxAmount = (dailySpends.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(50.0)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
      .padding(16.dp)
      .testTag("daily_bar_chart")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Daily Spending Trend",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Peak: ${currency}${String.format(Locale.US, "%.0f", maxAmount)}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      for (item in dailySpends) {
        val barFraction = (item.amount / maxAmount).toFloat().coerceIn(0.04f, 1f)
        val isHighest = item.amount >= maxAmount && item.amount > 0

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Bottom,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          if (isHighest) {
            Text(
              text = "${currency}${item.amount.toInt()}",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 2.dp)
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth(0.55f)
              .fillMaxHeight(barFraction * 0.75f)
              .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
              .background(
                if (isHighest) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
              )
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = item.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighest) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun BudgetProgressBar(
  categoryName: String,
  categoryColor: Color,
  spentAmount: Double,
  limitAmount: Double,
  currency: String = "$",
  modifier: Modifier = Modifier
) {
  val ratio = if (limitAmount > 0) (spentAmount / limitAmount).toFloat() else 0f
  val progress = ratio.coerceIn(0f, 1f)
  val isExceeded = spentAmount > limitAmount
  val isWarning = ratio >= 0.8f && !isExceeded

  val progressColor = when {
    isExceeded -> ExpenseCoral
    isWarning -> MaterialTheme.colorScheme.tertiary
    else -> categoryColor
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(categoryColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = categoryName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = String.format(Locale.US, "%s%.0f", currency, spentAmount),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = progressColor
        )
        Text(
          text = String.format(Locale.US, " / %s%.0f", currency, limitAmount),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(SlateBorder.copy(alpha = 0.5f))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress)
          .fillMaxHeight()
          .clip(RoundedCornerShape(4.dp))
          .background(progressColor)
      )
    }

    if (isExceeded) {
      Text(
        text = String.format(Locale.US, "Exceeded by %s%.2f!", currency, spentAmount - limitAmount),
        style = MaterialTheme.typography.labelSmall,
        color = ExpenseCoral,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}
