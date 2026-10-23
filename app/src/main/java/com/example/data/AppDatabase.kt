package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.DossierDao
import com.example.data.dao.EvidenceDao
import com.example.data.dao.FarmerDao
import com.example.data.dao.PlotDao
import com.example.data.model.Converters
import com.example.data.model.DossierRecord
import com.example.data.model.EvidenceEntry
import com.example.data.model.FarmerProfile
import com.example.data.model.Plot

@Database(
    entities = [
        Plot::class,
        EvidenceEntry::class,
        FarmerProfile::class,
        DossierRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plotDao(): PlotDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun farmerDao(): FarmerDao
    abstract fun dossierDao(): DossierDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jalsaakshya_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
