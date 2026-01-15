package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.BarrioVivoTextField
import com.example.barriovivo.ui.component.ErrorMessage
import com.example.barriovivo.ui.theme.GreenPrimary

val LOCATIONS = listOf(
    "Madrid",
    "Barcelona",
    "Valencia",
    "Sevilla",
    "Bilbao",
    "Otro"
)

@Composable
fun AuthScreen(
    isLoading: Boolean = false,
    error: String? = null,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: (email: String, password: String, confirmPassword: String, city: String, latitude: Double, longitude: Double, zipCode: String) -> Unit,
    onErrorDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Login", "Registro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Título
        Text(
            text = "BarrioVivo",
            style = MaterialTheme.typography.headlineLarge,
            color = GreenPrimary,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (!error.isNullOrEmpty()) {
            ErrorMessage(message = error, onDismiss = onErrorDismiss)
        }

        // Content
        when (selectedTab) {
            0 -> LoginTab(
                isLoading = isLoading,
                onLoginClick = onLoginClick
            )
            1 -> RegisterTab(
                isLoading = isLoading,
                onRegisterClick = onRegisterClick
            )
        }
    }
}

@Composable
fun LoginTab(
    isLoading: Boolean = false,
    onLoginClick: (email: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        BarrioVivoTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarrioVivoTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPassword = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña"
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        BarrioVivoButton(
            text = "Iniciar Sesión",
            onClick = { onLoginClick(email, password) },
            isLoading = isLoading
        )
    }
}

@Composable
fun RegisterTab(
    isLoading: Boolean = false,
    onRegisterClick: (email: String, password: String, confirmPassword: String, city: String, latitude: Double, longitude: Double, zipCode: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf(LOCATIONS[0]) }
    var zipCode by remember { mutableStateOf("") }

    val locationCoordinates = mapOf(
        "Madrid" to Pair(40.4168, -3.7038),
        "Barcelona" to Pair(41.3851, 2.1734),
        "Valencia" to Pair(39.4699, -0.3763),
        "Sevilla" to Pair(37.3891, -5.9844),
        "Bilbao" to Pair(43.2630, -2.9350),
        "Otro" to Pair(0.0, 0.0)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        BarrioVivoTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarrioVivoTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPassword = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña"
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarrioVivoTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar Contraseña",
            isPassword = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirmar Contraseña"
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location selector (simple dropdown)
        LocationSelector(
            locations = LOCATIONS,
            selectedLocation = selectedCity,
            onLocationSelected = { selectedCity = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarrioVivoTextField(
            value = zipCode,
            onValueChange = { zipCode = it },
            label = "Código Postal",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(32.dp))

        BarrioVivoButton(
            text = "Registrarse",
            onClick = {
                val (lat, lon) = locationCoordinates[selectedCity] ?: Pair(0.0, 0.0)
                onRegisterClick(email, password, confirmPassword, selectedCity, lat, lon, zipCode)
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun LocationSelector(
    locations: List<String>,
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Ubicación",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BarrioVivoTextField(
            value = selectedLocation,
            onValueChange = {},
            label = "Selecciona tu ciudad",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación"
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

