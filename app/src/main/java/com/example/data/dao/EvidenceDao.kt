package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EvidenceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence_entries ORDER BY capturedAt DESC")
    fun getAllEvidence(): Flow<List<EvidenceEntry>>

    @Query("SELECT * FROM evidence_entries WHERE plotId = :plotId ORDER BY capturedAt ASC")
    fun getEvidenceForPlot(plotId: String): Flow<List<EvidenceEntry>>

    @Query("SELECT * FROM evidence_entries WHERE plotId = :plotId ORDER BY capturedAt ASC")
    suspend fun getEvidenceForPlotSync(plotId: String): List<EvidenceEntry>

    @Query("SELECT * FROM evidence_entries WHERE id = :id LIMIT 1")
    suspend fun getEvidenceById(id: String): EvidenceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(entry: EvidenceEntry)

    @Delete
    suspend fun deleteEvidence(entry: EvidenceEntry)

    @Query("DELETE FROM evidence_entries WHERE id = :id")
    suspend fun deleteEvidenceById(id: String)

    @Query("DELETE FROM evidence_entries WHERE plotId = :plotId")
    suspend fun deleteEvidenceForPlot(plotId: String)
}
