package com.example.healthtracker.pages

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.healthtracker.services.user.UserViewModel
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.ExperimentalLayoutApi

// ─────────────────────────────────────────────
//  ENTRY POINT
// ─────────────────────────────────────────────
@Composable
fun HealthTrackerApp(
    userViewModel: UserViewModel = viewModel(),
    windowSizeClass: WindowSizeClass? = null
) {
    val prefs by userViewModel.prefs.collectAsState()
    val isDarkMode = prefs.darkMode
    val colors = if (isDarkMode) DarkColors else LightColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()) {
            HealthTrackerScreen(
                userViewModel = userViewModel,
                isDarkMode = isDarkMode,
                onDarkModeToggle = { userViewModel.saveDarkMode(it) },
                windowSizeClass = windowSizeClass
            )
        }
    }
}

// ─────────────────────────────────────────────
//  ECRÃ PRINCIPAL (Lógica de Navegação e Detalhes)
// ─────────────────────────────────────────────
@Composable
fun HealthTrackerScreen(
    userViewModel: UserViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    windowSizeClass: WindowSizeClass? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Estados para controlar a visibilidade dos ecrãs de detalhe
    var showStepDetails by remember { mutableStateOf(false) }
    var showEmotionDetails by remember { mutableStateOf(false) }
    var showWaterDetails by remember { mutableStateOf(false) }
    val prefs by userViewModel.prefs.collectAsState()
    val history by userViewModel.historyEntries.collectAsState()
    val useNavRail = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact

    // Launcher para entrada de voz (Água)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
                if (spokenText.isNotEmpty()) {
                    val ml = spokenText.filter { it.isDigit() }.toIntOrNull()
                    if (ml != null) {
                        userViewModel.addWater(ml)
                    }
                }
            }
        }
    )

    val startVoiceWaterInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale a quantidade de água (ex: 250)...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            // Não suportado
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavRail) {
            NavRail(selectedTab) {
                selectedTab = it
                showStepDetails = false
                showEmotionDetails = false
                showWaterDetails = false
            }
        }

        Scaffold(
            containerColor = AppTheme.colors.background,
            bottomBar = {
                // Esconder a barra se algum detalhe estiver aberto
                if (!useNavRail && !showStepDetails && !showEmotionDetails && !showWaterDetails) {
                    BottomNavBar(selectedTab) {
                        selectedTab = it
                        showStepDetails = false
                        showEmotionDetails = false
                        showWaterDetails = false
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                val contentModifier = if (useNavRail) Modifier.widthIn(max = 800.dp).fillMaxHeight() else Modifier.fillMaxSize()
                Box(modifier = contentModifier) {
                    when {
                        showStepDetails -> {
                            StepsDetailScreen(
                                todaySteps = prefs.todaySteps,
                                stepsGoal = prefs.stepsGoal,
                                history = history,
                                onBack = { showStepDetails = false }
                            )
                        }
                        showEmotionDetails -> {
                            EmotionDetailScreen(
                                todayEmotion = prefs.todayEmotion,
                                history = history,
                                onBack = { showEmotionDetails = false }
                            )
                        }
                        showWaterDetails -> {
                            WaterDetailScreen(
                                todayWaterMl = prefs.todayWaterMl,
                                waterGoalMl = prefs.waterGoalMl,
                                history = history,
                                onBack = { showWaterDetails = false }
                            )
                        }
                        else -> {
                            when (selectedTab) {
                                1 -> ProfileScreen(viewModel = userViewModel)
                                2 -> StatsScreen(
                                    todayEmotion = prefs.todayEmotion,
                                    todayWaterMl = prefs.todayWaterMl,
                                    waterGoalMl = prefs.waterGoalMl,
                                    history = history,
                                    userPrefs = prefs // Necessário para o PDF
                                )
                                3 -> SettingScreen(
                                    viewModel = userViewModel,
                                    isDarkMode = isDarkMode,
                                    onDarkModeToggle = onDarkModeToggle
                                )
                                else -> HomeScreenContent(
                                    firstName = prefs.firstName,
                                    lastName = prefs.lastName,
                                    todaySteps = prefs.todaySteps,
                                    stepsGoal = prefs.stepsGoal,
                                    todayEmotion = prefs.todayEmotion,
                                    todayWaterMl = prefs.todayWaterMl,
                                    waterGoalMl = prefs.waterGoalMl,
                                    onEmotionSelected = { userViewModel.setEmotion(it) },
                                    onAddWater = { userViewModel.addWater(it) },
                                    onStepsClick = { showStepDetails = true },
                                    onEmotionClick = { showEmotionDetails = true },
                                    onWaterClick = { showWaterDetails = true },
                                    onVoiceWaterClick = startVoiceWaterInput,
                                    isExpanded = useNavRail,
                                    profilePictureUri = prefs.profilePictureUri
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  NAVIGATION COMPONENTS
// ─────────────────────────────────────────────
@Composable
fun NavRail(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val c = AppTheme.colors
    val items = listOf(Icons.Default.Home to "Início", Icons.Default.Person to "Perfil", Icons.Default.BarChart to "Stats", Icons.Default.Tune to "Definições")
    NavigationRail(containerColor = c.card) {
        Spacer(Modifier.weight(1f))
        items.forEachIndexed { index, item ->
            val (icon, label) = item
            NavigationRailItem(
                selected = selectedTab == index, onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = label) }, label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(selectedIconColor = c.navSelected, unselectedIconColor = c.navUnselected, selectedTextColor = c.navSelected, unselectedTextColor = c.navUnselected, indicatorColor = c.primary.copy(alpha = 0.12f))
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val c = AppTheme.colors
    val items = listOf(Icons.Default.Home to "Início", Icons.Default.Person to "Perfil", Icons.Default.BarChart to "Stats", Icons.Default.Tune to "Definições")
    NavigationBar(containerColor = c.card, tonalElevation = 8.dp) {
        items.forEachIndexed { index, item ->
            val (icon, label) = item
            NavigationBarItem(
                selected = selectedTab == index, onClick = { onTabSelected(index) },
                icon = { Icon(imageVector = icon, contentDescription = label, tint = if (selectedTab == index) c.navSelected else c.navUnselected) },
                label = { Text(text = label, fontSize = 10.sp, color = if (selectedTab == index) c.navSelected else c.navUnselected) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = c.primary.copy(alpha = 0.12f))
            )
        }
    }
}

// ─────────────────────────────────────────────
//  CONTENT HOME (Com Cliques nos Cards)
// ─────────────────────────────────────────────
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    firstName: String = "", lastName: String = "",
    todaySteps: Int = 0, stepsGoal: Int = 10000,
    todayEmotion: Int = 2, todayWaterMl: Int = 0, waterGoalMl: Int = 2500,
    onEmotionSelected: (Int) -> Unit = {}, onAddWater: (Int) -> Unit = {},
    onStepsClick: () -> Unit = {}, onEmotionClick: () -> Unit = {}, onWaterClick: () -> Unit = {},
    onVoiceWaterClick: () -> Unit = {},
    isExpanded: Boolean = false, profilePictureUri: String? = null
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderSection(firstName = firstName, lastName = lastName, profilePictureUri = profilePictureUri)

        Box(modifier = Modifier.clickable { onStepsClick() }) {
            StepsCard(stepsCurrent = todaySteps, stepsGoal = stepsGoal, isExpanded = isExpanded)
        }

        Box(modifier = Modifier.clickable { onEmotionClick() }) {
            EmotionalStateCard(selectedEmotion = todayEmotion, onEmotionSelected = onEmotionSelected)
        }

        Box(modifier = Modifier.clickable { onWaterClick() }) {
            WaterIntakeCard(totalMl = todayWaterMl, goalMl = waterGoalMl, onAddWater = onAddWater, onMicClick = onVoiceWaterClick)
        }
    }
}

// ─────────────────────────────────────────────
//  COMPONENTES DE SUPORTE
// ─────────────────────────────────────────────

@Composable
fun HeaderSection(firstName: String = "", lastName: String = "", profilePictureUri: String? = null) {
    val c = AppTheme.colors
    val displayName = when {
        firstName.isBlank() && lastName.isBlank() -> "utilizador"
        lastName.isBlank() -> firstName
        firstName.isBlank() -> lastName
        else -> "$firstName $lastName"
    }
    val today = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("pt")).format(Date())
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Olá, $displayName!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            Text(today, fontSize = 13.sp, color = c.textSecondary)
        }
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(c.primary, Color(0xFF6AB0F5)))), contentAlignment = Alignment.Center) {
            if (profilePictureUri != null) {
                AsyncImage(model = profilePictureUri, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  CARDS E COMPONENTES GRÁFICOS
// ─────────────────────────────────────────────

@Composable
fun StepsCard(stepsCurrent: Int = 0, stepsGoal: Int = 10000, isExpanded: Boolean = false) {
    val c = AppTheme.colors
    val progress = if (stepsGoal > 0) stepsCurrent.toFloat() / stepsGoal.toFloat() else 0f
    val distanceKm = stepsCurrent * 0.00078f
    val calories = (stepsCurrent * 0.04f).toInt()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Passos", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Start
            ) {
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    CircularProgressRing(progress = progress, trackColor = c.primary.copy(alpha = 0.15f),
                        activeColor = c.primary, size = 72.dp, strokeWidth = 8.dp)
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = c.primary, modifier = Modifier.size(32.dp))
                }

                if (!isExpanded) Spacer(Modifier.width(16.dp))

                Column(horizontalAlignment = if (isExpanded) Alignment.End else Alignment.Start) {
                    Text("Passos ${"%,d".format(stepsCurrent)} / ${"%,d".format(stepsGoal)}",
                        fontSize = 14.sp, color = c.textPrimary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Distância ${"%.1f".format(distanceKm)} km", fontSize = 13.sp, color = c.textSecondary)
                    Text("Calorias $calories kcal", fontSize = 13.sp, color = c.textSecondary)
                }
            }
        }
    }
}

@Composable
fun CircularProgressRing(progress: Float, trackColor: Color, activeColor: Color, size: Dp, strokeWidth: Dp) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "progress")
    Canvas(modifier = Modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val diameter = minOf(this.size.width, this.size.height) - stroke
        val topLeft = Offset(stroke / 2, stroke / 2)
        drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawArc(color = activeColor, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

@Composable
fun EmotionalStateCard(selectedEmotion: Int = 2, onEmotionSelected: (Int) -> Unit = {}) {
    val c = AppTheme.colors
    val emotions = listOf("😄" to "Muito Bom", "🙂" to "Bom", "😐" to "Neutro", "😢" to "Triste", "😤" to "Estressado")
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado Emocional", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Text("Como se sente hoje?", fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                emotions.forEachIndexed { index, item ->
                    val (emoji, label) = item
                    val isSelected = selectedEmotion == index
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) c.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onEmotionSelected(index) }
                        .padding(horizontal = 6.dp, vertical = 6.dp)) {
                        Text(text = emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(text = label, fontSize = 9.sp, color = if (isSelected) c.primary else c.textSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterIntakeCard(totalMl: Int = 0, goalMl: Int = 2500, onAddWater: (Int) -> Unit = {}, onMicClick: () -> Unit = {}) {
    val c = AppTheme.colors
    val progress = if (goalMl > 0) totalMl.toFloat() / goalMl.toFloat() else 0f
    val cupOptions = listOf(200, 250, 300, 350)
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quantidade de água", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                IconButton(onClick = onMicClick) {
                    Icon(Icons.Default.Mic, contentDescription = "Inserir por voz", tint = c.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterBottle(progress = progress, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("${totalMl}ml", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.primary)
                    Text("de ${goalMl}ml", fontSize = 11.sp, color = c.textSecondary)
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(listOf(200, 250), listOf(300, 350)).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { ml ->
                                    Button(
                                        onClick = { onAddWater(ml) },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics { contentDescription = "Adicionar ${ml}ml de água" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = c.primary.copy(alpha = 0.15f),
                                            contentColor = c.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${ml}ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterBottle(progress: Float, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val waterLevel by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(1500, easing = EaseInOutSine), label = "water")
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bottlePath = Path().apply {
            moveTo(w * 0.3f, h * 0.1f)
            lineTo(w * 0.7f, h * 0.1f)
            lineTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.9f)
            quadraticBezierTo(w * 0.85f, h, w * 0.7f, h)
            lineTo(w * 0.3f, h)
            quadraticBezierTo(w * 0.15f, h, w * 0.15f, h * 0.9f)
            lineTo(w * 0.25f, h * 0.25f)
            close()
        }
        drawPath(path = bottlePath, color = c.primary.copy(alpha = 0.1f))
        clipPath(path = bottlePath) {
            drawRect(color = Color(0xFF42A5F5), topLeft = Offset(0f, h * (1f - waterLevel)), size = Size(w, h * waterLevel))
        }
        drawPath(path = bottlePath, color = c.primary, style = Stroke(width = 2.dp.toPx()))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Scaffold(containerColor = LightColors.background, bottomBar = { BottomNavBar(0) {} }) { padding ->
                HomeScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", todaySteps = 7500, stepsGoal = 10000, todayWaterMl = 1200)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold(containerColor = DarkColors.background, bottomBar = { BottomNavBar(0) {} }) { padding ->
                HomeScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", todaySteps = 7500, stepsGoal = 10000, todayWaterMl = 1200)
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun HomeScreenTabletPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Row(modifier = Modifier.fillMaxSize().background(LightColors.background)) {
                NavRail(0) {}
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Scaffold(modifier = Modifier.widthIn(max = 800.dp), containerColor = LightColors.background) { padding ->
                        HomeScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", todaySteps = 7500, stepsGoal = 10000, isExpanded = true, todayWaterMl = 1200)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun HomeScreenTabletDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Row(modifier = Modifier.fillMaxSize().background(DarkColors.background)) {
                NavRail(0) {}
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Scaffold(modifier = Modifier.widthIn(max = 800.dp), containerColor = DarkColors.background) { padding ->
                        HomeScreenContent(modifier = Modifier.padding(padding), firstName = "João", lastName = "Silva", todaySteps = 7500, stepsGoal = 10000, isExpanded = true, todayWaterMl = 1200)
                    }
                }
            }
        }
    }
}
