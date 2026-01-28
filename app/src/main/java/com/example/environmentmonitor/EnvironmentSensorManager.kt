package com.example.environmentmonitor

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import kotlin.math.log10
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.environmentmonitor.model.Measurement

class EnvironmentSensorManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationTracking(onLocationReceived: (Double, Double) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude)
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    onLocationReceived(it.latitude, it.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private var mediaRecorder: MediaRecorder? = null

    fun startAudioMonitoring() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (mediaRecorder == null) {
            val tempFile = File(context.cacheDir, "temp_audio.3gp")

            try {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(tempFile.absolutePath)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mediaRecorder = null
            }
        }
    }

    fun getAmplitudeInDecibels(): Double {
        return mediaRecorder?.let { recorder ->
            val amplitude = recorder.maxAmplitude
            if (amplitude > 0) {
                20 * log10(amplitude.toDouble())
            } else {
                0.0
            }
        } ?: 0.0
    }

    fun stopAudioMonitoring() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
        } finally {
            mediaRecorder = null
        }
    }
}

fun exportMeasurements(context: Context, measurements: List<Measurement>) {
    val reportText = StringBuilder("Raport Monitora Środowiskowego\n\n")
    measurements.forEach {
        reportText.append("Data: ${java.util.Date(it.timestamp)}\n")
        reportText.append("Hałas: ${it.decibels} dB\n")
        reportText.append("Lokalizacja: ${it.latitude}, ${it.longitude}\n")
        reportText.append("Mapa: https://www.google.com/maps?q=${it.latitude},${it.longitude}\n")
        reportText.append("---------------------------\n")
    }

    val file = File(context.cacheDir, "raport_pomiarow.txt")
    file.writeText(reportText.toString())

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Raport z pomiarów środowiskowych")
        putExtra(Intent.EXTRA_TEXT, "W załączniku przesyłam zebrane dane pomiarowe.")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Wyślij raport przez..."))
}