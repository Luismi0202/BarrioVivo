package com.example.barriovivo.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.BarrioVivoTextField
import com.example.barriovivo.ui.theme.ErrorRed
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.viewmodel.CreateMealViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMealScreen(
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: CreateMealViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Obtener usuario actual
    val authViewModel: com.example.barriovivo.ui.viewmodel.AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    var photoUris by remember { mutableStateOf(listOf<Uri>()) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Permisos
    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
            }
        }
    )

    // Launcher para seleccionar múltiples imágenes de la galería
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                photoUris = (photoUris + uris).distinct()
            }
        }
    )

    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val imageFile = File(imagesDir, "IMG_${timeStamp}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                pendingCameraUri?.let { uri ->
                    photoUris = (photoUris + uri).distinct()
                }
            }
        }
    )

    // DatePicker state
    val datePickerState = rememberDatePickerState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Publicar Comida",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título (obligatorio)
            Text(
                text = "Nombre de la comida *",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            BarrioVivoTextField(
                value = title,
                onValueChange = {
                    title = it
                    localError = null
                },
                label = "Ej: Pizza margarita"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción (opcional)
            Text(
                text = "Descripción (opcional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detalles sobre la comida") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha de caducidad (obligatoria)
            Text(
                text = "Fecha de caducidad *",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (expiryDate != null) {
                        expiryDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } else {
                        "Seleccionar fecha"
                    }
                )
            }
            if (expiryDate == null) {
                Text(
                    text = "Debes mostrar la fecha de caducidad",
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fotos (obligatorio al menos una)
            Text(
                text = "Fotos *",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Foto principal
            if (photoUris.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = photoUris.first()),
                        contentDescription = "Foto principal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Miniaturas de fotos
            if (photoUris.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photoUris.drop(1)) { uri ->
                        Card(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = "Foto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botones de añadir fotos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            pickImagesLauncher.launch("image/*")
                        } catch (_: Exception) {
                            scope.launch { snackbarHostState.showSnackbar("Error al abrir galería") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería")
                }
                OutlinedButton(
                    onClick = {
                        requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                        try {
                            val uri = createImageUri(context)
                            pendingCameraUri = uri
                            takePictureLauncher.launch(uri)
                        } catch (_: Exception) {
                            scope.launch { snackbarHostState.showSnackbar("Error al abrir cámara") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cámara")
                }
            }

            if (photoUris.isEmpty()) {
                Text(
                    text = "Debe haber al menos una foto",
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            } else if (photoUris.isNotEmpty()) {
                TextButton(
                    onClick = { photoUris = emptyList() },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Quitar todas las fotos")
                }
            }

            // Errores
            if (localError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = localError!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                val canPublish = title.isNotBlank() && photoUris.isNotEmpty() && expiryDate != null && currentUser != null && !uiState.isLoading
                BarrioVivoButton(
                    text = if (uiState.isLoading) "Publicando..." else "Publicar",
                    isLoading = uiState.isLoading,
                    enabled = canPublish,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (currentUser == null) {
                            localError = "Debes iniciar sesión para publicar"
                            scope.launch {
                                snackbarHostState.showSnackbar("Debes iniciar sesión")
                            }
                            return@BarrioVivoButton
                        }
                        if (title.isBlank()) {
                            localError = "El nombre de la comida es obligatorio"
                            return@BarrioVivoButton
                        }
                        if (photoUris.isEmpty()) {
                            localError = "Debes añadir al menos una foto"
                            return@BarrioVivoButton
                        }
                        if (expiryDate == null) {
                            localError = "Debes seleccionar la fecha de caducidad"
                            return@BarrioVivoButton
                        }

                        // Limpiar errores previos
                        localError = null
                        viewModel.clearError()

                        // Log para debug
                        println("📤 Publicando comida: $title")
                        println("👤 Usuario: ${currentUser.email}")
                        println("📍 Ubicación: ${currentUser.location.city}")
                        println("📸 Fotos: ${photoUris.size}")

                        // Usar datos reales del usuario
                        viewModel.createMealPost(
                            userId = currentUser.id,
                            userName = currentUser.email,
                            title = title,
                            description = description,
                            photoUris = photoUris.map { it.toString() },
                            expiryDate = expiryDate!!,
                            location = currentUser.location
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Observar el éxito de la publicación
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Publicado con éxito!")
            }
            kotlinx.coroutines.delay(500) // Pequeño delay para que se vea el snackbar
            viewModel.clearError() // Limpiar estado
            onClose()
        }
    }

    // Observar errores
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            localError = uiState.error
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        expiryDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Selecciona fecha de caducidad") }
            )
        }
    }
}
