package com.example.healthtracker.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthtracker.data.room.DailyEntryEntity
import com.example.healthtracker.ui.theme.AppTheme

@Composable
fun StatsScreen(
    todayEmotion: Int,
    todayWaterMl: Int,
    waterGoalMl: Int,
    history: List<DailyEntryEntity> = emptyList()
) {
    val c = AppTheme.colors
    var selectedPeriod by remember { mutableIntStateOf(1) } // 0: Dia, 1: Semana, 2: Mês
    val periods = listOf("Dia", "Semana", "Mês")

    // Lógica de filtro de dados
    val waterData = remember(selectedPeriod, history, todayWaterMl) {
        val historicalValues = when (selectedPeriod) {
            0 -> emptyList()
            1 -> history.takeLast(6).map { it.waterMl.toFloat() }
            else -> history.takeLast(29).map { it.waterMl.toFloat() }
        }
        historicalValues + todayWaterMl.toFloat()
    }

    val emotionData = remember(selectedPeriod, history, todayEmotion) {
        val historicalValues = when (selectedPeriod) {
            0 -> emptyList()
            1 -> history.takeLast(6).map { it.emotionIndex.toFloat() }
            else -> history.takeLast(29).map { it.emotionIndex.toFloat() }
        }
        historicalValues + todayEmotion.toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Estatísticas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            periods.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                    onClick = { selectedPeriod = index },
                    selected = selectedPeriod == index,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = c.primary.copy(alpha = 0.15f),
                        activeContentColor = c.primary,
                        inactiveContainerColor = c.card
                    )
                ) {
                    Text(label, fontSize = 13.sp)
                }
            }
        }

        // Gráfico de Emoções
        StatsChartCard(
            title = "Histórico Emocional",
            subtitle = "Escala de 0 a 4 (Hoje: $todayEmotion)",
            icon = Icons.Default.Face
        ) {
            SimpleBarChart(
                data = emotionData,
                maxValue = 4f,
                barColor = c.primary
            )
        }

        // Gráfico de Água
        StatsChartCard(
            title = "Consumo de Água",
            subtitle = "Hoje: ${todayWaterMl}ml / Meta: ${waterGoalMl}ml",
            icon = Icons.Default.LocalDrink
        ) {
            SimpleBarChart(
                data = waterData,
                maxValue = waterGoalMl.toFloat().coerceAtLeast(1000f),
                barColor = Color(0xFF42A5F5)
            )
        }
    }
}

@Composable
fun StatsChartCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val c = AppTheme.colors
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            }
            Text(subtitle, fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    maxValue: Float,
    barColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        val spacing = size.width / (data.size + 1)
        val barWidth = (size.width / (data.size + 1)) * 0.7f

        data.forEachIndexed { index, value ->
            val x = spacing * (index + 1)
            val barHeight = (value / maxValue) * size.height
            val alpha = if (index == data.size - 1) 1f else 0.4f

            drawRoundRect(
                color = barColor.copy(alpha = alpha),
                topLeft = Offset(x - barWidth / 2, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}