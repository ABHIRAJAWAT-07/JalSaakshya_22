package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Plot
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY createdAt DESC")
    fun getAllPlots(): Flow<List<Plot>>

    @Query("SELECT * FROM plots WHERE id = :id LIMIT 1")
    suspend fun getPlotById(id: String): Plot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: Plot)

    @Update
    suspend fun updatePlot(plot: Plot)

    @Delete
    suspend fun deletePlot(plot: Plot)

    @Query("DELETE FROM plots WHERE id = :id")
    suspend fun deletePlotById(id: String)
}
