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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.BarrioVivoTextField
import com.example.barriovivo.ui.theme.ErrorRed
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.GreenDark
import com.example.barriovivo.ui.theme.OrangePrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray
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

    // Fondo semi-transparente para efecto de diálogo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) { padding ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header con indicador de drag
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextGray.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🍽️ Publicar Comida",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Text(
                                text = "Comparte lo que te sobra",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .background(TextGray.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Título (obligatorio)
                    Text(
                        text = "Nombre de la comida *",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            localError = null
                        },
                        placeholder = { Text("Ej: Pizza margarita, Ensalada...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Descripción (opcional)
                    Text(
                        text = "Descripción (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Detalles sobre la comida, ingredientes...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Fecha de caducidad (obligatoria)
                    Text(
                        text = "Fecha de caducidad *",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (expiryDate != null) GreenPrimary else TextGray
                        )
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (expiryDate != null) {
                                "📅 ${expiryDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                            } else {
                                "Seleccionar fecha"
                            },
                            fontWeight = if (expiryDate != null) FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Fotos (obligatorio al menos una)
                    Text(
                        text = "Fotos *",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Foto principal
                    if (photoUris.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(model = photoUris.first()),
                                    contentDescription = "Foto principal",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Botón para eliminar
                                IconButton(
                                    onClick = { photoUris = photoUris.drop(1) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Badge de foto principal
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = GreenPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Foto principal",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Miniaturas de fotos adicionales
                    if (photoUris.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(photoUris.drop(1)) { uri ->
                                Card(
                                    modifier = Modifier.size(80.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = uri),
                                            contentDescription = "Foto",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { photoUris = photoUris.filter { it != uri } },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Botones de añadir fotos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    pickImagesLauncher.launch("image/*")
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Error al abrir galería") }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galería", color = GreenPrimary)
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cámara", color = OrangePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avisos de validación en rojo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Requisitos para publicar:",
                                style = MaterialTheme.typography.labelMedium,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (photoUris.isNotEmpty()) "✅" else "❌",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Debe haber al menos una foto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (photoUris.isNotEmpty()) GreenDark else ErrorRed
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (expiryDate != null) "✅" else "❌",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Debe mostrar la fecha de caducidad",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (expiryDate != null) GreenDark else ErrorRed
                                )
                            }
                        }
                    }

                    // Errores
                    if (localError != null || uiState.error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = localError ?: uiState.error ?: "",
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }

                        val canPublish = title.isNotBlank() && photoUris.isNotEmpty() && expiryDate != null && currentUser != null && !uiState.isLoading

                        Button(
                            onClick = {
                                if (currentUser == null) {
                                    localError = "Debes iniciar sesión para publicar"
                                    return@Button
                                }
                                if (title.isBlank()) {
                                    localError = "El nombre de la comida es obligatorio"
                                    return@Button
                                }
                                if (photoUris.isEmpty()) {
                                    localError = "Debes añadir al menos una foto"
                                    return@Button
                                }
                                if (expiryDate == null) {
                                    localError = "Debes seleccionar la fecha de caducidad"
                                    return@Button
                                }

                                localError = null
                                viewModel.clearError()

                                viewModel.createMealPost(
                                    userId = currentUser.id,
                                    userName = currentUser.email,
                                    title = title,
                                    description = description,
                                    photoUris = photoUris.map { it.toString() },
                                    expiryDate = expiryDate!!,
                                    location = currentUser.location
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = canPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Publicar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Observar el éxito de la publicación
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Publicado con éxito! 🎉")
            }
            kotlinx.coroutines.delay(800)
            viewModel.clearError()
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
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            expiryDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
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
                title = {
                    Text(
                        "📅 Selecciona fecha de caducidad",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}
