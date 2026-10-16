package com.akshar.dyslexiareader.domain.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.akshar.dyslexiareader.domain.model.StudentProgressStats
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProgressReportExporter {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    /**
     * Exports student progress as a dyslexia-friendly, clean plain text summary.
     */
    fun exportProgressAsText(stats: StudentProgressStats): String {
        if (stats.profile.isDeleted) return ""

        val dateStr = dateFormat.format(Date())
        val sb = StringBuilder()

        sb.appendLine("================================================================")
        sb.appendLine("        AKSHAR — DYSLEXIAREADER STUDENT PROGRESS REPORT         ")
        sb.appendLine("================================================================")
        sb.appendLine("Generated: $dateStr")
        sb.appendLine("Student Name: ${stats.profile.displayName}")
        sb.appendLine("Grade Level:  ${stats.profile.gradeLevel}")
        sb.appendLine("Preferred Font: ${stats.profile.preferredFontId.uppercase()}")
        sb.appendLine("Theme: ${stats.profile.preferredThemeId.uppercase()}")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("📊 1. READING OVERVIEW")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("• Pages Read This Week:    ${stats.weeklyPagesRead} pages")
        sb.appendLine("• Total Pages Read:        ${stats.totalPagesRead} pages")
        sb.appendLine("• Reading Time This Week:  ${stats.weeklyReadingMinutes} minutes")
        sb.appendLine("• Total Reading Time:      ${stats.totalReadingMinutes} minutes (${String.format(Locale.ROOT, "%.1f", stats.totalReadingMinutes / 60f)} hours)")
        sb.appendLine("• Mastered Practice Words: ${stats.masteredWordsCount} words")
        sb.appendLine("• Active Days This Week:   ${stats.activeDaysThisWeek} of 7 days")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("📅 2. WEEKLY DAILY BREAKDOWN")
        sb.appendLine("----------------------------------------------------------------")
        stats.dailyMinutesThisWeek.forEach { day ->
            val bar = "█".repeat((day.minutesRead / 2).coerceAtMost(20))
            sb.appendLine("  ${day.dayOfWeek.padEnd(4)}: ${day.minutesRead.toString().padStart(3)} mins  $bar")
        }
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("⭐ 3. WORDS YOU HAVE MASTERED (${stats.masteredWords.size})")
        sb.appendLine("----------------------------------------------------------------")
        if (stats.masteredWords.isEmpty()) {
            sb.appendLine("  (No words mastered yet. Practice mode will populate this list!)")
        } else {
            val wordsList = stats.masteredWords.map { it.word }.chunked(4)
            wordsList.forEach { row ->
                sb.appendLine("  " + row.joinToString("   •   "))
            }
        }
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("📝 4. TEACHER & PARENT NOTES (${stats.teacherNotes.size})")
        sb.appendLine("----------------------------------------------------------------")
        if (stats.teacherNotes.isEmpty()) {
            sb.appendLine("  (No teacher notes added yet.)")
        } else {
            stats.teacherNotes.forEachIndexed { idx, note ->
                val noteDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(note.createdAt))
                sb.appendLine("  [${idx + 1}] [$noteDate] [${note.category}] - by ${note.authorName}")
                sb.appendLine("      \"${note.noteText}\"")
                sb.appendLine()
            }
        }
        sb.appendLine("================================================================")
        sb.appendLine("Privacy Notice: Stored locally on-device. DPDP Act 2023 Compliant.")
        sb.appendLine("================================================================")

        return sb.toString()
    }

    /**
     * Generates a high-quality, dyslexia-friendly PDF document.
     * Uses warm paper background (#FAF4E6), charcoal text (#2B2B2B), and terracotta accents.
     */
    fun exportProgressAsPdf(context: Context, stats: StudentProgressStats): File? {
        if (stats.profile.isDeleted) return null

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Palette definition
        val bgPaint = Paint().apply { color = Color.parseColor("#FAF4E6") }
        val headerPaint = Paint().apply { color = Color.parseColor("#C85A32") }
        val tealPaint = Paint().apply { color = Color.parseColor("#2D7A78") }
        val greenPaint = Paint().apply { color = Color.parseColor("#387B4C") }
        val cardPaint = Paint().apply { color = Color.parseColor("#FDFBF7") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E5DDD0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val textPrimary = Paint().apply {
            color = Color.parseColor("#2B2B2B")
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val textTitle = Paint().apply {
            color = Color.parseColor("#2B2B2B")
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val textSubtitle = Paint().apply {
            color = Color.parseColor("#5C5750")
            textSize = 10f
            isAntiAlias = true
        }

        // 1. Warm Paper Background
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // 2. Header Banner
        canvas.drawRoundRect(RectF(24f, 24f, 571f, 90f), 12f, 12f, headerPaint)
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Akshar — DyslexiaReader Progress Report", 40f, 54f, headerTextPaint)
        val headerSubPaint = Paint().apply {
            color = Color.parseColor("#FDECE6")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("Positive, gentle reading growth for ${stats.profile.displayName} (${stats.profile.gradeLevel})", 40f, 74f, headerSubPaint)

        // 3. Stats Summary Cards Row
        var curY = 105f
        val cardWidth = 125f
        val cardHeight = 65f
        val gap = 12f

        val statItems = listOf(
            Triple("📖 Pages This Wk", "${stats.weeklyPagesRead} pages", tealPaint),
            Triple("⏱️ Time This Wk", "${stats.weeklyReadingMinutes} mins", headerPaint),
            Triple("⭐ Mastered Words", "${stats.masteredWordsCount} words", greenPaint),
            Triple("🌱 Active Days", "${stats.activeDaysThisWeek} / 7 days", tealPaint)
        )

        statItems.forEachIndexed { index, item ->
            val left = 24f + index * (cardWidth + gap)
            val rect = RectF(left, curY, left + cardWidth, curY + cardHeight)
            canvas.drawRoundRect(rect, 10f, 10f, cardPaint)
            canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

            val labelPaint = Paint().apply {
                color = Color.parseColor("#5C5750")
                textSize = 9.5f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = item.third.color
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(item.first, left + 10f, curY + 22f, labelPaint)
            canvas.drawText(item.second, left + 10f, curY + 48f, valuePaint)
        }

        // 4. Weekly Reading Time Visual Chart
        curY += 80f
        val chartRect = RectF(24f, curY, 571f, curY + 130f)
        canvas.drawRoundRect(chartRect, 12f, 12f, cardPaint)
        canvas.drawRoundRect(chartRect, 12f, 12f, borderPaint)

        canvas.drawText("Weekly Reading Consistency (Minutes Read per Day)", 38f, curY + 22f, textTitle.apply { textSize = 13f })

        val days = stats.dailyMinutesThisWeek
        val barMaxHeight = 60f
        val maxMins = (days.maxOfOrNull { it.minutesRead } ?: 20).coerceAtLeast(20)
        val barSlotWidth = (500f / days.size)

        days.forEachIndexed { i, day ->
            val barHeight = ((day.minutesRead.toFloat() / maxMins) * barMaxHeight).coerceAtLeast(4f)
            val barLeft = 45f + i * barSlotWidth + 15f
            val barTop = curY + 95f - barHeight
            val barRight = barLeft + 28f
            val barBottom = curY + 95f

            val bPaint = if (day.minutesRead > 0) greenPaint else Paint().apply { color = Color.parseColor("#E5DDD0") }
            canvas.drawRoundRect(RectF(barLeft, barTop, barRight, barBottom), 4f, 4f, bPaint)

            canvas.drawText(day.dayOfWeek, barLeft + 4f, curY + 112f, textSubtitle)
            if (day.minutesRead > 0) {
                canvas.drawText("${day.minutesRead}m", barLeft + 2f, barTop - 4f, textSubtitle)
            }
        }

        // 5. Mastered Words Bank
        curY += 145f
        val wordBankRect = RectF(24f, curY, 571f, curY + 130f)
        canvas.drawRoundRect(wordBankRect, 12f, 12f, cardPaint)
        canvas.drawRoundRect(wordBankRect, 12f, 12f, borderPaint)

        canvas.drawText("Words You Have Mastered (${stats.masteredWords.size})", 38f, curY + 22f, textTitle.apply { textSize = 13f })
        if (stats.masteredWords.isEmpty()) {
            canvas.drawText("No mastered words yet. Practice reading textbook lessons to grow your list!", 38f, curY + 55f, textSubtitle)
        } else {
            var wordX = 38f
            var wordY = curY + 48f
            val chipPaint = Paint().apply { color = Color.parseColor("#E4F2E8") }
            val chipBorder = Paint().apply { color = Color.parseColor("#387B4C"); style = Paint.Style.STROKE; strokeWidth = 1f }
            val chipText = Paint().apply { color = Color.parseColor("#387B4C"); textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); isAntiAlias = true }

            stats.masteredWords.take(18).forEach { wm ->
                val wWidth = chipText.measureText(wm.word) + 16f
                if (wordX + wWidth > 550f) {
                    wordX = 38f
                    wordY += 26f
                }
                if (wordY < curY + 120f) {
                    val cRect = RectF(wordX, wordY - 14f, wordX + wWidth, wordY + 8f)
                    canvas.drawRoundRect(cRect, 6f, 6f, chipPaint)
                    canvas.drawRoundRect(cRect, 6f, 6f, chipBorder)
                    canvas.drawText(wm.word, wordX + 8f, wordY, chipText)
                    wordX += wWidth + 8f
                }
            }
        }

        // 6. Teacher & Parent Notes
        curY += 145f
        val notesRect = RectF(24f, curY, 571f, curY + 175f)
        canvas.drawRoundRect(notesRect, 12f, 12f, cardPaint)
        canvas.drawRoundRect(notesRect, 12f, 12f, borderPaint)

        canvas.drawText("Teacher & Parent Notes (${stats.teacherNotes.size})", 38f, curY + 22f, textTitle.apply { textSize = 13f })

        if (stats.teacherNotes.isEmpty()) {
            canvas.drawText("No notes added yet. Use the Parent/Teacher Stats screen to record observations.", 38f, curY + 55f, textSubtitle)
        } else {
            var noteY = curY + 46f
            stats.teacherNotes.take(3).forEach { note ->
                val noteDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(note.createdAt))
                canvas.drawText("• [${note.category}] ($noteDate) - \"${note.noteText}\"", 38f, noteY, textPrimary.apply { textSize = 10.5f })
                noteY += 34f
            }
        }

        // 7. Footer & DPDP Compliance
        val footerPaint = Paint().apply {
            color = Color.parseColor("#7A746C")
            textSize = 9f
            isAntiAlias = true
        }
        val genDate = dateFormat.format(Date())
        canvas.drawText("Generated locally on $genDate • DPDP Act 2023 Private on-device export • Akshar DyslexiaReader", 40f, 825f, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache dir
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val safeName = stats.profile.displayName.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val pdfFile = File(reportsDir, "Progress_Report_${safeName}.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Purges all cached report files for a deleted profile to comply with DPDP Act.
     */
    fun purgeReportsForProfile(context: Context, profileName: String) {
        val reportsDir = File(context.cacheDir, "reports")
        if (reportsDir.exists()) {
            val safeName = profileName.replace(Regex("[^a-zA-Z0-9_]"), "_")
            reportsDir.listFiles()?.forEach { file ->
                if (file.name.contains(safeName, ignoreCase = true)) {
                    file.delete()
                }
            }
        }
    }
}
