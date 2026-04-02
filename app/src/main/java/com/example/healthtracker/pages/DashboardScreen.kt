package com.example.healthtracker.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.healthtracker.data.UserPreferences
import com.example.healthtracker.data.room.DailyEntryEntity
import com.example.healthtracker.services.pdf.PdfGenerator
import com.example.healthtracker.ui.theme.AppTheme
import com.example.healthtracker.ui.theme.DarkColors
import com.example.healthtracker.ui.theme.LightColors
import com.example.healthtracker.ui.theme.LocalAppColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    todayEmotion: Int,
    todayWaterMl: Int,
    waterGoalMl: Int,
    history: List<DailyEntryEntity> = emptyList(),
    userPrefs: UserPreferences = UserPreferences()
) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val pdfGenerator = remember { PdfGenerator(context) }

    var selectedPeriod by remember { mutableIntStateOf(1) }
    val periods = listOf("Dia", "Semana", "Mês")

    // Lógica para abrir o PDF
    val openPdf: (File) -> Unit = { file ->
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Relatório"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Preparação de dados para os gráficos
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

    // Labels para os gráficos (dias abreviados + "Hoje")
    val waterLabels = remember(selectedPeriod, history, todayWaterMl) {
        val historicalLabels = when (selectedPeriod) {
            0 -> emptyList()
            1 -> history.takeLast(6).map { it.date.takeLast(5) } // ex: "10-05"
            else -> history.takeLast(29).map { it.date.takeLast(5) }
        }
        historicalLabels + "Hoje"
    }

    val emotionLabels = remember(selectedPeriod, history, todayEmotion) {
        val historicalLabels = when (selectedPeriod) {
            0 -> emptyList()
            1 -> history.takeLast(6).map { it.date.takeLast(5) }
            else -> history.takeLast(29).map { it.date.takeLast(5) }
        }
        historicalLabels + "Hoje"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Estatísticas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)

        // 1. Seletor de Período
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

        // 2. CARD DE EXPORTAÇÃO PDF (Agora no final)
        ExportPdfCard(
            onClick = {
                val generatedFile = pdfGenerator.generateHealthReport(userPrefs, history)
                if (generatedFile != null && generatedFile.exists()) {
                    openPdf(generatedFile)
                }
            }
        )

        // 3. Gráfico de Emoções
        StatsChartCard(
            title = "Histórico Emocional",
            subtitle = "Hoje: ${todayEmotion}/4",
            icon = Icons.Default.Face
        ) {
            SimpleBarChart(emotionData, 4f, c.primary, emotionLabels)
        }

        // 4. Gráfico de Água
        StatsChartCard(
            title = "Consumo de Água",
            subtitle = "Hoje: ${todayWaterMl}ml / meta ${waterGoalMl}ml",
            icon = Icons.Default.LocalDrink
        ) {
            SimpleBarChart(waterData, waterGoalMl.toFloat().coerceAtLeast(1000f), Color(0xFF42A5F5), waterLabels)
        }



        // Espaço extra no fim para não ficar colado à barra de navegação
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ExportPdfCard(onClick: () -> Unit) {
    val c = AppTheme.colors
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = c.card)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(45.dp).background(c.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, null, tint = c.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Relatório de Saúde", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                    Text("Gerar e abrir PDF", fontSize = 12.sp, color = c.textSecondary)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = c.textSecondary.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun StatsChartCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    val c = AppTheme.colors
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = c.card)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            }
            Text(subtitle, fontSize = 12.sp, color = c.textSecondary)
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(175.dp)) { content() }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    maxValue: Float,
    barColor: Color,
    labels: List<String> = emptyList()
) {
    val labelColor = AppTheme.colors.textSecondary
    val paint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 28f
            isAntiAlias = true
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas

        val labelAreaHeight = if (labels.isNotEmpty()) 36f else 0f
        val chartHeight = size.height - labelAreaHeight
        val spacing = size.width / (data.size + 1)
        val barWidth = spacing * 0.7f

        data.forEachIndexed { index, value ->
            val x = spacing * (index + 1)
            val barHeight = (value / maxValue) * chartHeight

            // Barra
            drawRoundRect(
                color = barColor.copy(alpha = if (index == data.size - 1) 1f else 0.4f),
                topLeft = Offset(x - barWidth / 2, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Etiqueta em baixo (mesmo x da barra)
            if (labels.isNotEmpty() && index < labels.size) {
                paint.color = labelColor.copy(alpha = 0.7f).hashCode().let {
                    android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue * 255).toInt()
                    )
                }
                drawContext.canvas.nativeCanvas.drawText(
                    labels[index],
                    x,
                    size.height - 4f,
                    paint
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatsScreenLightPreview() {
    val sampleHistory = listOf(
        DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
        DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 0, calories = 280),
        DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 3, calories = 120),
        DailyEntryEntity(date = "2023-10-04", steps = 8000, waterMl = 2500, emotionIndex = 2, calories = 320),
        DailyEntryEntity(date = "2023-10-05", steps = 10000, waterMl = 2000, emotionIndex = 1, calories = 400),
        DailyEntryEntity(date = "2023-10-06", steps = 6000, waterMl = 1700, emotionIndex = 4, calories = 240)
    )
    CompositionLocalProvider(LocalAppColors provides LightColors) {
        MaterialTheme {
            StatsScreen(
                todayEmotion = 2,
                todayWaterMl = 2100,
                waterGoalMl = 2500,
                history = sampleHistory,
                userPrefs = UserPreferences(firstName = "João", lastName = "Silva")
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatsScreenDarkPreview() {
    val sampleHistory = listOf(
        DailyEntryEntity(date = "2023-10-01", steps = 5000, waterMl = 1500, emotionIndex = 1, calories = 200),
        DailyEntryEntity(date = "2023-10-02", steps = 7000, waterMl = 1800, emotionIndex = 0, calories = 280),
        DailyEntryEntity(date = "2023-10-03", steps = 3000, waterMl = 1200, emotionIndex = 3, calories = 120),
        DailyEntryEntity(date = "2023-10-04", steps = 8000, waterMl = 2500, emotionIndex = 2, calories = 320),
        DailyEntryEntity(date = "2023-10-05", steps = 10000, waterMl = 2000, emotionIndex = 1, calories = 400),
        DailyEntryEntity(date = "2023-10-06", steps = 6000, waterMl = 1700, emotionIndex = 4, calories = 240)
    )
    CompositionLocalProvider(LocalAppColors provides DarkColors) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            StatsScreen(
                todayEmotion = 0,
                todayWaterMl = 1200,
                waterGoalMl = 2500,
                history = sampleHistory,
                userPrefs = UserPreferences(firstName = "João", lastName = "Silva")
            )
        }
    }
}
