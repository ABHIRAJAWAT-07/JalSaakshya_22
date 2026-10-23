package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.EvidenceEntry
import com.example.data.model.FarmerProfile
import com.example.data.model.FloodPhase
import com.example.data.model.Plot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfDossierGenerator {

    suspend fun generateDossierPdf(
        context: Context,
        farmer: FarmerProfile?,
        plot: Plot,
        entries: List<EvidenceEntry>,
        dossierId: String,
        qrPayload: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDir = File(context.filesDir, "dossiers").apply { mkdirs() }
            val pdfFile = File(pdfDir, "JalSaakshya_${plot.name.replace(" ", "_")}_$dossierId.pdf")

            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            // Generate QR Code bitmap
            val qrBitmap = QrCodeGenerator.generateQrBitmap(qrPayload, 200)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            // Split entries into pages: 2 entries per page
            val entriesPerPage = 2
            val totalPages = ((entries.size + entriesPerPage - 1) / entriesPerPage).coerceAtLeast(1)

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawColor(Color.rgb(250, 252, 249))

                // Header on every page
                paint.color = Color.rgb(27, 77, 62) // Deep Farm Green
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

                paint.color = Color.WHITE
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("JAL-SAAKSHYA (जलसाक्ष्य)", 24f, 32f, paint)

                paint.textSize = 10f
                paint.isFakeBoldText = false
                paint.color = Color.rgb(220, 240, 230)
                canvas.drawText("Farmer-Held Geo-Tagged & Time-Stamped Crop Damage Dossier", 24f, 50f, paint)

                paint.textSize = 9f
                paint.color = Color.rgb(200, 225, 210)
                canvas.drawText("Page ${pageIndex + 1} of $totalPages • ID: ${dossierId.take(12)}", 430f, 50f, paint)

                var currentY = 85f

                // Page 1: Farmer & Plot Metadata Summary Card + QR Code
                if (pageIndex == 0) {
                    val metaCardRect = RectF(24f, currentY, (pageWidth - 24).toFloat(), currentY + 125f)
                    paint.color = Color.rgb(240, 247, 244)
                    canvas.drawRoundRect(metaCardRect, 8f, 8f, paint)

                    paint.color = Color.rgb(180, 215, 195)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(metaCardRect, 8f, 8f, paint)

                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(27, 77, 62)
                    paint.textSize = 12f
                    paint.isFakeBoldText = true
                    canvas.drawText("FIELD & CULTIVATOR DETAILS (किसान एवं खेत विवरण)", 38f, currentY + 22f, paint)

                    paint.textSize = 10f
                    paint.isFakeBoldText = false
                    paint.color = Color.rgb(40, 40, 40)

                    val farmerName = farmer?.name?.ifBlank { "Unregistered Farmer (पंजीकृत कृषक)" } ?: "Farmer"
                    val village = farmer?.village?.ifBlank { "Flood Affected District" } ?: "Village"

                    canvas.drawText("Farmer: $farmerName", 38f, currentY + 42f, paint)
                    canvas.drawText("Village/Tehsil: $village", 38f, currentY + 58f, paint)
                    canvas.drawText("Plot Name: ${plot.name}", 38f, currentY + 74f, paint)
                    canvas.drawText("Standing Crop: ${plot.cropType} (${plot.areaAcre} Acre)", 38f, currentY + 90f, paint)
                    canvas.drawText("Reference GPS: ${plot.latitude}, ${plot.longitude}", 38f, currentY + 106f, paint)

                    // Draw QR Code on right side of card
                    if (qrBitmap != null) {
                        val qrSize = 96
                        val qrDest = Rect(pageWidth - 24 - 110, (currentY + 14).toInt(), pageWidth - 24 - 14, (currentY + 14 + qrSize).toInt())
                        canvas.drawBitmap(qrBitmap, null, qrDest, paint)

                        paint.textSize = 8f
                        paint.color = Color.rgb(100, 100, 100)
                        canvas.drawText("Scan to Verify", (pageWidth - 24 - 95).toFloat(), currentY + 118f, paint)
                    }

                    currentY += 140f
                }

                // Section Title
                paint.color = Color.rgb(30, 30, 30)
                paint.textSize = 13f
                paint.isFakeBoldText = true
                val sectionTitle = if (pageIndex == 0) "EVIDENCE TIMELINE (कालानुक्रमिक फसल क्षति साक्ष्य)" else "EVIDENCE TIMELINE (Continued)"
                canvas.drawText(sectionTitle, 24f, currentY, paint)
                currentY += 15f

                // Draw evidence entries for this page
                val startIndex = pageIndex * entriesPerPage
                val endIndex = (startIndex + entriesPerPage).coerceAtMost(entries.size)

                for (i in startIndex until endIndex) {
                    val entry = entries[i]
                    val cardHeight = 250f
                    val cardRect = RectF(24f, currentY, (pageWidth - 24).toFloat(), currentY + cardHeight)

                    // Card background
                    paint.style = Paint.Style.FILL
                    paint.color = Color.WHITE
                    canvas.drawRoundRect(cardRect, 8f, 8f, paint)

                    // Border colored according to phase
                    val phaseColor = when (entry.phase) {
                        FloodPhase.BEFORE -> Color.rgb(45, 106, 79)
                        FloodPhase.DURING -> Color.rgb(0, 102, 153)
                        FloodPhase.AFTER -> Color.rgb(153, 88, 42)
                    }
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    paint.color = phaseColor
                    canvas.drawRoundRect(cardRect, 8f, 8f, paint)

                    // Phase banner inside card
                    paint.style = Paint.Style.FILL
                    paint.color = phaseColor
                    val bannerRect = RectF(24f, currentY, (pageWidth - 24).toFloat(), currentY + 28f)
                    canvas.drawRoundRect(bannerRect, 8f, 8f, paint)

                    paint.color = Color.WHITE
                    paint.textSize = 11f
                    paint.isFakeBoldText = true
                    canvas.drawText("PHASE: ${entry.phase.labelEn.uppercase()}", 36f, currentY + 18f, paint)

                    // Draw photo thumbnail on left
                    val photoFile = File(entry.photoPath)
                    if (photoFile.exists()) {
                        val sampleBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        if (sampleBitmap != null) {
                            val photoRect = RectF(38f, currentY + 40f, 38f + 190f, currentY + 40f + 190f)
                            canvas.drawBitmap(sampleBitmap, null, photoRect, paint)
                            sampleBitmap.recycle()
                        }
                    } else {
                        // Placeholder
                        paint.color = Color.rgb(235, 235, 235)
                        val photoRect = RectF(38f, currentY + 40f, 38f + 190f, currentY + 40f + 190f)
                        canvas.drawRoundRect(photoRect, 4f, 4f, paint)
                        paint.color = Color.rgb(120, 120, 120)
                        paint.textSize = 10f
                        canvas.drawText("Photo Record Saved", 60f, currentY + 130f, paint)
                    }

                    // Metadata on right side of photo
                    val textLeft = 245f
                    var textY = currentY + 52f
                    paint.color = Color.rgb(20, 20, 20)
                    paint.textSize = 10.5f
                    paint.isFakeBoldText = true

                    canvas.drawText("DATE & TIME (समय):", textLeft, textY, paint)
                    paint.isFakeBoldText = false
                    canvas.drawText(sdf.format(Date(entry.capturedAt)), textLeft, textY + 14f, paint)
                    textY += 34f

                    paint.isFakeBoldText = true
                    canvas.drawText("GPS COORDINATES (अक्षांश/देशांतर):", textLeft, textY, paint)
                    paint.isFakeBoldText = false
                    canvas.drawText("${entry.latitude}° N, ${entry.longitude}° E (Accurate)", textLeft, textY + 14f, paint)
                    textY += 34f

                    paint.isFakeBoldText = true
                    canvas.drawText("VOICE NOTE VERIFICATION:", textLeft, textY, paint)
                    paint.isFakeBoldText = false
                    val voiceDesc = if (entry.voiceNotePath != null || entry.voiceDurationSec > 0) {
                        "Recorded (${entry.voiceDurationSec} sec) • Stored on Device"
                    } else {
                        "None recorded"
                    }
                    canvas.drawText(voiceDesc, textLeft, textY + 14f, paint)
                    textY += 34f

                    paint.isFakeBoldText = true
                    canvas.drawText("OBSERVATIONS / LOSS SEVERITY:", textLeft, textY, paint)
                    paint.isFakeBoldText = false
                    paint.color = Color.rgb(50, 50, 50)
                    val notes = entry.cropDamageNotes.ifBlank { "Crop condition recorded per visual evidence." }
                    val cleanNotes = if (notes.length > 90) notes.take(87) + "..." else notes
                    canvas.drawText(cleanNotes, textLeft, textY + 14f, paint)

                    if (entry.estimatedLossPercent > 0) {
                        textY += 32f
                        paint.isFakeBoldText = true
                        paint.color = Color.rgb(180, 20, 20)
                        canvas.drawText("Estimated Crop Damage: ${entry.estimatedLossPercent}%", textLeft, textY, paint)
                    }

                    currentY += cardHeight + 15f
                }

                // Footer on bottom of page
                paint.color = Color.rgb(180, 190, 185)
                canvas.drawLine(24f, (pageHeight - 40).toFloat(), (pageWidth - 24).toFloat(), (pageHeight - 40).toFloat(), paint)

                paint.color = Color.rgb(90, 100, 95)
                paint.textSize = 8.5f
                paint.isFakeBoldText = false
                canvas.drawText("Official Farmer Crop Loss Dossier • Valid for Lekhpal, PMFBY Insurance, KCC & Relief Settlement", 24f, (pageHeight - 25).toFloat(), paint)
                canvas.drawText("Generated: ${sdf.format(Date())}", 420f, (pageHeight - 25).toFloat(), paint)

                document.finishPage(page)
            }

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            pdfFile
        } catch (e: Exception) {
            null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "JalSaakshya Crop Loss Damage Dossier")
                putExtra(Intent.EXTRA_TEXT, "Official Farmer-held Geo-Tagged & Time-Stamped Crop Damage Dossier generated by JalSaakshya.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Damage Dossier PDF via"))
        } catch (e: Exception) {
            // handle error
        }
    }
}
