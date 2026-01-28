package com.example.environmentmonitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmentmonitor.model.AppDatabase
import com.example.environmentmonitor.model.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class EnvironmentViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).measurementDao()

    private val sensorManager = EnvironmentSensorManager(application)

    private val _uiState = MutableStateFlow(EnvironmentUiState())
    val uiState: StateFlow<EnvironmentUiState> = _uiState.asStateFlow()

    private var isMonitoring = false

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        sensorManager.startLocationTracking { lat, lon ->
            _uiState.update { it.copy(latitude = lat, longitude = lon) }
        }

        sensorManager.startAudioMonitoring()

        viewModelScope.launch(Dispatchers.IO) {
            while (isMonitoring) {
                val db = sensorManager.getAmplitudeInDecibels()
                _uiState.update { it.copy(currentDb = db) }
                delay(500)
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        sensorManager.stopLocationTracking()
        sensorManager.stopAudioMonitoring()
    }

    fun saveMeasurement(imagePath: String?) {
        val currentState = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val measurement = Measurement(
                timestamp = System.currentTimeMillis(),
                decibels = currentState.currentDb,
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                imagePath = imagePath
            )
            dao.insert(measurement)
        }
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            db.measurementDao().delete(measurement)

            measurement.imagePath?.let { fileName ->
                val file = File(getApplication<Application>().filesDir, fileName)
                if (file.exists()) file.delete()
            }

        }
    }


    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

data class EnvironmentUiState(
    val currentDb: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

