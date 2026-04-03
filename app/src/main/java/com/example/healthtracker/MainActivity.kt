package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.pages.HealthTrackerApp
import com.example.healthtracker.services.steps.StepForegroundService
import com.example.healthtracker.services.user.UserViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val userViewModel: UserViewModel = viewModel()
            val prefs by userViewModel.prefs.collectAsState()

            // O serviço agora tenta iniciar sempre para garantir a contagem.
            // A lógica de "esconder" a notificação será tratada dentro do serviço.
            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    StepForegroundService.start(this@MainActivity)
                }
            }

            RequestPermissions {
                // Ao aceitar as permissões, iniciamos o serviço imediatamente
                StepForegroundService.start(this@MainActivity)
            }
            
            HealthTrackerApp(userViewModel, windowSizeClass)
        }
    }
}

@Composable
fun RequestPermissions(onGranted: () -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        if (activityGranted) onGranted()
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        ))
    }
}
