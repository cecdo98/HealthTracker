package com.example.healthtracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
//  MODELO DE CORES
// ─────────────────────────────────────────────
data class AppColors(
    val background:   Color,
    val card:         Color,
    val primary:      Color,
    val textPrimary:  Color,
    val textSecondary: Color,
    val navSelected:  Color,
    val navUnselected: Color,
    val inputBorder:  Color,
    val inputBorderFocused: Color,
    val divider:      Color,
    val switchTrackOn: Color,
    val error:        Color,
    val isDark:       Boolean
)

// ─────────────────────────────────────────────
//  TEMA CLARO
// ─────────────────────────────────────────────
val LightColors = AppColors(
    background          = Color(0xFFF5F7FA),
    card                = Color(0xFFFFFFFF),
    primary             = Color(0xFF4A90E2),
    textPrimary         = Color(0xFF2D3748),
    textSecondary       = Color(0xFF718096),
    navSelected         = Color(0xFF4A90E2),
    navUnselected       = Color(0xFFB0BEC5),
    inputBorder         = Color(0xFFCBD5E0),
    inputBorderFocused  = Color(0xFF4A90E2),
    divider             = Color(0xFFF0F0F0),
    switchTrackOn       = Color(0xFF4A90E2),
    error               = Color(0xFFE53E3E),
    isDark              = false
)

// ─────────────────────────────────────────────
//  TEMA ESCURO
// ─────────────────────────────────────────────
val DarkColors = AppColors(
    background          = Color(0xFF1A202C),
    card                = Color(0xFF2D3748),
    primary             = Color(0xFF63B3ED),
    textPrimary         = Color(0xFFF7FAFC),
    textSecondary       = Color(0xFFA0AEC0),
    navSelected         = Color(0xFF63B3ED),
    navUnselected       = Color(0xFF718096),
    inputBorder         = Color(0xFF4A5568),
    inputBorderFocused  = Color(0xFF63B3ED),
    divider             = Color(0xFF4A5568),
    switchTrackOn       = Color(0xFF63B3ED),
    error               = Color(0xFFFC8181),
    isDark              = true
)

// ─────────────────────────────────────────────
//  COMPOSITION LOCAL — acesso global às cores
// ─────────────────────────────────────────────
val LocalAppColors = staticCompositionLocalOf { LightColors }

// ─────────────────────────────────────────────
//  ATALHO — AppTheme.colors.background, etc.
// ─────────────────────────────────────────────
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}