package com.example.healthtracker.pages

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtracker.services.user.UserViewModel


// ─────────────────────────────────────────────
//  CORES (partilhadas com ProfileScreen.kt)
// ─────────────────────────────────────────────
val BackgroundColor   = Color(0xFFF5F7FA)
val CardColor         = Color(0xFFFFFFFF)
val PrimaryBlue       = Color(0xFF4A90E2)
val TextDark          = Color(0xFF2D3748)
val TextLight         = Color(0xFF718096)
val BottomBarSelected = Color(0xFF4A90E2)
val BottomBarUnsel    = Color(0xFFB0BEC5)

// ─────────────────────────────────────────────
//  ECRÃ PRINCIPAL — gere a navegação entre tabs
// ─────────────────────────────────────────────
@Composable
fun HealthTrackerScreen(userViewModel: UserViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = { BottomNavBar(selectedTab) { selectedTab = it } }
    ) { padding ->
        when (selectedTab) {
            3    -> ProfileScreen(modifier = Modifier.padding(padding), viewModel = userViewModel)
            else -> HomeScreen(modifier = Modifier.padding(padding), viewModel = userViewModel)
        }
    }
}

// ─────────────────────────────────────────────
//  ECRÃ HOME
// ─────────────────────────────────────────────
@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = viewModel()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderSection(firstName = viewModel.firstName)
        StepsCard()
        EmotionalStateCard()
        WaterIntakeCard()
    }
}

// ─────────────────────────────────────────────
//  CABEÇALHO
// ─────────────────────────────────────────────
@Composable
fun HeaderSection(firstName: String = "") {
    val displayName = if (firstName.isNotBlank()) firstName else "utilizador"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Olá, $displayName!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("16 de Junho, 2025", fontSize = 13.sp, color = TextLight)
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
fun StepsCard() {
    val stepsCurrent = 10000
    val stepsGoal    = 12000
    val progress     = stepsCurrent.toFloat() / stepsGoal.toFloat()

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
                        progress = progress, trackColor = Color(0xFFE8F0FE),
                        activeColor = PrimaryBlue, size = 72.dp, strokeWidth = 8.dp
                    )
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Passos ${"%,d".format(stepsCurrent)} / ${"%,d".format(stepsGoal)}", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Distância 7,8 km", fontSize = 13.sp, color = TextLight)
                    Text("Calorias 412 kcal", fontSize = 13.sp, color = TextLight)
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
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Canvas(modifier = Modifier.size(size)) {
        val stroke   = strokeWidth.toPx()
        val diameter = minOf(this.size.width, this.size.height) - stroke
        val topLeft  = Offset(stroke / 2, stroke / 2)
        drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
            topLeft = topLeft, size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawArc(color = activeColor, startAngle = -90f, sweepAngle = 360f * animatedProgress,
            useCenter = false, topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

// ─────────────────────────────────────────────
//  CARD ESTADO EMOCIONAL
// ─────────────────────────────────────────────
@Composable
fun EmotionalStateCard() {
    var selectedEmotion by remember { mutableStateOf(2) }
    val emotions = listOf("😄" to "Muito Bem", "🙂" to "Bem", "😐" to "Neutro", "😢" to "Triste", "😤" to "Estressado")

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
                            .clickable { selectedEmotion = index }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(text = emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(text = label, fontSize = 9.sp,
                            color = if (isSelected) PrimaryBlue else TextLight,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center)
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
fun WaterIntakeCard() {
    val currentLiters = 1.6f; val goalLiters = 2.5f
    val progress = currentLiters / goalLiters; val cups = 6

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quantidade de água", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                WaterBottle(progress = progress, modifier = Modifier.size(64.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${currentLiters}L", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("/ ${goalLiters}L", fontSize = 16.sp, color = TextLight, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("($cups copos registados)", fontSize = 12.sp, color = TextLight)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                        color = PrimaryBlue, trackColor = Color(0xFFE3EEFF)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  GARRAFA DE ÁGUA
// ─────────────────────────────────────────────
@Composable
fun WaterBottle(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "water"
    )
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val bottleTop = h * 0.2f; val bottleLeft = w * 0.25f; val bottleRight = w * 0.75f
        val bottleBottom = h * 0.95f; val neckTop = h * 0.05f; val neckLeft = w * 0.38f; val neckRight = w * 0.62f
        val path = Path().apply {
            moveTo(neckLeft, neckTop); lineTo(neckRight, neckTop); lineTo(neckRight, bottleTop)
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
        Icons.Default.Home     to "Início",
        Icons.Default.BarChart to "Progresso",
        Icons.Default.Star     to "Metas",
        Icons.Default.Person   to "Perfil"
    )
    NavigationBar(containerColor = CardColor, tonalElevation = 8.dp) {
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                icon = {
                    Icon(imageVector = icon, contentDescription = label,
                        tint = if (selectedTab == index) BottomBarSelected else BottomBarUnsel)
                },
                label = {
                    Text(text = label, fontSize = 10.sp,
                        color = if (selectedTab == index) BottomBarSelected else BottomBarUnsel)
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = PrimaryBlue.copy(alpha = 0.12f))
            )
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HealthTrackerPreview() {
    MaterialTheme { HealthTrackerScreen() }
}