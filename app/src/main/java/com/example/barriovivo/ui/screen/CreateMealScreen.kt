package com.example.barriovivo.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.ErrorMessage
import com.example.barriovivo.ui.component.ExpiryDateWarning
import com.example.barriovivo.ui.theme.BackgroundLight
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.OrangePrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMealScreen(
    onBack: () -> Unit = {},
    onCreateClick: (title: String, description: String, photoUri: String, expiryDate: LocalDate, location: String) -> Unit = { _, _, _, _, _ -> },
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    expiryDateError: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expiryDateText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Publicación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Error message
            if (!error.isNullOrEmpty()) {
                ErrorMessage(message = error, onDismiss = onErrorDismiss)
            }

            // Expiry date warning
            ExpiryDateWarning()

            Spacer(modifier = Modifier.height(16.dp))

            // Photo selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Foto",
                            tint = TextGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Selecciona una foto",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text("Foto seleccionada", color = GreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cámara"
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Cámara")
                }
                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Galería"
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Galería")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre de la comida *") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expiry Date
            OutlinedTextField(
                value = expiryDateText,
                onValueChange = { expiryDateText = it },
                label = { Text("Fecha de caducidad (dd/MM/yyyy) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !expiryDateError.isNullOrEmpty()
            )
            if (!expiryDateError.isNullOrEmpty()) {
                Text(
                    text = expiryDateError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Create button
            BarrioVivoButton(
                text = "Publicar Comida",
                onClick = {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val expiryDate = LocalDate.parse(expiryDateText, formatter)
                        onCreateClick(title, description, photoUri, expiryDate, location)
                    } catch (e: Exception) {
                        // Error will be handled in viewmodel
                    }
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

