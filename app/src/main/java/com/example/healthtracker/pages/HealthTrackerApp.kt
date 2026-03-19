package com.example.healthtracker.pages

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        MaterialTheme(
            colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
        ) {
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
//  ECRÃ PRINCIPAL (Adaptativo e Ajustado)
// ─────────────────────────────────────────────
@Composable
fun HealthTrackerScreen(
    userViewModel: UserViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    windowSizeClass: WindowSizeClass? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    val prefs by userViewModel.prefs.collectAsState()
    
    val useNavRail = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact

    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavRail) {
            NavRail(selectedTab) { selectedTab = it }
        }
        
        Scaffold(
            containerColor = AppTheme.colors.background,
            bottomBar = {
                if (!useNavRail) {
                    BottomNavBar(selectedTab) { selectedTab = it }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentModifier = if (useNavRail) {
                    Modifier.widthIn(max = 800.dp).fillMaxHeight()
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = contentModifier) {
                    when (selectedTab) {
                        1 -> ProfileScreen(viewModel = userViewModel)
                        2 -> SettingScreen(
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
                            onAddWater = { userViewModel.addWater(it) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  NAVIGATION RAIL
// ─────────────────────────────────────────────
@Composable
fun NavRail(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val c = AppTheme.colors
    val items = listOf(
        Icons.Default.Home to "Início",
        Icons.Default.Person to "Perfil",
        Icons.Default.Tune to "Definições"
    )
    NavigationRail(
        containerColor = c.card,

    ) {
        Spacer(Modifier.weight(1f))
        items.forEachIndexed { index, (icon, label) ->
            NavigationRailItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = c.navSelected,
                    unselectedIconColor = c.navUnselected,
                    selectedTextColor = c.navSelected,
                    unselectedTextColor = c.navUnselected,
                    indicatorColor = c.primary.copy(alpha = 0.12f)
                )
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────
//  BOTTOM NAV BAR
// ─────────────────────────────────────────────
@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val c = AppTheme.colors
    val items = listOf(
        Icons.Default.Home   to "Início",
        Icons.Default.Person to "Perfil",
        Icons.Default.Tune   to "Definições"
    )
    NavigationBar(containerColor = c.card, tonalElevation = 8.dp) {
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                icon = { Icon(imageVector = icon, contentDescription = label,
                    tint = if (selectedTab == index) c.navSelected else c.navUnselected) },
                label = { Text(text = label, fontSize = 10.sp,
                    color = if (selectedTab == index) c.navSelected else c.navUnselected) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = c.primary.copy(alpha = 0.12f))
            )
        }
    }
}

// ─────────────────────────────────────────────
//  CONTENT HOME
// ─────────────────────────────────────────────
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    firstName: String = "",
    lastName: String = "",
    todaySteps: Int = 0,
    stepsGoal: Int = 10000,
    todayEmotion: Int = 2,
    todayWaterMl: Int = 0,
    waterGoalMl: Int = 2500,
    onEmotionSelected: (Int) -> Unit = {},
    onAddWater: (Int) -> Unit = {}
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderSection(firstName = firstName, lastName = lastName)
        StepsCard(stepsCurrent = todaySteps, stepsGoal = stepsGoal)
        EmotionalStateCard(selectedEmotion = todayEmotion, onEmotionSelected = onEmotionSelected)
        WaterIntakeCard(totalMl = todayWaterMl, goalMl = waterGoalMl, onAddWater = onAddWater)
    }
}

// ─────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────
@Composable
fun HeaderSection(firstName: String = "", lastName: String = "") {
    val c = AppTheme.colors
    val displayName = when {
        firstName.isBlank() && lastName.isBlank() -> "utilizador"
        lastName.isBlank()  -> firstName
        firstName.isBlank() -> lastName
        else                -> "$firstName $lastName"
    }
    val today = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("pt")).format(Date())

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Olá, $displayName!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            Text(today, fontSize = 13.sp, color = c.textSecondary)
        }
        Box(
            modifier = Modifier
                .size(48.dp).clip(CircleShape)
                .background(Brush.linearGradient(colors = listOf(c.primary, Color(0xFF6AB0F5)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  STEPS CARD
// ─────────────────────────────────────────────
@Composable
fun StepsCard(stepsCurrent: Int = 0, stepsGoal: Int = 10000) {
    val c = AppTheme.colors
    val progress   = if (stepsGoal > 0) stepsCurrent.toFloat() / stepsGoal.toFloat() else 0f
    val distanceKm = stepsCurrent * 0.00078f
    val calories   = (stepsCurrent * 0.04f).toInt()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Passos", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    CircularProgressRing(progress = progress, trackColor = c.primary.copy(alpha = 0.15f),
                        activeColor = c.primary, size = 72.dp, strokeWidth = 8.dp)
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = c.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
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

// ─────────────────────────────────────────────
//  PROGRESS RING
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
//  EMOTIONAL CARD
// ─────────────────────────────────────────────
@Composable
fun EmotionalStateCard(selectedEmotion: Int = 2, onEmotionSelected: (Int) -> Unit = {}) {
    val c = AppTheme.colors
    val emotions = listOf("😄" to "Muito Bem", "🙂" to "Bem", "😐" to "Neutro", "😢" to "Triste", "😤" to "Estressado")
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado Emocional", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Text("Como se sente hoje?", fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                emotions.forEachIndexed { index, (emoji, label) ->
                    val isSelected = selectedEmotion == index
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSelected) c.primary.copy(alpha = 0.12f) else Color.Transparent).clickable { onEmotionSelected(index) }.padding(horizontal = 6.dp, vertical = 6.dp)) {
                        Text(text = emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(text = label, fontSize = 9.sp, color = if (isSelected) c.primary else c.textSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  WATER CARD
// ─────────────────────────────────────────────
@Composable
fun WaterIntakeCard(totalMl: Int = 0, goalMl: Int = 2500, onAddWater: (Int) -> Unit = {}) {
    val c = AppTheme.colors
    val progress = if (goalMl > 0) totalMl.toFloat() / goalMl.toFloat() else 0f
    val cupOptions = listOf(200, 250, 300, 350)
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quantidade de água", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterBottle(progress = progress, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("${totalMl}ml", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.primary)
                    Text("/ ${goalMl}ml", fontSize = 10.sp, color = c.textSecondary)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cupOptions.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { ml -> CupButton(ml = ml, modifier = Modifier.weight(1f), onClick = { onAddWater(ml) }) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CupButton(ml: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    // Usamos ElevatedButton com Outlined para ter sombra e borda
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = c.card,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp, // Sombra abaixo do botão
        border = BorderStroke(1.dp, c.primary.copy(alpha = 0.4f)),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
            Text(text = "$ml ml", fontSize = 10.sp, color = c.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun WaterBottle(progress: Float, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "water")
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val bottleTop = h * 0.2f; val bottleLeft = w * 0.25f; val bottleRight = w * 0.75f
        val bottleBottom = h * 0.95f; val neckTop = h * 0.05f
        val neckLeft = w * 0.38f; val neckRight = w * 0.62f
        val path = Path().apply {
            moveTo(neckLeft, neckTop); lineTo(neckRight, neckTop); lineTo(neckRight, bottleTop)
            lineTo(bottleRight, bottleTop + h * 0.08f); lineTo(bottleRight, bottleBottom)
            lineTo(bottleLeft, bottleBottom); lineTo(bottleLeft, bottleTop + h * 0.08f)
            lineTo(neckLeft, bottleTop); close()
        }
        drawPath(path, c.primary.copy(alpha = 0.15f))
        val waterLevel = bottleBottom - (bottleBottom - bottleTop) * animatedProgress
        val waterPath = Path().apply {
            moveTo(bottleLeft, waterLevel); lineTo(bottleRight, waterLevel)
            lineTo(bottleRight, bottleBottom); lineTo(bottleLeft, bottleBottom); close()
        }
        clipPath(path) { drawPath(waterPath, c.primary.copy(alpha = 0.8f)) }
        drawPath(path, c.primary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ─────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            Scaffold(containerColor = LightColors.background,
                bottomBar = { BottomNavBar(0) {} }) { padding ->
                HomeScreenContent(modifier = Modifier.padding(padding),
                    firstName = "João", lastName = "Silva",
                    todaySteps = 7500, stepsGoal = 10000)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Scaffold(containerColor = DarkColors.background,
                bottomBar = { BottomNavBar(0) {} }) { padding ->
                HomeScreenContent(modifier = Modifier.padding(padding),
                    firstName = "João", lastName = "Silva",
                    todaySteps = 7500, stepsGoal = 10000)
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
                    Scaffold(
                        modifier = Modifier.widthIn(max = 800.dp),
                        containerColor = LightColors.background
                    ) { padding ->
                        HomeScreenContent(modifier = Modifier.padding(padding),
                            firstName = "João", lastName = "Silva",
                            todaySteps = 7500, stepsGoal = 10000)
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
                    Scaffold(
                        modifier = Modifier.widthIn(max = 800.dp),
                        containerColor = DarkColors.background
                    ) { padding ->
                        HomeScreenContent(modifier = Modifier.padding(padding),
                            firstName = "João", lastName = "Silva",
                            todaySteps = 7500, stepsGoal = 10000)
                    }
                }
            }
        }
    }
}
