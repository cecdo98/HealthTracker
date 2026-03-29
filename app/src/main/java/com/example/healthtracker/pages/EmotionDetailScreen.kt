package com.example.healthtracker.pages

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthtracker.data.room.DailyEntryEntity
import com.example.healthtracker.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionDetailScreen(
    todayEmotion: Int,
    history: List<DailyEntryEntity>,
    onBack: () -> Unit
) {
    val c = AppTheme.colors
    val emotions = listOf("😄" to "Muito Bem", "🙂" to "Bem", "😐" to "Neutro", "😢" to "Triste", "😤" to "Estressado")

    // Simulação de estatística: Humor predominante
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
            DetailMetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Humor mais frequente",
                value = emotions[mostFrequentIndex].second + " " + emotions[mostFrequentIndex].first,
                icon = Icons.Default.Face,
                color = c.primary
            )

            // Gráfico de Histórico
            StatsChartCard(
                title = "Evolução Emocional",
                subtitle = "Humor nos últimos dias (0: Estressado - 4: Muito Bem)",
                icon = Icons.Default.Face
            ) {
                val emotionHistory = history.takeLast(6).map { it.emotionIndex.toFloat() } + todayEmotion.toFloat()
                SimpleBarChart(
                    data = emotionHistory,
                    maxValue = 4f,
                    barColor = c.primary
                )
            }
        }
    }
}


