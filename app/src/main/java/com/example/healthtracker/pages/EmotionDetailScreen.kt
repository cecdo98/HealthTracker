package com.example.healthtracker.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
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
fun EmotionDetailScreen(
    todayEmotion: Int,
    history: List<DailyEntryEntity>,
    onBack: () -> Unit
) {
    val c = AppTheme.colors
    val emotions = listOf("😄" to "Muito Bem", "🙂" to "Bem", "😐" to "Neutro", "😢" to "Triste", "😤" to "Estressado")

    // Lógica para voltar com o botão do sistema
    BackHandler(onBack = onBack)

    // Lógica: Humor predominante
    val allEmotions = history.map { it.emotionIndex } + todayEmotion
    val mostFrequentIndex = allEmotions.groupBy { it }.maxByOrNull { it.value.size }?.key ?: todayEmotion

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Estado Emocional", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // Card de Resumo
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = c.card)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emotions[todayEmotion].first, fontSize = 64.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Hoje sente-se", fontSize = 14.sp, color = c.textSecondary)
                    Text(emotions[todayEmotion].second, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.primary)
                }
            }

            // Estatística Rápida
            EmotionMetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Humor mais frequente",
                value = emotions[mostFrequentIndex].second + " " + emotions[mostFrequentIndex].first,
                icon = Icons.Default.Face,
                color = c.primary
            )

            // Gráfico de Histórico
            EmotionChartCard(
                title = "Evolução Emocional",
                subtitle = "Humor nos últimos dias (0-4)",
                icon = Icons.Default.Face
            ) {
                val emotionHistory = history.takeLast(6).map { it.emotionIndex.toFloat() } + todayEmotion.toFloat()
                EmotionBarChart(
                    data = emotionHistory,
                    maxValue = 4f,
                    barColor = c.primary
                )
            }
        }
    }
}

@Composable
private fun EmotionMetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    val c = AppTheme.colors
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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

@Composable
private fun EmotionChartCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val c = AppTheme.colors
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            }
            Text(subtitle, fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun EmotionBarChart(data: List<Float>, maxValue: Float, barColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        val spacing = size.width / (data.size + 1)
        val barWidth = spacing * 0.7f
        data.forEachIndexed { index, value ->
            val x = spacing * (index + 1)
            val barHeight = (value / maxValue) * size.height
            drawRoundRect(
                color = barColor.copy(alpha = if (index == data.size - 1) 1f else 0.4f),
                topLeft = Offset(x - barWidth / 2, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmotionDetailScreenLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            EmotionDetailScreen(
                todayEmotion = 0,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 0, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 3, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmotionDetailScreenDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            EmotionDetailScreen(
                todayEmotion = 2,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 4, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 2, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun EmotionDetailScreenTabletLightPreview() {
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            EmotionDetailScreen(
                todayEmotion = 0,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 0, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 3, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
fun EmotionDetailScreenTabletDarkPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            EmotionDetailScreen(
                todayEmotion = 2,
                history = listOf(
                    DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
                    DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 4, calories = 280),
                    DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 2, calories = 120)
                ),
                onBack = {}
            )
        }
    }
}