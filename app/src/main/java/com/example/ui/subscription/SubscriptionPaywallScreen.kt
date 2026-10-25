package com.example.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubscriptionTier
import com.example.model.UserProfile
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard

@Composable
fun SubscriptionPaywallScreen(
  userProfile: UserProfile?,
  onSubscribe: (SubscriptionTier) -> Unit,
  onApplyPromo: (String) -> Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTier by remember { mutableStateOf(SubscriptionTier.PRO_ANNUAL) }
  var promoCode by remember { mutableStateOf("") }
  var promoMessage by remember { mutableStateOf<String?>(null) }
  var isPromoSuccess by remember { mutableStateOf(false) }
  var showPurchaseConfirmDialog by remember { mutableStateOf(false) }

  val isCurrentlyPro = userProfile?.isPro == true

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("paywall_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close paywall",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        TextButton(onClick = {
          onSubscribe(SubscriptionTier.PRO_ANNUAL)
          promoMessage = "Purchases restored successfully!"
          isPromoSuccess = true
        }) {
          Text("Restore", style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    // Hero Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(AccentGold, Color(0xFFD97706))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = if (isCurrentlyPro) "You Are a Pro Member!" else "Unlock SpendFlow Pro",
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = if (isCurrentlyPro)
            "Active Plan: ${userProfile?.getTierEnum()?.title}. Unlimited access to automated recurring logging and advanced charts."
          else
            "Automate unlimited monthly recurring expenses, access full financial charts, and budget with confidence.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
    }

    // Pricing Tier Selector
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Select a Plan",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PricingCard(
          tier = SubscriptionTier.PRO_ANNUAL,
          price = "$39.99 / year",
          periodNote = "$3.33 / month • Billed annually",
          badge = "BEST VALUE (SAVE 33%)",
          isSelected = selectedTier == SubscriptionTier.PRO_ANNUAL,
          onSelect = { selectedTier = SubscriptionTier.PRO_ANNUAL }
        )

        PricingCard(
          tier = SubscriptionTier.PRO_MONTHLY,
          price = "$4.99 / month",
          periodNote = "Flexible monthly subscription",
          badge = null,
          isSelected = selectedTier == SubscriptionTier.PRO_MONTHLY,
          onSelect = { selectedTier = SubscriptionTier.PRO_MONTHLY }
        )

        PricingCard(
          tier = SubscriptionTier.PRO_LIFETIME,
          price = "$79.99 one-time",
          periodNote = "Pay once, own forever",
          badge = "LIFETIME ACCESS",
          isSelected = selectedTier == SubscriptionTier.PRO_LIFETIME,
          onSelect = { selectedTier = SubscriptionTier.PRO_LIFETIME }
        )
      }
    }

    // Features List
    item {
      Spacer(modifier = Modifier.height(24.dp))
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Everything in SpendFlow Pro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          ProFeatureRow("Unlimited Recurring Subscriptions & Auto-Logging")
          ProFeatureRow("Interactive Category Donut & Daily Trend Visualizations")
          ProFeatureRow("Custom Category Budget Targets & Overspend Alerts")
          ProFeatureRow("Export Financial Reports & CSV Summaries")
          ProFeatureRow("Cloud Sync & Real-time Cross Device Backup")
          ProFeatureRow("Priority 24/7 Financial App Support")
        }
      }
    }

    // Promo Code Field
    item {
      Spacer(modifier = Modifier.height(20.dp))
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Have a Promo or Referral Code?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = promoCode,
              onValueChange = { promoCode = it },
              placeholder = { Text("e.g. PROMO50 or SPENDFREE") },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("promo_code_input"),
              shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val ok = onApplyPromo(promoCode)
                if (ok) {
                  promoMessage = "Code accepted! Pro features activated."
                  isPromoSuccess = true
                } else {
                  promoMessage = "Invalid code. Try 'PROMO50' or 'SPENDFREE'."
                  isPromoSuccess = false
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("apply_promo_button")
            ) {
              Text("Apply")
            }
          }

          if (promoMessage != null) {
            Text(
              text = promoMessage ?: "",
              style = MaterialTheme.typography.labelSmall,
              color = if (isPromoSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }
      }
    }

    // CTA Button
    item {
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = { showPurchaseConfirmDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("subscribe_cta_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isCurrentlyPro) "Switch Plan to ${selectedTier.title}"
          else "Get ${selectedTier.title} - ${selectedTier.priceDisplay}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Recurring billing. Cancel anytime in Google Play Store settings. 100% money-back guarantee.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  // Simulated Google Play Billing Confirmation Sheet
  if (showPurchaseConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showPurchaseConfirmDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Confirm Subscription")
        }
      },
      text = {
        Column {
          Text(
            text = "Google Play Purchase Simulator",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Item: SpendFlow Pro (${selectedTier.title})\nPrice: ${selectedTier.priceDisplay} (${selectedTier.billingPeriod})\nAccount: ${userProfile?.email ?: "Current User"}\n\nAll premium features including automated monthly logging will be unlocked immediately.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSubscribe(selectedTier)
            showPurchaseConfirmDialog = false
            onDismiss()
          },
          modifier = Modifier.testTag("confirm_purchase_button")
        ) {
          Text("Subscribe & Pay")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPurchaseConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun PricingCard(
  tier: SubscriptionTier,
  price: String,
  periodNote: String,
  badge: String?,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else SlateBorder.copy(alpha = 0.5f)
  val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else SlateSurfaceCard

  Card(
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .clickable { onSelect() }
      .testTag("pricing_card_${tier.name.lowercase()}"),
    colors = CardDefaults.cardColors(containerColor = backgroundColor)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = tier.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = periodNote,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        RadioButton(
          selected = isSelected,
          onClick = { onSelect() },
          colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = price,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        if (badge != null) {
          Surface(
            color = AccentGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = badge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AccentGold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProFeatureRow(text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Icon(
      imageVector = Icons.Default.CheckCircle,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
