package com.example.environmentmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val decibels: Double,    
    val latitude: Double,     
    val longitude: Double,     
    val imagePath: String?    
)
