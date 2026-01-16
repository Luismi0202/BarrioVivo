package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.BarrioVivoTextField
import com.example.barriovivo.ui.component.ErrorMessage
import com.example.barriovivo.ui.component.PasswordTextField
import com.example.barriovivo.ui.theme.GreenPrimary
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.barriovivo.ui.viewmodel.AuthViewModel

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
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Login", "Registro")

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
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

        // Mostrar errores en snackbar local
        LaunchedEffect(error) {
            if (!error.isNullOrEmpty()) {
                snackbarHostState.showSnackbar(error)
            }
        }
    }
}

@Composable
fun LoginTab(
    isLoading: Boolean = false,
    onLoginClick: (email: String, password: String) -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }

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

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            errorMessage = null
        )

        Spacer(modifier = Modifier.height(24.dp))

        BarrioVivoButton(
            text = "Iniciar Sesión",
            onClick = { onLoginClick(email, password) },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { showResetDialog = true }) {
            Text("¿Olvidaste tu contraseña?")
        }

        if (showResetDialog) {
            var resetEmail by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmNewPassword by remember { mutableStateOf("") }
            var isResetPasswordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        authViewModel.resetPassword(resetEmail, newPassword, confirmNewPassword)
                        showResetDialog = false
                    }) { Text("Actualizar") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Recuperar contraseña") },
                text = {
                    Column {
                        BarrioVivoTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = "Email",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PasswordTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Nueva contraseña",
                            isPasswordVisible = isResetPasswordVisible,
                            onTogglePasswordVisibility = { isResetPasswordVisible = !isResetPasswordVisible }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PasswordTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = "Confirmar nueva contraseña",
                            isPasswordVisible = isResetPasswordVisible,
                            onTogglePasswordVisibility = { isResetPasswordVisible = !isResetPasswordVisible }
                        )
                    }
                }
            )
        }
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
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf(LOCATIONS[0]) }
    var zipCode by remember { mutableStateOf("") }

    val locationCoordinates = mapOf(
        "Madrid" to Pair(40.4168, -3.7038),
        "Barcelona" to Pair(41.3851, 2.1734),
        "Valencia" to Pair(39.4699, -0.3763),
        "Sevilla" to Pair(37.3891, -5.9845),
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

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            errorMessage = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar Contraseña",
            isPasswordVisible = isPasswordVisible, // sincronizado con el mismo estado
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            errorMessage = null
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

@OptIn(ExperimentalMaterial3Api::class)
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

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLocation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecciona tu ciudad") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Cerrar" else "Abrir"
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .onGloballyPositioned { /* anchor correcto */ }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            onLocationSelected(location)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
