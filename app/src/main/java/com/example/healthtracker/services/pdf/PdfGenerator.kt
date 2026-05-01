package com.example.healthtracker.services.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.healthtracker.data.UserPreferences
import com.example.healthtracker.data.room.DailyEntryEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class PdfGenerator(private val context: Context) {

    // Cores
    private val colorPrimary = Color.parseColor("#1A569D")
    private val colorAccent = Color.parseColor("#42A5F5")
    private val colorTextPrimary = Color.parseColor("#1A202C")
    private val colorTextSecondary = Color.parseColor("#4A5568")
    private val colorSuccess = Color.parseColor("#2F855A")
    private val colorWarning = Color.parseColor("#C05621")
    private val colorError = Color.parseColor("#C53030")
    private val colorBackground = Color.parseColor("#F7FAFC")
    private val colorBorder = Color.parseColor("#E2E8F0")

    private val pageWidth = 595f
    private val pageHeight = 842f
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)
    private val footerHeight = 40f
    private val maxY = pageHeight - footerHeight

    fun generateHealthReport(
        prefs: UserPreferences,
        history: List<DailyEntryEntity>
    ): File? {

        val pdf = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val sectionSpacing = 36f
        val lineSpacing = 22f
        val rowHeight = 22f

        var currentY = 0f

        // ── HEADER ──────────────────────────────────────────────
        paint.color = colorPrimary
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, pageWidth, 140f, paint)

        // Círculo logo
        paint.color = Color.WHITE
        canvas.drawCircle(margin + 20f, 70f, 24f, paint)

        paint.color = colorPrimary
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("H", margin + 10f, 79f, paint)

        // Títulos header
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("HEALTH REPORT", margin + 60f, 65f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Análise Detalhada de Atividade e Bem-Estar Diário", margin + 60f, 84f, paint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Gerado em: $dateStr", pageWidth - margin, 115f, paint)
        paint.textAlign = Paint.Align.LEFT

        currentY = 165f

        // ── PERFIL ───────────────────────────────────────────────
        if (currentY + 70f < maxY) {
            drawSectionHeader(canvas, "Perfil do Utilizador", margin, currentY, paint)
            currentY += sectionSpacing

            paint.textSize = 11f
            paint.color = colorTextPrimary
            paint.typeface = Typeface.DEFAULT

            canvas.drawText("Nome: ${prefs.firstName} ${prefs.lastName}", margin, currentY, paint)

            val weight = prefs.weight.toFloatOrNull() ?: 0f
            val height = prefs.height.toFloatOrNull() ?: 0f

            if (weight > 0 && height > 0) {
                val bmi = weight / (height / 100).pow(2)

                val (label, color) = when {
                    bmi < 18.5 -> "Baixo Peso" to colorWarning
                    bmi < 25   -> "Normal"     to colorSuccess
                    bmi < 30   -> "Sobrepeso"  to colorWarning
                    else       -> "Obesidade"  to colorError
                }

                paint.color = colorTextPrimary
                canvas.drawText("IMC: %.1f ($label)".format(bmi), margin + 260f, currentY, paint)
            }

            currentY += lineSpacing

            paint.color = colorTextPrimary
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Idade: ${prefs.age}", margin, currentY, paint)
            canvas.drawText("Metas: ${prefs.stepsGoal} passos | ${prefs.waterGoalMl}ml", margin + 200f, currentY, paint)

            currentY += sectionSpacing
        }

        // ── ESTATÍSTICAS ─────────────────────────────────────────
        if (history.isNotEmpty() && currentY + 115f < maxY) {
            drawSectionHeader(canvas, "Estatísticas", margin, currentY, paint)
            currentY += sectionSpacing

            val cardSpacing = 14f
            val cardWidth = (contentWidth - (2f * cardSpacing)) / 3f

            val avgSteps = history.map { it.steps }.average().toInt()
            val totalWater = history.sumOf { it.waterMl }
            val avgCal = history.map { it.calories }.average().toInt()

            drawStatCard(canvas, "Passos", "$avgSteps", margin, currentY, cardWidth, paint)
            drawStatCard(canvas, "Água", "${totalWater}ml", margin + cardWidth + cardSpacing, currentY, cardWidth, paint)
            drawStatCard(canvas, "Calorias", "${avgCal}kcal", margin + (cardWidth + cardSpacing) * 2f, currentY, cardWidth, paint)

            currentY += 88f // card height 75f + padding 13f
        }

        // ── TENDÊNCIA ─────────────────────────────────────────────
        if (history.isNotEmpty() && currentY + 130f < maxY) {
            drawSectionHeader(canvas, "Tendência", margin, currentY, paint)
            currentY += 28f

            drawMiniChart(canvas, history.takeLast(7), margin, currentY, contentWidth, 75f, paint)

            currentY += 105f // chart 75f + labels 15f + padding 15f
        }

        // ── HISTÓRICO ─────────────────────────────────────────────
        if (currentY + 50f < maxY) {
            drawSectionHeader(canvas, "Histórico", margin, currentY, paint)
            currentY += 28f

            val colDate      = margin
            val colSteps     = margin + contentWidth * 0.22f
            val colWater     = margin + contentWidth * 0.42f
            val colCalories  = margin + contentWidth * 0.62f
            val colState     = margin + contentWidth * 0.80f

            // Cabeçalho tabela
            paint.color = Color.parseColor("#D1DCF0")
            paint.style = Paint.Style.FILL
            canvas.drawRect(margin, currentY - 14f, pageWidth - margin, currentY + 8f, paint)

            paint.color = colorPrimary
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = 9f

            canvas.drawText("DATA",      colDate,     currentY, paint)
            canvas.drawText("PASSOS",    colSteps,    currentY, paint)
            canvas.drawText("ÁGUA",      colWater,    currentY, paint)
            canvas.drawText("CALORIAS",  colCalories, currentY, paint)
            canvas.drawText("ESTADO",    colState,    currentY, paint)

            currentY += rowHeight

            // Linhas de dados
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 9f

            history.reversed().forEachIndexed { index, e ->
                if (currentY + rowHeight > maxY) return@forEachIndexed

                // Fundo alternado
                if (index % 2 == 0) {
                    paint.color = colorBackground
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(margin, currentY - 13f, pageWidth - margin, currentY + 6f, paint)
                }

                paint.style = Paint.Style.FILL
                paint.color = colorTextPrimary
                canvas.drawText(e.date,          colDate,     currentY, paint)
                canvas.drawText("${e.steps}",    colSteps,    currentY, paint)
                canvas.drawText("${e.waterMl}ml",colWater,    currentY, paint)
                canvas.drawText("${e.calories}kcal", colCalories, currentY, paint)

                val (emo, color) = when (e.emotionIndex) {
                    0    -> "Muito Bem" to colorSuccess
                    1    -> "Bem"       to colorAccent
                    2    -> "Neutro"    to colorTextSecondary
                    3    -> "Triste"    to colorWarning
                    else -> "Stress"    to colorError
                }

                paint.color = color
                canvas.drawText(emo, colState, currentY, paint)

                currentY += rowHeight
            }
        }

        // ── FOOTER ────────────────────────────────────────────────
        paint.color = colorBorder
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, pageHeight - 30f, pageWidth, pageHeight, paint)

        paint.color = colorTextSecondary
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Relatório gerado automaticamente pela app Health Tracker", margin, pageHeight - 12f, paint)

        pdf.finishPage(page)

        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "relatorio.pdf"
        )

        return try {
            pdf.writeTo(FileOutputStream(file))
            pdf.close()
            file
        } catch (e: Exception) {
            pdf.close()
            null
        }
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, x: Float, y: Float, paint: Paint) {
        paint.textSize = 13f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = colorPrimary
        paint.style = Paint.Style.FILL

        canvas.drawText(title.uppercase(), x, y, paint)

        paint.strokeWidth = 2f
        canvas.drawLine(x, y + 6f, x + contentWidth, y + 6f, paint)

        // reset
        paint.strokeWidth = 1f
        paint.typeface = Typeface.DEFAULT
    }

    private fun drawStatCard(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        width: Float,
        paint: Paint
    ) {
        val rect = RectF(x, y, x + width, y + 75f)

        // Sombra leve
        paint.color = Color.parseColor("#E8EDF5")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(x + 2f, y + 2f, x + width + 2f, y + 77f), 12f, 12f, paint)

        // Fundo
        paint.color = Color.WHITE
        canvas.drawRoundRect(rect, 12f, 12f, paint)

        // Border
        paint.color = colorBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Label
        paint.color = colorTextSecondary
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x + 12f, y + 24f, paint)

        // Valor
        paint.color = colorPrimary
        paint.textSize = 17f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(value, x + 12f, y + 54f, paint)

        // reset
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 11f
    }

    private fun drawMiniChart(
        canvas: Canvas,
        data: List<DailyEntryEntity>,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        paint: Paint
    ) {
        if (data.isEmpty()) return

        val max = data.maxOf { it.steps }.toFloat().coerceAtLeast(1000f)
        val spacing = width / data.size
        val barWidth = spacing * 0.5f

        // Linha base
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawLine(x, y + height, x + width, y + height, paint)

        data.forEachIndexed { i, d ->
            val h = (d.steps / max) * height
            val left = x + i * spacing + (spacing - barWidth) / 2f

            // Barra com gradiente simulado — cor mais clara no topo
            paint.color = colorAccent
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(
                RectF(left, y + height - h, left + barWidth, y + height),
                5f, 5f, paint
            )

            // Label data
            paint.color = colorTextSecondary
            paint.textSize = 7f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(d.date.takeLast(5), left - 2f, y + height + 13f, paint)
        }

        paint.strokeWidth = 1f
    }
}