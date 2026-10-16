package com.akshar.dyslexiareader.data.sync

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.akshar.dyslexiareader.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    private val appDatabase: AppDatabase
) {

    suspend fun exportDataToJsonString(parentAccountId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("parentAccountId", parentAccountId)

        // 1. Profiles
        val profiles = appDatabase.childProfileDao().getAllProfilesDirect()
        val profilesArray = JSONArray()
        for (p in profiles) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("displayName", p.displayName)
                put("gradeLevel", p.gradeLevel)
                put("pinType", p.pinType)
                put("avatarIconName", p.avatarIconName)
                put("preferredThemeId", p.preferredThemeId)
                put("preferredFontId", p.preferredFontId)
                put("createdAt", p.createdAt)
                put("updatedAt", p.updatedAt)
            }
            profilesArray.put(pObj)
        }
        root.put("profiles", profilesArray)

        // 2. Documents
        val docs = appDatabase.documentDao().getAllDocumentsDirect()
        val docsArray = JSONArray()
        for (d in docs) {
            val dObj = JSONObject().apply {
                put("id", d.id)
                put("childProfileId", d.childProfileId)
                put("title", d.title)
                put("cleanText", d.cleanText)
                put("rawOcrText", d.rawOcrText)
                put("detectedScript", d.detectedScript)
                put("subject", d.subject)
                put("isFavorite", d.isFavorite)
                put("tags", d.tags)
                put("wordCount", d.wordCount)
                put("readingProgressPercent", d.readingProgressPercent)
                put("lastReadWordIndex", d.lastReadWordIndex)
                put("createdAt", d.createdAt)
                put("updatedAt", d.updatedAt)
                put("isDeleted", d.isDeleted)
            }
            docsArray.put(dObj)
        }
        root.put("documents", docsArray)

        // 3. Pages
        val pages = appDatabase.pageDao().getAllPages()
        val pagesArray = JSONArray()
        for (page in pages) {
            val pgObj = JSONObject().apply {
                put("id", page.id)
                put("documentId", page.documentId)
                put("pageNumber", page.pageNumber)
                put("cleanText", page.cleanText)
                put("detectedScript", page.detectedScript)
                put("wordCount", page.wordCount)
                put("createdAt", page.createdAt)
                put("updatedAt", page.updatedAt)
            }
            pagesArray.put(pgObj)
        }
        root.put("pages", pagesArray)

        // 4. Reader Settings
        val settings = appDatabase.readerSettingsDao().getAllSettings()
        val settingsArray = JSONArray()
        for (s in settings) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("childProfileId", s.childProfileId)
                put("theme", s.theme)
                put("fontSize", s.fontSize.toDouble())
                put("lineSpacing", s.lineSpacing.toDouble())
                put("letterSpacing", s.letterSpacing.toDouble())
                put("wordSpacing", s.wordSpacing.toDouble())
                put("fontFamily", s.fontFamily)
                put("rulerEnabled", s.rulerEnabled)
                put("rulerWindowLines", s.rulerWindowLines)
                put("ttsSpeed", s.ttsSpeed.toDouble())
                put("ttsPitch", s.ttsPitch.toDouble())
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
            }
            settingsArray.put(sObj)
        }
        root.put("readerSettings", settingsArray)

        // 5. Reading Sessions
        val sessions = appDatabase.readingSessionDao().getAllSessions()
        val sessionsArray = JSONArray()
        for (session in sessions) {
            val sesObj = JSONObject().apply {
                put("id", session.id)
                put("childProfileId", session.childProfileId)
                put("documentId", session.documentId)
                put("durationSeconds", session.durationSeconds)
                put("wordsReadCount", session.wordsReadCount)
                put("startProgressPercent", session.startProgressPercent)
                put("endProgressPercent", session.endProgressPercent)
                put("createdAt", session.createdAt)
            }
            sessionsArray.put(sesObj)
        }
        root.put("readingSessions", sessionsArray)

        // 6. Practice Words / Word Mastery
        val words = appDatabase.wordMasteryDao().getAllWordsDirect()
        val wordsArray = JSONArray()
        for (w in words) {
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("childProfileId", w.childProfileId)
                put("word", w.word)
                put("cleanWord", w.cleanWord)
                put("script", w.script)
                put("timesRead", w.timesRead)
                put("timesPracticed", w.timesPracticed)
                put("isMastered", w.isMastered)
                put("lastPracticedAt", w.lastPracticedAt)
                put("createdAt", w.createdAt)
                put("updatedAt", w.updatedAt)
            }
            wordsArray.put(wObj)
        }
        root.put("practiceWords", wordsArray)

        return@withContext root.toString(2)
    }

    suspend fun createExportFileAndShareIntent(context: Context, parentAccountId: String): Intent = withContext(Dispatchers.IO) {
        val jsonString = exportDataToJsonString(parentAccountId)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val exportFile = File(exportDir, "akshar_data_export_${System.currentTimeMillis()}.json")
        exportFile.writeText(jsonString)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Akshar Data Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
