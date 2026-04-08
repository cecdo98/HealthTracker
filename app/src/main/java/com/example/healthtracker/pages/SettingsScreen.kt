package com.example.healthtracker.pages

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.R
import com.example.healthtracker.services.user.UserViewModel
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors

// ─────────────────────────────────────────────
//  RECEIVER
// ─────────────────────────────────────────────
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type    = intent.getStringExtra("type") ?: return
        val channel = "health_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(channel, "Lembretes de Saúde", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val (title, text, id) = when (type) {
            "water" -> Triple("💧 Hora de beber água!", "Não te esqueças de manter a hidratação.", 100)
            "mood"  -> Triple("😊 Como te sentes?", "Regista o teu estado emocional de hoje.", 101)
            else    -> return
        }
        val notification = NotificationCompat.Builder(context, channel)
            .setContentTitle(title).setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setAutoCancel(true).build()
        manager.notify(id, notification)
    }
}

// ─────────────────────────────────────────────
//  FREQUÊNCIAS
// ─────────────────────────────────────────────
val NOTIF_FREQUENCIES = linkedMapOf(
    "30m" to 30 * 60 * 1000L,
    "1h"  to 60 * 60 * 1000L,
    "2h"  to 2  * 60 * 60 * 1000L,
    "4h"  to 4  * 60 * 60 * 1000L,
    "8h"  to 8  * 60 * 60 * 1000L
)

fun scheduleRepeatingNotification(context: Context, type: String, frequencyMs: Long, requestCode: Int) {
    val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent        = Intent(context, NotificationReceiver::class.java).apply { putExtra("type", type) }
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + frequencyMs, frequencyMs, pendingIntent)
}

fun cancelRepeatingNotification(context: Context, type: String, requestCode: Int) {
    val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent        = Intent(context, NotificationReceiver::class.java).apply { putExtra("type", type) }
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.cancel(pendingIntent)
}

// ─────────────────────────────────────────────
//  WRAPPER
// ─────────────────────────────────────────────
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {}
) {
    val prefs by viewModel.prefs.collectAsState()

    SettingScreenContent(
        modifier         = modifier,
        stepsGoal        = prefs.stepsGoal,
        waterGoalMl      = prefs.waterGoalMl,
        notifWater       = prefs.notifWater,
        notifMood        = prefs.notifMood,
        waterFreq        = prefs.waterFreq,
        moodFreq         = prefs.moodFreq,
        googleLinked     = prefs.googleLinked,
        isDarkMode       = isDarkMode,
        onDarkModeToggle = onDarkModeToggle,
        onSave           = { steps, water, nw, nm, wf, mf, gl ->
            // Passamos todos os parâmetros necessários para o ViewModel
            viewModel.saveSettings(
                stepsGoal = steps,
                waterGoalMl = water,
                notifWater = nw,
                notifSteps = prefs.notifSteps,
                notifMood = nm,
                waterFreq = wf,
                moodFreq = mf,
                darkMode = isDarkMode,
                googleLinked = gl
            )
        }
    )
}

// ─────────────────────────────────────────────
//  CONTENT
// ─────────────────────────────────────────────
@Composable
fun SettingScreenContent(
    modifier: Modifier = Modifier,
    stepsGoal: Int = 10000,
    waterGoalMl: Int = 2500,
    notifWater: Boolean = false,
    notifMood: Boolean = false,
    waterFreq: String = "1h",
    moodFreq: String = "1h",
    googleLinked: Boolean = false,
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onSave: (Int, Int, Boolean, Boolean, String, String, Boolean) -> Unit = { _,_,_,_,_,_,_ -> }
) {
    val c       = AppTheme.colors
    val context = LocalContext.current

    var stepsGoalText    by remember(stepsGoal)    { mutableStateOf(stepsGoal.toString()) }
    var waterGoalText    by remember(waterGoalMl)  { mutableStateOf(waterGoalMl.toString()) }
    var googleState      by remember(googleLinked) { mutableStateOf(googleLinked) }
    var notifWaterState  by remember(notifWater)   { mutableStateOf(notifWater) }
    var notifMoodState   by remember(notifMood)    { mutableStateOf(notifMood) }
    var waterFreqState   by remember(waterFreq)    { mutableStateOf(waterFreq) }
    var moodFreqState    by remember(moodFreq)     { mutableStateOf(moodFreq) }
    var settingsSaved    by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center) {
            Text("Definições", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = c.card),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null, tint = c.primary, modifier = Modifier.size(22.dp))
                    Column {
                        Text("Modo escuro", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Text(if (isDarkMode) "Ativado" else "Desativado", fontSize = 12.sp, color = c.textSecondary)
                    }
                }
                Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                        checkedTrackColor = c.primary, uncheckedTrackColor = Color(0xFFCBD5E0)))
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = c.card),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.Unspecified
                    )
                    Column {
                        Text("Ligar conta google", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Text(if (googleState) "Ativado" else "Desativado", fontSize = 12.sp, color = c.textSecondary)
                    }
                }
                Switch(checked = googleState, onCheckedChange = { googleState = it; settingsSaved = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                        checkedTrackColor = c.primary, uncheckedTrackColor = Color(0xFFCBD5E0)))
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = c.card),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Definir metas diárias",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary
                )

                GoalRow("Passos", stepsGoalText, "", 6, KeyboardType.Number, c) {
                    stepsGoalText = it
                    settingsSaved = false
                }

                GoalRow("Água", waterGoalText, "ml", 5, KeyboardType.Number, c) {
                    waterGoalText = it
                    settingsSaved = false
                }

                // 🔽 BOTÃO AGORA AQUI
                Button(
                    onClick = {
                        val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                        val water = waterGoalText.toIntOrNull() ?: waterGoalMl

                        onSave(
                            steps,
                            water,
                            notifWaterState,
                            notifMoodState,
                            waterFreqState,
                            moodFreqState,
                            googleState
                        )

                        settingsSaved = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    Text("Guardar metas", color = Color.White)
                }

                if (settingsSaved) {
                    Text(
                        "✓ Metas guardadas!",
                        color = c.primary,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = c.card),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Notificações",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary
                )

                Spacer(Modifier.height(8.dp))

                // ───────── HUMOR ─────────
                NotifRowWithFreq(
                    "😊 Humor",
                    "Lembrar para registar emoção",
                    notifMoodState,
                    moodFreqState,
                    c,
                    { isChecked ->
                        notifMoodState = isChecked
                        settingsSaved = false

                        if (isChecked) {
                            cancelRepeatingNotification(context, "mood", 101)

                            scheduleRepeatingNotification(
                                context,
                                "mood",
                                NOTIF_FREQUENCIES[moodFreqState] ?: NOTIF_FREQUENCIES["4h"]!!,
                                101
                            )
                        } else {
                            cancelRepeatingNotification(context, "mood", 101)
                        }

                        val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                        val water = waterGoalText.toIntOrNull() ?: waterGoalMl

                        onSave(
                            steps, water,
                            notifWaterState,
                            notifMoodState,
                            waterFreqState,
                            moodFreqState,
                            googleState
                        )
                    },
                    { newFreq ->
                        moodFreqState = newFreq
                        settingsSaved = false

                        if (notifMoodState) {
                            cancelRepeatingNotification(context, "mood", 101)

                            scheduleRepeatingNotification(
                                context,
                                "mood",
                                NOTIF_FREQUENCIES[newFreq]!!,
                                101
                            )
                        }

                        val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                        val water = waterGoalText.toIntOrNull() ?: waterGoalMl

                        onSave(
                            steps, water,
                            notifWaterState,
                            notifMoodState,
                            waterFreqState,
                            moodFreqState,
                            googleState
                        )
                    }
                )

                HorizontalDivider(color = c.divider, modifier = Modifier.padding(vertical = 6.dp))

                // ───────── ÁGUA ─────────
                NotifRowWithFreq(
                    "💧 Água",
                    "Lembrar para beber água",
                    notifWaterState,
                    waterFreqState,
                    c,
                    { isChecked ->
                        notifWaterState = isChecked
                        settingsSaved = false

                        if (isChecked) {
                            cancelRepeatingNotification(context, "water", 100)

                            scheduleRepeatingNotification(
                                context,
                                "water",
                                NOTIF_FREQUENCIES[waterFreqState] ?: NOTIF_FREQUENCIES["1h"]!!,
                                100
                            )
                        } else {
                            cancelRepeatingNotification(context, "water", 100)
                        }

                        val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                        val water = waterGoalText.toIntOrNull() ?: waterGoalMl

                        onSave(
                            steps, water,
                            notifWaterState,
                            notifMoodState,
                            waterFreqState,
                            moodFreqState,
                            googleState
                        )
                    },
                    { newFreq ->
                        waterFreqState = newFreq
                        settingsSaved = false

                        if (notifWaterState) {
                            cancelRepeatingNotification(context, "water", 100)

                            scheduleRepeatingNotification(
                                context,
                                "water",
                                NOTIF_FREQUENCIES[newFreq]!!,
                                100
                            )
                        }

                        val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                        val water = waterGoalText.toIntOrNull() ?: waterGoalMl

                        onSave(
                            steps, water,
                            notifWaterState,
                            notifMoodState,
                            waterFreqState,
                            moodFreqState,
                            googleState
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalRow(label: String, value: String, suffix: String, maxLen: Int, keyboard: KeyboardType, c: com.example.healthtracker.ui.theme.AppColors, onChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary, letterSpacing = 0.8.sp, modifier = Modifier.width(52.dp))
        OutlinedTextField(
            value = value, onValueChange = { if (it.length <= maxLen) onChange(it) },
            modifier = Modifier.weight(1f), singleLine = true,
            suffix = if (suffix.isNotEmpty()) {{ Text(suffix, color = c.textSecondary, fontSize = 12.sp) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = c.inputBorderFocused, unfocusedBorderColor = c.inputBorder, focusedContainerColor = c.card, unfocusedContainerColor = c.card, focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
    }
}

@Composable
private fun NotifRowWithFreq(label: String, description: String, checked: Boolean, selectedFreq: String, c: com.example.healthtracker.ui.theme.AppColors, onToggle: (Boolean) -> Unit, onFreqChange: (String) -> Unit) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val freqOptions = NOTIF_FREQUENCIES.keys.toList()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            Text(description, fontSize = 11.sp, color = c.textSecondary)
        }
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = c.primary, uncheckedTrackColor = Color(0xFFCBD5E0)))
        Box {
            OutlinedButton(
                onClick = { if (checked) dropdownExpanded = true }, enabled = checked,
                modifier = Modifier.height(40.dp).width(72.dp), shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (checked) c.primary.copy(alpha = 0.5f) else c.inputBorder),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (checked) c.primary else c.textSecondary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Text(selectedFreq, fontSize = 12.sp)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, containerColor = c.card) {
                freqOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option, fontSize = 13.sp, color = c.textPrimary) }, onClick = { onFreqChange(option); dropdownExpanded = false })
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 2) {} }) { padding ->
                SettingScreenContent(modifier = Modifier.padding(padding), isDarkMode = false, googleLinked = false)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold(bottomBar = { BottomNavBar(selectedTab = 2) {} }) { padding ->
                SettingScreenContent(modifier = Modifier.padding(padding), isDarkMode = true, googleLinked = true)
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun SettingScreenTabletLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Row(modifier = Modifier.fillMaxSize().background(LightColors.background)) {
                NavRail(selectedTab = 2) {}
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Scaffold(modifier = Modifier.widthIn(max = 800.dp), containerColor = LightColors.background) { padding ->
                        SettingScreenContent(modifier = Modifier.padding(padding), isDarkMode = false, googleLinked = false)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun SettingScreenTabletDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Row(modifier = Modifier.fillMaxSize().background(DarkColors.background)) {
                NavRail(selectedTab = 2) {}
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Scaffold(modifier = Modifier.widthIn(max = 800.dp), containerColor = DarkColors.background) { padding ->
                        SettingScreenContent(modifier = Modifier.padding(padding), isDarkMode = true, googleLinked = true)
                    }
                }
            }
        }
    }
}
