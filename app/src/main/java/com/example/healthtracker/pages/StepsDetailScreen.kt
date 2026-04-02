package com.example.healthtracker.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthtracker.data.room.DailyEntryEntity
import com.example.healthtracker.ui.theme.AppTheme
import androidx.activity.compose.BackHandler
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsDetailScreen(
    todaySteps: Int,
    stepsGoal: Int,
    history: List<DailyEntryEntity>,
    onBack: () -> Unit
) {
    val c = AppTheme.colors
    val progress = if (stepsGoal > 0) todaySteps.toFloat() / stepsGoal.toFloat() else 0f
    val distanceKm = todaySteps * 0.00078f
    val calories = (todaySteps * 0.04f).toInt()

    // Lógica para voltar com o botão do sistema
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes de Passos", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background)
            )
        },
        containerColor = c.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Principal de Progresso
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = c.card)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressRing(
                            progress = progress,
                            trackColor = c.primary.copy(alpha = 0.1f),
                            activeColor = c.primary,
                            size = 180.dp,
                            strokeWidth = 16.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = c.primary, modifier = Modifier.size(40.dp))
                            Text(text = "%,d".format(todaySteps), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = c.textPrimary)
                            Text(text = "de %,d".format(stepsGoal), fontSize = 14.sp, color = c.textSecondary)
                        }
                    }
                }
            }

            // Grelha de Métricas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Distância",
                    value = "%.2f km".format(distanceKm),
                    icon = Icons.Default.Timeline,
                    color = Color(0xFF4CAF50)
                )
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Calorias",
                    value = "$calories kcal",
                    icon = Icons.Default.Whatshot,
                    color = Color(0xFFFF9800)
                )
            }

            // Gráfico de Histórico (Reutilizando o SimpleBarChart do Dashboard)
            StatsChartCard(
                title = "Atividade Semanal",
                subtitle = "Passos nos últimos 7 dias",
                icon = Icons.Default.Timeline
            ) {
                val stepHistory = history.takeLast(6).map { it.steps.toFloat() } + todaySteps.toFloat()
                val stepLabels = history.takeLast(6).map { it.date.takeLast(5) } + "Hoje"
                SimpleBarChart(
                    data = stepHistory,
                    maxValue = stepsGoal.toFloat().coerceAtLeast(10000f),
                    barColor = c.primary,
                    labels = stepLabels
                )
            }
        }
    }
}

@Composable
fun DetailMetricCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    val c = AppTheme.colors
    ElevatedCard(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = c.textSecondary)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StepsDetailScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            StepsDetailScreen(
                todaySteps = 7500,
                stepsGoal = 10000,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 9000, waterMl = 1800, emotionIndex = 0, calories = 360),
                    DailyEntryEntity(date = "2023-10-03", steps = 11000, waterMl = 1200, emotionIndex = 2, calories = 440)
                ),
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StepsDetailScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            StepsDetailScreen(
                todaySteps = 12000,
                stepsGoal = 10000,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 8000, waterMl = 1500, emotionIndex = 1, calories = 320),
                    DailyEntryEntity(date = "2023-10-02", steps = 15000, waterMl = 1800, emotionIndex = 4, calories = 600),
                    DailyEntryEntity(date = "2023-10-03", steps = 2000, waterMl = 1200, emotionIndex = 3, calories = 80)
                ),
                onBack = {}
            )
        }
    }
}
