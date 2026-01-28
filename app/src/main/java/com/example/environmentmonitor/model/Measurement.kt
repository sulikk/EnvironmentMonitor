package com.example.environmentmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val decibels: Double,      // Sensor 1: Mikrofon [cite: 59]
    val latitude: Double,      // Sensor 2: GPS [cite: 58]
    val longitude: Double,     // Sensor 2: GPS [cite: 58]
    val imagePath: String?     // Sensor 3: Aparat [cite: 60]
)