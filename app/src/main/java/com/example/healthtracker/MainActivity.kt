package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.healthtracker.pages.HealthTrackerApp
import com.example.healthtracker.services.steps.StepForegroundService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StepForegroundService.start(this)

        setContent {
            RequestPermissions()
            HealthTrackerApp()
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