package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.healthtracker.pages.HealthTrackerScreen
import com.example.healthtracker.services.steps.StepForegroundService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicia o serviço de passos em background
        StepForegroundService.start(this)

        setContent {
            MaterialTheme {
                RequestPermissions()
                HealthTrackerScreen()
            }
        }
    }
    // Não para o serviço aqui mesmo com a app fechada
}

// ─────────────────────────────────────────────
//  PEDE AS PERMISSÕES NECESSÁRIAS
// ─────────────────────────────────────────────
@Composable
fun RequestPermissions() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted = permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        val notifGranted    = permissions[android.Manifest.permission.POST_NOTIFICATIONS]   ?: false
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                android.Manifest.permission.ACTIVITY_RECOGNITION,  // sensor de passos
                android.Manifest.permission.POST_NOTIFICATIONS      // notificação (Android 13+)
            )
        )
    }
}