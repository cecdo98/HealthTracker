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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────
//  CORES
// ─────────────────────────────────────────────
val BackgroundColor   = Color(0xFFF5F7FA)
val CardColor         = Color(0xFFFFFFFF)
val PrimaryBlue       = Color(0xFF4A90E2)
val TextDark          = Color(0xFF2D3748)
val TextLight         = Color(0xFF718096)
val BottomBarSelected = Color(0xFF4A90E2)
val BottomBarUnsel    = Color(0xFFB0BEC5)

// ─────────────────────────────────────────────
//  ECRÃ PRINCIPAL — único sítio com ViewModel
// ─────────────────────────────────────────────
@Composable
fun HealthTrackerScreen(userViewModel: UserViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val prefs by userViewModel.prefs.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = { BottomNavBar(selectedTab) { selectedTab = it } }
    ) { padding ->
        when (selectedTab) {
            1    -> ProfileScreen(modifier = Modifier.padding(padding), viewModel = userViewModel)
            2    -> SettingScreen(modifier = Modifier.padding(padding), viewModel = userViewModel)
            else -> HomeScreenContent(
                modifier          = Modifier.padding(padding),
                firstName         = prefs.firstName,
                lastName          = prefs.lastName,
                todaySteps        = prefs.todaySteps,
                stepsGoal         = prefs.stepsGoal,
                todayEmotion      = prefs.todayEmotion,
                todayWaterMl      = prefs.todayWaterMl,
                waterGoalMl       = prefs.waterGoalMl,
                onEmotionSelected = { userViewModel.setEmotion(it) },
                onAddWater        = { userViewModel.addWater(it) }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  ECRÃ HOME — composable puro, sem ViewModel
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderSection(firstName = firstName , lastName = lastName)
        StepsCard(stepsCurrent = todaySteps, stepsGoal = stepsGoal)
        EmotionalStateCard(
            selectedEmotion   = todayEmotion,
            onEmotionSelected = onEmotionSelected
        )
        WaterIntakeCard(
            totalMl    = todayWaterMl,
            goalMl     = waterGoalMl,
            onAddWater = onAddWater
        )
    }
}

// ─────────────────────────────────────────────
//  CABEÇALHO
// ─────────────────────────────────────────────
@Composable
fun HeaderSection(firstName: String = "", lastName: String = "") {
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
            Text("Olá, $displayName!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(today, fontSize = 13.sp, color = TextLight)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = listOf(PrimaryBlue, Color(0xFF6AB0F5)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  CARD DE PASSOS
// ─────────────────────────────────────────────
@Composable
fun StepsCard(
    stepsCurrent: Int = 0,
    stepsGoal: Int = 10000
) {
    val progress   = if (stepsGoal > 0) stepsCurrent.toFloat() / stepsGoal.toFloat() else 0f
    val distanceKm = stepsCurrent * 0.00078f
    val calories   = (stepsCurrent * 0.04f).toInt()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Passos", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    CircularProgressRing(
                        progress    = progress,
                        trackColor  = Color(0xFFE8F0FE),
                        activeColor = PrimaryBlue,
                        size        = 72.dp,
                        strokeWidth = 8.dp
                    )
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Passos ${"%,d".format(stepsCurrent)} / ${"%,d".format(stepsGoal)}",
                        fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Distância ${"%.1f".format(distanceKm)} km", fontSize = 13.sp, color = TextLight)
                    Text("Calorias $calories kcal", fontSize = 13.sp, color = TextLight)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  ANEL CIRCULAR CUSTOM
// ─────────────────────────────────────────────
@Composable
fun CircularProgressRing(
    progress: Float, trackColor: Color, activeColor: Color, size: Dp, strokeWidth: Dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue    = progress,
        animationSpec  = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label          = "progress"
    )
    Canvas(modifier = Modifier.size(size)) {
        val stroke   = strokeWidth.toPx()
        val diameter = minOf(this.size.width, this.size.height) - stroke
        val topLeft  = Offset(stroke / 2, stroke / 2)
        drawArc(
            color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
            topLeft = topLeft, size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = activeColor, startAngle = -90f, sweepAngle = 360f * animatedProgress,
            useCenter = false, topLeft = topLeft, size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

// ─────────────────────────────────────────────
//  CARD ESTADO EMOCIONAL
// ─────────────────────────────────────────────
@Composable
fun EmotionalStateCard(
    selectedEmotion: Int = 2,
    onEmotionSelected: (Int) -> Unit = {}
) {
    val emotions = listOf(
        "😄" to "Muito Bem",
        "🙂" to "Bem",
        "😐" to "Neutro",
        "😢" to "Triste",
        "😤" to "Estressado"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado Emocional", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text("Como se sente hoje?", fontSize = 12.sp, color = TextLight)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                emotions.forEachIndexed { index, (emoji, label) ->
                    val isSelected = selectedEmotion == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onEmotionSelected(index) }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(text = emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text       = label,
                            fontSize   = 9.sp,
                            color      = if (isSelected) PrimaryBlue else TextLight,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  CARD DE ÁGUA
// ─────────────────────────────────────────────
@Composable
fun WaterIntakeCard(
    totalMl: Int = 0,
    goalMl: Int = 2500,
    onAddWater: (Int) -> Unit = {}
) {
    val progress   = if (goalMl > 0) totalMl.toFloat() / goalMl.toFloat() else 0f
    val cupOptions = listOf(200, 250, 300, 350)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quantidade de água", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterBottle(progress = progress, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("${totalMl}ml", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("/ ${goalMl}ml", fontSize = 10.sp, color = TextLight)
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cupOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { ml ->
                                CupButton(
                                    ml       = ml,
                                    modifier = Modifier.weight(1f),
                                    onClick  = { onAddWater(ml) }
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
//  BOTÃO DE COPO
// ─────────────────────────────────────────────
@Composable
fun CupButton(ml: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick         = onClick,
        modifier        = modifier.height(52.dp),
        shape           = RoundedCornerShape(12.dp),
        colors          = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
        border          = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
        contentPadding  = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalDrink,
                contentDescription = null,
                tint     = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
            Text(text = "$ml ml", fontSize = 10.sp, color = PrimaryBlue)
        }
    }
}

// ─────────────────────────────────────────────
//  GARRAFA DE ÁGUA
// ─────────────────────────────────────────────
@Composable
fun WaterBottle(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "water"
    )
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val bottleTop    = h * 0.2f;  val bottleLeft   = w * 0.25f; val bottleRight = w * 0.75f
        val bottleBottom = h * 0.95f; val neckTop      = h * 0.05f
        val neckLeft     = w * 0.38f; val neckRight    = w * 0.62f
        val path = Path().apply {
            moveTo(neckLeft, neckTop);  lineTo(neckRight, neckTop);  lineTo(neckRight, bottleTop)
            lineTo(bottleRight, bottleTop + h * 0.08f); lineTo(bottleRight, bottleBottom)
            lineTo(bottleLeft, bottleBottom); lineTo(bottleLeft, bottleTop + h * 0.08f)
            lineTo(neckLeft, bottleTop); close()
        }
        drawPath(path, Color(0xFFE3EEFF))
        val waterLevel = bottleBottom - (bottleBottom - bottleTop) * animatedProgress
        val waterPath = Path().apply {
            moveTo(bottleLeft, waterLevel); lineTo(bottleRight, waterLevel)
            lineTo(bottleRight, bottleBottom); lineTo(bottleLeft, bottleBottom); close()
        }
        clipPath(path) { drawPath(waterPath, PrimaryBlue.copy(alpha = 0.8f)) }
        drawPath(path, PrimaryBlue, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ─────────────────────────────────────────────
//  BOTTOM NAV BAR
// ─────────────────────────────────────────────
@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home   to "Início",
        Icons.Default.Person to "Perfil",
        Icons.Default.Tune   to "Definições"
    )
    NavigationBar(containerColor = CardColor, tonalElevation = 8.dp) {
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon, contentDescription = label,
                        tint = if (selectedTab == index) BottomBarSelected else BottomBarUnsel
                    )
                },
                label = {
                    Text(
                        text     = label,
                        fontSize = 10.sp,
                        color    = if (selectedTab == index) BottomBarSelected else BottomBarUnsel
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = PrimaryBlue.copy(alpha = 0.12f))
            )
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEW — sem ViewModel, dados fictícios
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        Scaffold(
            containerColor = BackgroundColor,
            bottomBar      = { BottomNavBar(selectedTab = 0) {} }
        ) { padding ->
            HomeScreenContent(
                modifier     = Modifier.padding(padding),
                firstName    = "João",
                lastName     = "Silva",
                todaySteps   = 7500,
                stepsGoal    = 10000,
                todayEmotion = 1,
                todayWaterMl = 1200,
                waterGoalMl  = 2500
            )
        }
    }
}