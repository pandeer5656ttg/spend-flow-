package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SpendFlowRepository
import com.example.ui.auth.SignInScreen
import com.example.ui.main.MainScreen
import com.example.ui.theme.SpendFlowTheme
import com.example.viewmodel.AuthState
import com.example.viewmodel.SpendFlowViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SpendFlowTheme {
        val context = LocalContext.current
        val viewModel: SpendFlowViewModel = viewModel {
          SpendFlowViewModel(SpendFlowRepository(context))
        }

        val authState by viewModel.authState.collectAsStateWithLifecycle()

        when (authState) {
          is AuthState.Loading -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
          }
          is AuthState.Unauthenticated -> {
            SignInScreen()
          }
          is AuthState.Authenticated -> {
            SpendFlowMainApp(viewModel = viewModel)
          }
        }
      }
    }
  }
}

@Composable
fun SpendFlowMainApp(viewModel: SpendFlowViewModel) {
  MainScreen(viewModel = viewModel)
}
