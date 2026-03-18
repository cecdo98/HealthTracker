package com.example.healthtracker.pages

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
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel

// ─────────────────────────────────────────────
//  WRAPPER — lê o ViewModel e passa para o Content
// ─────────────────────────────────────────────
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
    val prefs by viewModel.prefs.collectAsState()

    SettingScreenContent(
        modifier    = modifier,
        stepsGoal   = prefs.stepsGoal,
        waterGoalMl = prefs.waterGoalMl,
        notifWater  = prefs.notifWater,
        notifSteps  = prefs.notifSteps,
        notifMood   = prefs.notifMood,
        onSave      = { steps, water, nw, ns, nm ->
            viewModel.saveSettings(steps, water, nw, ns, nm)
        }
    )
}

// ─────────────────────────────────────────────
//  CONTENT — composable puro, sem ViewModel
// ─────────────────────────────────────────────
@Composable
fun SettingScreenContent(
    modifier: Modifier = Modifier,
    stepsGoal: Int = 10000,
    waterGoalMl: Int = 2500,
    notifWater: Boolean = true,
    notifSteps: Boolean = true,
    notifMood: Boolean = false,
    onSave: (Int, Int, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    // Estado local dos campos antes de guardar
    var stepsGoalText by remember(stepsGoal)   { mutableStateOf(stepsGoal.toString()) }
    var waterGoalText by remember(waterGoalMl) { mutableStateOf(waterGoalMl.toString()) }
    var googleLinked  by remember { mutableStateOf(false) }

    var notifWaterState by remember(notifWater) { mutableStateOf(notifWater) }
    var notifStepsState by remember(notifSteps) { mutableStateOf(notifSteps) }
    var notifMoodState  by remember(notifMood)  { mutableStateOf(notifMood) }
    var remindWaterText by remember { mutableStateOf("") }
    var remindStepsText by remember { mutableStateOf("") }
    var remindMoodText  by remember { mutableStateOf("") }
    var freqWater       by remember { mutableStateOf("1h") }
    var freqSteps       by remember { mutableStateOf("1h") }
    var freqMood        by remember { mutableStateOf("1h") }
    var settingsSaved   by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingHeader()

        GoogleAccountCard(
            linked   = googleLinked,
            onToggle = { googleLinked = it; settingsSaved = false }
        )

        DailyTargetCard(
            stepsGoalText  = stepsGoalText,
            waterGoalText  = waterGoalText,
            onStepsChanged = { stepsGoalText = it; settingsSaved = false },
            onWaterChanged = { waterGoalText = it; settingsSaved = false }
        )

        NotificationsCard(
            notifWater = notifWaterState, notifSteps = notifStepsState, notifMood = notifMoodState,
            remindWaterText = remindWaterText, remindStepsText = remindStepsText, remindMoodText = remindMoodText,
            freqWater = freqWater, freqSteps = freqSteps, freqMood = freqMood,
            onWaterToggle       = { notifWaterState = it; settingsSaved = false },
            onStepsToggle       = { notifStepsState = it; settingsSaved = false },
            onMoodToggle        = { notifMoodState  = it; settingsSaved = false },
            onRemindWaterChange = { remindWaterText = it },
            onRemindStepsChange = { remindStepsText = it },
            onRemindMoodChange  = { remindMoodText  = it },
            onFreqWaterChange   = { freqWater = it },
            onFreqStepsChange   = { freqSteps = it },
            onFreqMoodChange    = { freqMood  = it }
        )

        ReportsCard()

        Button(
            onClick = {
                val steps = stepsGoalText.toIntOrNull() ?: stepsGoal
                val water = waterGoalText.toIntOrNull() ?: waterGoalMl
                onSave(steps, water, notifWaterState, notifStepsState, notifMoodState)
                settingsSaved = true
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Guardar definições", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        if (settingsSaved) {
            Text(
                text     = "✓ Definições guardadas!",
                color    = Color(0xFF54A3F3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────
//  CABEÇALHO
// ─────────────────────────────────────────────
@Composable
fun SettingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Definir metas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

// ─────────────────────────────────────────────
//  CARD GOOGLE ACCOUNT
// ─────────────────────────────────────────────
@Composable
fun GoogleAccountCard(linked: Boolean, onToggle: (Boolean) -> Unit) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ligar conta google", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Switch(
                checked         = linked,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = PrimaryBlue,
                    uncheckedTrackColor = Color(0xFFCBD5E0)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
//  CARD METAS DIÁRIAS
// ─────────────────────────────────────────────
@Composable
fun DailyTargetCard(
    stepsGoalText: String, waterGoalText: String,
    onStepsChanged: (String) -> Unit, onWaterChanged: (String) -> Unit
) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Definições", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("PASSOS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = TextLight, letterSpacing = 0.8.sp, modifier = Modifier.width(52.dp))
                OutlinedTextField(
                    value         = stepsGoalText,
                    onValueChange = { if (it.length <= 6) onStepsChanged(it) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = Color(0xFFCBD5E0),
                        focusedContainerColor = CardColor, unfocusedContainerColor = CardColor
                    ),
                    shape     = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Água", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = TextLight, letterSpacing = 0.8.sp, modifier = Modifier.width(52.dp))
                OutlinedTextField(
                    value         = waterGoalText,
                    onValueChange = { if (it.length <= 5) onWaterChanged(it) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    suffix        = { Text("ml", color = TextLight, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = Color(0xFFCBD5E0),
                        focusedContainerColor = CardColor, unfocusedContainerColor = CardColor
                    ),
                    shape     = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  CARD NOTIFICAÇÕES
// ─────────────────────────────────────────────
@Composable
fun NotificationsCard(
    notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
    remindWaterText: String, remindStepsText: String, remindMoodText: String,
    freqWater: String, freqSteps: String, freqMood: String,
    onWaterToggle: (Boolean) -> Unit, onStepsToggle: (Boolean) -> Unit, onMoodToggle: (Boolean) -> Unit,
    onRemindWaterChange: (String) -> Unit, onRemindStepsChange: (String) -> Unit, onRemindMoodChange: (String) -> Unit,
    onFreqWaterChange: (String) -> Unit, onFreqStepsChange: (String) -> Unit, onFreqMoodChange: (String) -> Unit
) {
    val freqOptions = listOf("30m", "1h", "2h", "4h", "8h")

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Notificações", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            NotifRow("Água",   notifWater, remindWaterText, freqWater, freqOptions, onWaterToggle, onRemindWaterChange, onFreqWaterChange)
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 4.dp))
            NotifRow("Passos", notifSteps, remindStepsText, freqSteps, freqOptions, onStepsToggle, onRemindStepsChange, onFreqStepsChange)
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 4.dp))
            NotifRow("Humor",  notifMood,  remindMoodText,  freqMood,  freqOptions, onMoodToggle,  onRemindMoodChange,  onFreqMoodChange)
        }
    }
}

// ─────────────────────────────────────────────
//  LINHA DE NOTIFICAÇÃO
// ─────────────────────────────────────────────
@Composable
private fun NotifRow(
    label: String, checked: Boolean, remindText: String,
    selectedFreq: String, freqOptions: List<String>,
    onToggle: (Boolean) -> Unit, onRemindChange: (String) -> Unit, onFreqChange: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.width(46.dp))

        Switch(
            checked = checked, onCheckedChange = onToggle,
            modifier = Modifier.height(28.dp),
            colors   = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = PrimaryBlue,
                uncheckedTrackColor = Color(0xFFCBD5E0)
            )
        )

        Spacer(Modifier.width(80.dp))

        Text("Lembrar", fontSize = 11.sp, color = TextLight)

        Box {
            OutlinedButton(
                onClick  = { if (checked) dropdownExpanded = true },
                enabled  = checked,
                modifier = Modifier.height(44.dp).width(64.dp),
                shape    = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                border = BorderStroke(1.dp, if (checked) Color(0xFFCBD5E0) else Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
            ) {
                Text(selectedFreq, fontSize = 11.sp, color = if (checked) TextDark else TextLight)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (checked) TextLight else Color(0xFFCBD5E0))
            }
            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                freqOptions.forEach { option ->
                    DropdownMenuItem(
                        text    = { Text(option, fontSize = 13.sp) },
                        onClick = { onFreqChange(option); dropdownExpanded = false }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  CARD EXPORTAR RELATÓRIOS
// ─────────────────────────────────────────────
@Composable
fun ReportsCard() {
    val exportItems = listOf(
        Triple(Icons.Default.DirectionsWalk,     Color(0xFF4A90E2), "Passos"),
        Triple(Icons.Default.SentimentSatisfied, Color(0xFFFFB347), "Humor"),
        Triple(Icons.Default.WaterDrop,          Color(0xFF5BC8F5), "Água")
    )

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Exportar relatórios", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                exportItems.forEach { (icon, tint, label) ->
                    ExportItemButton(icon = icon, iconTint = tint, label = label, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick  = { },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Exportar todos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ExportItemButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color, label: String, modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick        = { },
        modifier       = modifier.height(68.dp),
        shape          = RoundedCornerShape(12.dp),
        border         = BorderStroke(1.dp, Color(0xFFCBD5E0)),
        contentPadding = PaddingValues(4.dp),
        colors         = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text("Exportar", fontSize = 11.sp, color = TextLight)
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingScreenPreview() {
    MaterialTheme {
        Scaffold(bottomBar = { BottomNavBar(selectedTab = 2) {} }) { padding ->
            SettingScreenContent(
                modifier    = Modifier.padding(padding),
                stepsGoal   = 10000,
                waterGoalMl = 2500,
                notifWater  = true,
                notifSteps  = false,
                notifMood   = false
            )
        }
    }
}