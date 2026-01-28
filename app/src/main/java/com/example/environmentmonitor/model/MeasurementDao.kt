package com.example.environmentmonitor.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement)

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    suspend fun getAllMeasurements(): List<Measurement>

    @Delete
    suspend fun delete(measurement: Measurement)
}