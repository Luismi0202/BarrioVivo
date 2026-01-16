package com.example.barriovivo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.BarrioVivoTextField
import com.example.barriovivo.ui.component.ErrorMessage
import com.example.barriovivo.ui.component.PasswordTextField
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.GreenDark
import com.example.barriovivo.ui.theme.OrangePrimary
import com.example.barriovivo.ui.theme.TextGray
import com.example.barriovivo.ui.theme.ErrorRed
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.barriovivo.ui.viewmodel.AuthViewModel

val LOCATIONS = listOf(
    "Madrid",
    "Barcelona",
    "Valencia",
    "Sevilla",
    "Bilbao",
    "Zaragoza",
    "Málaga",
    "Murcia",
    "Palma de Mallorca",
    "Las Palmas",
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
    val tabs = listOf("Iniciar Sesión", "Registrarse")

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GreenPrimary.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo/Título con emoji
                Text(
                    text = "🍽️",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BarrioVivo",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Text(
                    text = "Comparte comida, reduce desperdicio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tabs mejorados
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = GreenPrimary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) GreenPrimary else TextGray
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message
                if (!error.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ $error",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
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

                Spacer(modifier = Modifier.height(24.dp))
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "👋 ¡Bienvenido de nuevo!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GreenDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        BarrioVivoTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = GreenPrimary
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

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                "¿Olvidaste tu contraseña?",
                color = GreenPrimary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showResetDialog) {
            var resetEmail by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmNewPassword by remember { mutableStateOf("") }
            var isResetPasswordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.resetPassword(resetEmail, newPassword, confirmNewPassword)
                            showResetDialog = false
                        },
                        enabled = resetEmail.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmNewPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = {
                    Text(
                        "🔐 Recuperar contraseña",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        BarrioVivoTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = "Correo electrónico",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GreenPrimary)
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
                            onTogglePasswordVisibility = { isResetPasswordVisible = !isResetPasswordVisible },
                            errorMessage = if (confirmNewPassword.isNotBlank() && newPassword != confirmNewPassword) "Las contraseñas no coinciden" else null
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTab(
    isLoading: Boolean = false,
    onRegisterClick: (email: String, password: String, confirmPassword: String, city: String, latitude: Double, longitude: Double, zipCode: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }

    val locationCoordinates = mapOf(
        "Madrid" to Pair(40.4168, -3.7038),
        "Barcelona" to Pair(41.3851, 2.1734),
        "Valencia" to Pair(39.4699, -0.3763),
        "Sevilla" to Pair(37.3891, -5.9845),
        "Bilbao" to Pair(43.2630, -2.9350),
        "Zaragoza" to Pair(41.6488, -0.8891),
        "Málaga" to Pair(36.7213, -4.4214),
        "Murcia" to Pair(37.9922, -1.1307),
        "Palma de Mallorca" to Pair(39.5696, 2.6502),
        "Las Palmas" to Pair(28.1235, -15.4363),
        "Otro" to Pair(0.0, 0.0)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "✨ Crea tu cuenta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GreenDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        BarrioVivoTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = GreenPrimary
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
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            errorMessage = if (confirmPassword.isNotBlank() && password != confirmPassword) "Las contraseñas no coinciden" else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location selector mejorado
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
            keyboardType = KeyboardType.Number,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = GreenPrimary
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val (lat, lon) = locationCoordinates[selectedCity] ?: Pair(0.0, 0.0)
                onRegisterClick(email, password, confirmPassword, selectedCity, lat, lon, zipCode)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() &&
                      confirmPassword.isNotBlank() && password == confirmPassword &&
                      selectedCity.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Crear Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
            text = "📍 Tu ubicación",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (selectedLocation.isBlank()) "Selecciona tu ciudad" else selectedLocation,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = if (selectedLocation.isBlank()) TextGray else GreenPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Cerrar" else "Abrir",
                            tint = GreenPrimary
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                location,
                                fontWeight = if (location == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                color = if (location == selectedLocation) GreenPrimary else TextGray
                            )
                        },
                        onClick = {
                            onLocationSelected(location)
                            expanded = false
                        },
                        leadingIcon = {
                            if (location == selectedLocation) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
