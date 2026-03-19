package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.pages.HealthTrackerApp
import com.example.healthtracker.services.steps.StepForegroundService
import com.example.healthtracker.services.user.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            val prefs by userViewModel.prefs.collectAsState()

            // Controla o serviço de passos com base na preferência do utilizador
            LaunchedEffect(prefs.notifSteps) {
                if (prefs.notifSteps) {
                    StepForegroundService.start(this@MainActivity)
                } else {
                    StepForegroundService.stop(this@MainActivity)
                }
            }

            RequestPermissions()
            HealthTrackerApp(userViewModel)
        }
    }
}

@Composable
fun RequestPermissions() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted = permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        val notifGranted    = permissions[android.Manifest.permission.POST_NOTIFICATIONS]   ?: false
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.POST_NOTIFICATIONS
        ))
    }
}