package com.example.environmentmonitor

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.environmentmonitor.model.Measurement
import java.io.File
import android.content.Intent
import android.net.Uri

@Composable
fun DashboardScreen(
    vm: EnvironmentViewModel = viewModel(),
    onNavigateToHistory: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    // --- 1. Obsługa Uprawnień Startowych (Lokalizacja + Mikrofon) ---
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    )

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Sprawdzamy, czy kluczowe uprawnienia zostały przyznane
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        if (locationGranted && audioGranted) {
            // Jeśli mamy zgody, startujemy sensory
            vm.startMonitoring()
        }
    }

    // Uruchamiamy pytanie o uprawnienia RAZ przy starcie ekranu
    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            vm.startMonitoring()
        } else {
            multiplePermissionsLauncher.launch(permissionsToRequest)
        }
    }

    // Zatrzymujemy monitoring przy wyjściu z ekranu
    DisposableEffect(Unit) {
        onDispose { vm.stopMonitoring() }
    }

    // --- 2. Obsługa Aparatu (Twoja stara logika, lekko poprawiona) ---
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            vm.saveMeasurement(imagePath = fileName)
        }
    }

    // Launcher dla samego aparatu (gdy klikamy przycisk)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Monitor Środowiskowy", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        // Wyświetlanie danych
        Text("Poziom hałasu: ${"%.2f".format(uiState.currentDb)} dB", style = MaterialTheme.typography.titleLarge)
        Text("Lat: ${uiState.latitude}", style = MaterialTheme.typography.bodyMedium)
        Text("Lon: ${uiState.longitude}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))

        Button(onClick = {
            // Sprawdzamy uprawnienie tylko do Kamery (pozostałe powinny być już nadane na starcie)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text("Zrób zdjęcie i zapisz pomiar")
        }

        OutlinedButton(onClick = onNavigateToHistory, modifier = Modifier.padding(top = 16.dp)) {
            Text("Zobacz historię")
        }
    }
}



@Composable
fun HistoryScreen(vm: EnvironmentViewModel = viewModel()) {
    val context = LocalContext.current

    // Zmieniamy produceState, abyśmy mogli ręcznie modyfikować 'value' po usunięciu
    val measurementsState = produceState<List<Measurement>>(initialValue = emptyList()) {
        value = com.example.environmentmonitor.model.AppDatabase
            .getDatabase(vm.getApplication()).measurementDao().getAllMeasurements()
    }

    // Wyciągamy listę do zmiennej pomocniczej dla czytelności
    val measurements = measurementsState.value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Historia pomiarów", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = { exportMeasurements(context, measurements) }) {
                Text("Eksportuj")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(measurements) { item ->
                // ZMIANA: Dodano parametr onClick do Card
                Card(
                    onClick = {
                        // Tworzymy URI zgodne ze standardem Androida dla map
                        // geo:lat,lon?q=lat,lon(Label) - to pozwoli postawić pinezkę
                        val uri =
                            Uri.parse("geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(Pomiar)")
                        val intent = Intent(Intent.ACTION_VIEW, uri)

                        // Próbujemy uruchomić aktywność (Mapy)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Opcjonalnie: obsługa błędu, np. brak aplikacji map
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- WYŚWIETLANIE ZDJĘCIA (bez zmian) ---
                        if (item.imagePath != null) {
                            val file = File(context.filesDir, item.imagePath)
                            if (file.exists()) {
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Zdjęcie pomiaru",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 8.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(80.dp).padding(end = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null)
                            }
                        }

                        // --- DANE TEKSTOWE (bez zmian) ---
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Hałas: ${"%.1f".format(item.decibels)} dB",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Lokalizacja: ${item.latitude}, ${item.longitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Data: ${java.util.Date(item.timestamp)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            // Dodatkowa wskazówka dla użytkownika
                            Text(
                                "Kliknij, aby zobaczyć mapę",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // --- PRZYCISK USUWANIA (bez zmian) ---
                        IconButton(onClick = {
                            vm.deleteMeasurement(item)
                            (measurementsState as MutableState).value =
                                measurements.filter { it != item }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń pomiar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

