package com.example.healthtracker.services.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.healthtracker.data.UserPreferences
import com.example.healthtracker.data.room.DailyEntryEntity
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    // Alterado para retornar File?
    fun generateHealthReport(prefs: UserPreferences, history: List<DailyEntryEntity>): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        var y = 40f

        canvas.drawText("Relatório de Saúde - HealthTracker", 150f, y, titlePaint)
        y += 40f

        paint.textSize = 14f
        canvas.drawText("Utilizador: ${prefs.firstName} ${prefs.lastName}", 50f, y, paint)
        y += 20f
        canvas.drawText("Metas: ${prefs.stepsGoal} passos | ${prefs.waterGoalMl}ml água", 50f, y, paint)
        y += 40f

        canvas.drawText("Histórico Recente:", 50f, y, titlePaint)
        y += 25f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Data", 50f, y, paint)
        canvas.drawText("Passos", 150f, y, paint)
        canvas.drawText("Água", 250f, y, paint)
        canvas.drawText("Humor", 350f, y, paint)
        y += 10f
        canvas.drawLine(50f, y, 500f, y, paint)
        y += 20f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val emotions = listOf("Muito Bem", "Bem", "Neutro", "Triste", "Estressado")
        val allData = history.takeLast(15)

        allData.forEach { entry ->
            if (y > 800) return@forEach
            canvas.drawText(entry.date, 50f, y, paint)
            canvas.drawText("${entry.steps}", 150f, y, paint)
            canvas.drawText("${entry.waterMl}ml", 250f, y, paint)
            val emotionText = emotions.getOrNull(entry.emotionIndex) ?: "N/A"
            canvas.drawText(emotionText, 350f, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val fileName = "Relatorio_Saude_${System.currentTimeMillis()}.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file // Sucesso: retorna o ficheiro
        } catch (e: Exception) {
            pdfDocument.close()
            null // Falha: retorna null
        }
    }
}