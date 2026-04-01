package com.example.healthtracker.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
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
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterDetailScreen(
    todayWaterMl: Int,
    waterGoalMl: Int,
    history: List<DailyEntryEntity>,
    onBack: () -> Unit
) {
    val c = AppTheme.colors
    val progress = if (waterGoalMl > 0) todayWaterMl.toFloat() / waterGoalMl.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    // Lógica para voltar com o botão do sistema
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes de Hidratação", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // Card Principal - Visualização da Garrafa
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = c.card)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaterBottle(
                        progress = progress, 
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(text = "$percentage%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = c.primary)
                    Text(text = "${todayWaterMl}ml de ${waterGoalMl}ml consumidos", fontSize = 14.sp, color = c.textSecondary)
                }
            }

            // Grid de Informação
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Faltam",
                    value = "${(waterGoalMl - todayWaterMl).coerceAtLeast(0)}ml",
                    icon = Icons.Default.LocalDrink,
                    color = Color(0xFF2196F3)
                )
                val cups = todayWaterMl / 250
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Copos (250ml)",
                    value = "~$cups",
                    icon = Icons.Default.LocalDrink,
                    color = Color(0xFF03A9F4)
                )
            }

            // Gráfico de Histórico
            StatsChartCard(
                title = "Consumo Semanal",
                subtitle = "Histórico de ml consumidos",
                icon = Icons.Default.LocalDrink
            ) {
                val waterHistory = history.takeLast(6).map { it.waterMl.toFloat() } + todayWaterMl.toFloat()
                SimpleBarChart(
                    data = waterHistory,
                    maxValue = waterGoalMl.toFloat().coerceAtLeast(2000f),
                    barColor = Color(0xFF42A5F5)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WaterDetailScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            WaterDetailScreen(
                todayWaterMl = 1500,
                waterGoalMl = 2500,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1200, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 2200, emotionIndex = 0, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 900, emotionIndex = 2, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WaterDetailScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            WaterDetailScreen(
                todayWaterMl = 2600,
                waterGoalMl = 2500,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 2500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 3000, emotionIndex = 4, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1000, emotionIndex = 3, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}
