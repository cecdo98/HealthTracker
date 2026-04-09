package com.example.healthtracker.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel

@Composable
fun AppTheme(
    userViewModel: UserViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val prefs by userViewModel.prefs.collectAsState()
    val isDarkMode = prefs.darkMode
    val colors = if (isDarkMode) DarkColors else LightColors

    CompositionLocalProvider(
        LocalAppColors provides colors
    ) {
        MaterialTheme(
            colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme(),
            typography = Typography,
            content = content
        )
    }
}

// Modificador personalizado que vibra ao clicar
fun Modifier.hapticClick(
    userViewModel: UserViewModel,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val prefs by userViewModel.prefs.collectAsState()
    
    this.clickable {
        if (prefs.hapticEnabled) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
        onClick()
    }
}
