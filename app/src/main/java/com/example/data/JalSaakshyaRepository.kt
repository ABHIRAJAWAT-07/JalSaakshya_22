package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.data.dao.DossierDao
import com.example.data.dao.EvidenceDao
import com.example.data.dao.FarmerDao
import com.example.data.dao.PlotDao
import com.example.data.model.DossierRecord
import com.example.data.model.EvidenceEntry
import com.example.data.model.FarmerProfile
import com.example.data.model.FloodPhase
import com.example.data.model.Plot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JalSaakshyaRepository(
    private val context: Context,
    private val plotDao: PlotDao,
    private val evidenceDao: EvidenceDao,
    private val farmerDao: FarmerDao,
    private val dossierDao: DossierDao
) {
    val allPlots: Flow<List<Plot>> = plotDao.getAllPlots()
    val farmerProfile: Flow<FarmerProfile?> = farmerDao.getProfile()
    val allEvidence: Flow<List<EvidenceEntry>> = evidenceDao.getAllEvidence()
    val allDossiers: Flow<List<DossierRecord>> = dossierDao.getAllDossiers()

    fun getEvidenceForPlot(plotId: String): Flow<List<EvidenceEntry>> {
        return evidenceDao.getEvidenceForPlot(plotId)
    }

    suspend fun getEvidenceForPlotSync(plotId: String): List<EvidenceEntry> {
        return evidenceDao.getEvidenceForPlotSync(plotId)
    }

    suspend fun getPlotById(id: String): Plot? = plotDao.getPlotById(id)

    suspend fun insertPlot(plot: Plot) = plotDao.insertPlot(plot)

    suspend fun deletePlot(plotId: String) {
        evidenceDao.deleteEvidenceForPlot(plotId)
        plotDao.deletePlotById(plotId)
    }

    suspend fun insertEvidence(entry: EvidenceEntry) = evidenceDao.insertEvidence(entry)

    suspend fun deleteEvidence(id: String) = evidenceDao.deleteEvidenceById(id)

    suspend fun updateProfile(profile: FarmerProfile) = farmerDao.insertOrUpdate(profile)

    suspend fun insertDossier(dossier: DossierRecord) = dossierDao.insertDossier(dossier)

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingPlots = allPlots.firstOrNull() ?: emptyList()
        if (existingPlots.isNotEmpty()) return@withContext

        // Initialize default profile
        val profile = FarmerProfile(
            id = 1,
            name = "रामेश्वर यादव (Rameshwar Yadav)",
            village = "सुजानपुर, बाढ़ क्षेत्र (Sujanpur Flood Zone)",
            district = "गोरखपुर (Gorakhpur)",
            language = "hi",
            phone = "9876543210"
        )
        farmerDao.insertOrUpdate(profile)

        // Seed 2 realistic plots in flood-affected plains
        val plot1 = Plot(
            id = "plot_sujanpur_01",
            name = "खेत 1 (उत्तरी रकबा - मुख्य धान)",
            cropType = "धान (Paddy - Sambha Mahsuri)",
            areaAcre = 1.75,
            latitude = 26.7606,
            longitude = 83.3732,
            createdAt = System.currentTimeMillis() - 14 * 86400000L
        )

        val plot2 = Plot(
            id = "plot_sujanpur_02",
            name = "खेत 2 (नहर के पास - मक्का)",
            cropType = "मक्का (Hybrid Maize)",
            areaAcre = 1.20,
            latitude = 26.7648,
            longitude = 83.3795,
            createdAt = System.currentTimeMillis() - 10 * 86400000L
        )

        plotDao.insertPlot(plot1)
        plotDao.insertPlot(plot2)

        // Generate synthetic reference photos stored in app files dir
        val photosDir = File(context.filesDir, "evidence_photos").apply { mkdirs() }

        val p1Before = createSamplePhoto(
            file = File(photosDir, "p1_before.jpg"),
            title = "BAADH SE PEHLE - STANDING PADDY",
            subtitle = "Plot 1 • Normal healthy crop",
            bgColor = Color.rgb(34, 112, 62),
            cropDetails = "Paddy stage: Tillering, healthy green cover"
        )
        val p1During = createSamplePhoto(
            file = File(photosDir, "p1_during.jpg"),
            title = "BAADH KE DAURAAN - 4FT SUBMERGED",
            subtitle = "Plot 1 • Rapti river overflow",
            bgColor = Color.rgb(20, 80, 130),
            cropDetails = "Water level: 3.8 feet, standing 72 hours"
        )
        val p1After = createSamplePhoto(
            file = File(photosDir, "p1_after.jpg"),
            title = "BAADH KE BAAD - SILT & ROOT ROT",
            subtitle = "Plot 1 • 85% Estimated destruction",
            bgColor = Color.rgb(139, 90, 43),
            cropDetails = "Silt deposition 6 inches, decaying crop"
        )

        val entryBefore = EvidenceEntry(
            id = "ev_01_before",
            plotId = plot1.id,
            phase = FloodPhase.BEFORE,
            photoPath = p1Before.absolutePath,
            voiceNotePath = null,
            voiceDurationSec = 22,
            latitude = 26.7607,
            longitude = 83.3734,
            capturedAt = System.currentTimeMillis() - 12 * 86400000L,
            cropDamageNotes = "खेत में धान की फसल पूरी तरह हरी और स्वस्थ खड़ी थी। कोई जलभराव नहीं था।",
            estimatedLossPercent = 0
        )

        val entryDuring = EvidenceEntry(
            id = "ev_02_during",
            plotId = plot1.id,
            phase = FloodPhase.DURING,
            photoPath = p1During.absolutePath,
            voiceNotePath = null,
            voiceDurationSec = 35,
            latitude = 26.7605,
            longitude = 83.3731,
            capturedAt = System.currentTimeMillis() - 4 * 86400000L,
            cropDamageNotes = "राप्ती नदी का बांध टूटने से खेत में 4 फीट पानी भर गया। पूरी फसल डूब गई।",
            estimatedLossPercent = 75
        )

        val entryAfter = EvidenceEntry(
            id = "ev_03_after",
            plotId = plot1.id,
            phase = FloodPhase.AFTER,
            photoPath = p1After.absolutePath,
            voiceNotePath = null,
            voiceDurationSec = 28,
            latitude = 26.7606,
            longitude = 83.3732,
            capturedAt = System.currentTimeMillis() - 1 * 86400000L,
            cropDamageNotes = "पानी उतरने के बाद आधा फीट गाद (सिल्ट) जम गया है। धान की जड़ें सड़ चुकी हैं। 85% नुकसान।",
            estimatedLossPercent = 85
        )

        evidenceDao.insertEvidence(entryBefore)
        evidenceDao.insertEvidence(entryDuring)
        evidenceDao.insertEvidence(entryAfter)
    }

    private fun createSamplePhoto(
        file: File,
        title: String,
        subtitle: String,
        bgColor: Int,
        cropDetails: String
    ): File {
        if (file.exists()) return file
        val width = 720
        val height = 720
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(bgColor)

        // Ground/Water Horizon line
        val horizonPaint = Paint().apply {
            color = Color.argb(80, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 480f, width.toFloat(), height.toFloat(), horizonPaint)

        // Grid lines to simulate field / water waves
        val wavePaint = Paint().apply {
            color = Color.argb(90, 255, 255, 255)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        for (i in 0..5) {
            val y = 500f + i * 35f
            canvas.drawLine(40f, y, (width - 40).toFloat(), y, wavePaint)
        }

        // Text paints
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(240, 240, 240)
            textSize = 26f
            isAntiAlias = true
        }
        val notePaint = Paint().apply {
            color = Color.rgb(255, 230, 150)
            textSize = 22f
            isAntiAlias = true
        }
        val stampPaint = Paint().apply {
            color = Color.rgb(200, 255, 200)
            textSize = 20f
            isAntiAlias = true
        }

        canvas.drawText("JAL-SAAKSHYA FIELD PROOF", 50f, 90f, stampPaint)
        canvas.drawText(title, 50f, 150f, textPaint)
        canvas.drawText(subtitle, 50f, 200f, subPaint)
        canvas.drawText(cropDetails, 50f, 260f, notePaint)

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("GEO-STAMP: 26.7606° N, 83.3732° E", 50f, 640f, stampPaint)
        canvas.drawText("TIME: ${sdf.format(Date())}", 50f, 675f, stampPaint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
        return file
    }
}
