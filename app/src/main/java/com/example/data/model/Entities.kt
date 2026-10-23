package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.UUID

enum class FloodPhase(val labelHi: String, val labelEn: String) {
    BEFORE("बाढ़ से पहले (सामान्य स्थिति)", "Before Flood (Normal crop)"),
    DURING("बाढ़ के दौरान (जलमग्न / पानी भरा)", "During Flood (Submerged crop)"),
    AFTER("बाढ़ के बाद (कीचड़ / नष्ट फसल)", "After Flood (Silted / Destroyed)")
}

class Converters {
    @TypeConverter
    fun fromFloodPhase(phase: FloodPhase): String = phase.name

    @TypeConverter
    fun toFloodPhase(value: String): FloodPhase = try {
        FloodPhase.valueOf(value)
    } catch (e: Exception) {
        FloodPhase.BEFORE
    }
}

@Entity(tableName = "farmer_profile")
data class FarmerProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val village: String = "",
    val district: String = "",
    val language: String = "hi",
    val phone: String = ""
)

@Entity(tableName = "plots")
data class Plot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cropType: String = "धान (Paddy)",
    val areaAcre: Double = 1.0,
    val latitude: Double = 26.8467,
    val longitude: Double = 80.9462,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "evidence_entries")
data class EvidenceEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val plotId: String,
    val phase: FloodPhase,
    val photoPath: String,
    val voiceNotePath: String? = null,
    val voiceDurationSec: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val capturedAt: Long = System.currentTimeMillis(),
    val cropDamageNotes: String = "",
    val estimatedLossPercent: Int = 0
)

@Entity(tableName = "dossiers")
data class DossierRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val plotId: String,
    val plotName: String,
    val farmerName: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val pdfFilePath: String,
    val qrPayload: String,
    val entriesCount: Int = 0
)
