package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DossierRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DossierDao {
    @Query("SELECT * FROM dossiers ORDER BY generatedAt DESC")
    fun getAllDossiers(): Flow<List<DossierRecord>>

    @Query("SELECT * FROM dossiers WHERE id = :id LIMIT 1")
    suspend fun getDossierById(id: String): DossierRecord?

    @Query("SELECT * FROM dossiers WHERE plotId = :plotId ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestDossierForPlot(plotId: String): DossierRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDossier(dossier: DossierRecord)

    @Delete
    suspend fun deleteDossier(dossier: DossierRecord)

    @Query("DELETE FROM dossiers WHERE id = :id")
    suspend fun deleteDossierById(id: String)
}
