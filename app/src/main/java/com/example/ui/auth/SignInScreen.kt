package com.example.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private const val TAG = "SignInScreen"

private data class LoginFeature(
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val title: String,
  val subtitle: String,
  val accentColor: Color
)

@Composable
fun SignInScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var selectedFeatureIndex by remember { mutableIntStateOf(0) }

  val features = remember {
    listOf(
      LoginFeature(
        icon = Icons.Default.Autorenew,
        title = "Automated Monthly Logging",
        subtitle = "Never enter recurring bills manually. SpendFlow registers subscriptions right on their billing date.",
        accentColor = EmeraldLight
      ),
      LoginFeature(
        icon = Icons.Default.PieChart,
        title = "Interactive Data Visualizations",
        subtitle = "Category donut charts, 7-day spending velocity, and real-time cashflow analytics.",
        accentColor = Color(0xFF38BDF8)
      ),
      LoginFeature(
        icon = Icons.Default.Savings,
        title = "Smart Guardrails & Budgets",
        subtitle = "Category limits, overspend alerts, and net savings rate tracking against your income.",
        accentColor = AccentGold
      )
    )
  }

  fun onSignInWithGoogleClicked() {
    val activity = context as? Activity ?: return
    isLoading = true
    errorMessage = null

    coroutineScope.launch {
      try {
        val googleIdOption = GetSignInWithGoogleOption.Builder(
          context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val credentialManager = CredentialManager.create(activity)
        val result = credentialManager.getCredential(
          request = request,
          context = activity
        )

        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
          credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken
          val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
          Firebase.auth.signInWithCredential(firebaseCredential)
            .addOnFailureListener { e ->
              Log.e(TAG, "Firebase sign-in failed", e)
              errorMessage = e.localizedMessage ?: "Authentication failed. Please try again."
              isLoading = false
            }
        } else {
          errorMessage = "Unexpected credential format returned"
          isLoading = false
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w(TAG, "Google Sign-In was cancelled by user or Play Services", e)
        isLoading = false
      } catch (e: Exception) {
        Log.e(TAG, "Credential Manager error", e)
        errorMessage = e.localizedMessage ?: "Unable to complete Google Sign-In"
        isLoading = false
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF070B14),
            Color(0xFF0F172A),
            Color(0xFF131D31)
          )
        )
      )
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Hero Branding & Glowing Logo
      item {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Ambient Glowing Card behind App Logo
          Box(
            modifier = Modifier
              .size(100.dp)
              .shadow(24.dp, shape = CircleShape, ambientColor = EmeraldPrimary, spotColor = EmeraldLight)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    Color(0xFF1E293B),
                    Color(0xFF0F172A)
                  )
                )
              )
              .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                  listOf(EmeraldLight, Color(0xFF059669), AccentGold)
                ),
                shape = CircleShape
              )
              .padding(14.dp),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.app_logo),
              contentDescription = "SpendFlow Logo",
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Brand Title
          Text(
            text = "SpendFlow",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          Surface(
            color = EmeraldPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
          ) {
            Text(
              text = "SMART FINANCIAL MANAGEMENT",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Automate your recurring bills, track daily expenses, and build wealth with clear visual analytics.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
        }
      }

      // Feature Showcase Card with Tabs
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard.copy(alpha = 0.85f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            // Feature selector pills
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              features.forEachIndexed { index, feature ->
                val isSelected = selectedFeatureIndex == index
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      if (isSelected) feature.accentColor.copy(alpha = 0.2f)
                      else Color(0xFF0F172A).copy(alpha = 0.6f)
                    )
                    .border(
                      width = if (isSelected) 1.5.dp else 0.dp,
                      color = if (isSelected) feature.accentColor else Color.Transparent,
                      shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedFeatureIndex = index }
                    .padding(vertical = 10.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = if (isSelected) feature.accentColor else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentFeature = features[selectedFeatureIndex]
            Row(verticalAlignment = Alignment.Top) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(currentFeature.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = currentFeature.icon,
                  contentDescription = null,
                  tint = currentFeature.accentColor,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = currentFeature.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = currentFeature.subtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF94A3B8)
                )
              }
            }
          }
        }
      }

      // Trust & Security Pillars
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TrustBadge(icon = Icons.Default.Lock, text = "256-bit Encrypted")
          TrustBadge(icon = Icons.Default.CloudDone, text = "Google Cloud")
          TrustBadge(icon = Icons.Default.Speed, text = "Offline Room Cache")
        }
      }

      // Error Banner
      item {
        AnimatedVisibility(
          visible = errorMessage != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      // Official Google Sign-In Button
      item {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Button(
            onClick = { onSignInWithGoogleClicked() },
            enabled = !isLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .shadow(8.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color.Black)
              .testTag("google_sign_in_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = Color(0xFF1F1F1F),
              disabledContainerColor = Color.White.copy(alpha = 0.7f),
              disabledContentColor = Color(0xFF1F1F1F).copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = EmeraldPrimary,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Connecting securely...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F1F1F)
              )
            } else {
              // Official 4-color Google G Vector
              Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F),
                letterSpacing = 0.2.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Seamless Google Sign-In • No password required",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "By continuing, you agree to SpendFlow's Terms of Service & Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun TrustBadge(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF1E293B).copy(alpha = 0.6f))
      .padding(horizontal = 8.dp, vertical = 5.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = EmeraldLight,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF94A3B8),
      fontSize = 10.sp
    )
  }
}
