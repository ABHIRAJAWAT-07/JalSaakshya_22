package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FarmerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmer_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<FarmerProfile?>

    @Query("SELECT * FROM farmer_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): FarmerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: FarmerProfile)
}
