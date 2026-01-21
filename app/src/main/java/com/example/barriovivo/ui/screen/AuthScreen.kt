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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
    // Ciudades principales de España ordenadas alfabéticamente
    "A Coruña",
    "Albacete",
    "Alcalá de Henares",
    "Alcobendas",
    "Alcorcón",
    "Algeciras",
    "Alicante",
    "Almería",
    "Aranjuez",
    "Arrecife",
    "Ávila",
    "Avilés",
    "Badajoz",
    "Badalona",
    "Barcelona",
    "Bilbao",
    "Burgos",
    "Cáceres",
    "Cádiz",
    "Cartagena",
    "Castellón de la Plana",
    "Ceuta",
    "Ciudad Real",
    "Córdoba",
    "Cuenca",
    "Dos Hermanas",
    "Elche",
    "El Puerto de Santa María",
    "Ferrol",
    "Fuengirola",
    "Fuenlabrada",
    "Getafe",
    "Gijón",
    "Girona",
    "Granada",
    "Guadalajara",
    "Huelva",
    "Huesca",
    "Ibiza",
    "Jaén",
    "Jerez de la Frontera",
    "Las Palmas de Gran Canaria",
    "Las Rozas de Madrid",
    "Leganés",
    "León",
    "Lérida",
    "Logroño",
    "Lorca",
    "Lugo",
    "Madrid",
    "Málaga",
    "Marbella",
    "Mataró",
    "Melilla",
    "Mérida",
    "Mollet del Vallès",
    "Móstoles",
    "Murcia",
    "Orense",
    "Oviedo",
    "Palencia",
    "Palma de Mallorca",
    "Pamplona",
    "Parla",
    "Pontevedra",
    "Pozuelo de Alarcón",
    "Reus",
    "Rivas-Vaciamadrid",
    "Sabadell",
    "Salamanca",
    "San Cristóbal de La Laguna",
    "San Fernando",
    "San Sebastián",
    "Santa Coloma de Gramenet",
    "Santa Cruz de Tenerife",
    "Santander",
    "Santiago de Compostela",
    "Segovia",
    "Sevilla",
    "Soria",
    "Talavera de la Reina",
    "Tarragona",
    "Telde",
    "Terrassa",
    "Teruel",
    "Toledo",
    "Torrejón de Ardoz",
    "Torremolinos",
    "Torrent",
    "Valencia",
    "Valladolid",
    "Vélez-Málaga",
    "Vigo",
    "Vitoria-Gasteiz",
    "Zamora",
    "Zaragoza",
    "✏️ Introducir manualmente"
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

        // Mostrar errores en snackbar solo si es necesario (comentado para evitar duplicados)
        // El error ya se muestra visualmente en la interfaz
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

    // Validación de email
    val isValidEmail = email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val showEmailError = email.isNotEmpty() && !isValidEmail

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "👋 ¡Bienvenido de nuevo!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GreenDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Campo de email con validación
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = if (showEmailError) ErrorRed else GreenPrimary
                )
            },
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text("Introduce un correo electrónico válido", color = ErrorRed) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (showEmailError) ErrorRed else GreenPrimary,
                unfocusedBorderColor = if (showEmailError) ErrorRed else Color.LightGray,
                errorBorderColor = ErrorRed
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
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
        // Coordenadas de todas las ciudades de España
        "A Coruña" to Pair(43.3623, -8.4115),
        "Albacete" to Pair(38.9942, -1.8585),
        "Alcalá de Henares" to Pair(40.4818, -3.3635),
        "Alcobendas" to Pair(40.5475, -3.6420),
        "Alcorcón" to Pair(40.3456, -3.8248),
        "Algeciras" to Pair(36.1408, -5.4536),
        "Alicante" to Pair(38.3452, -0.4815),
        "Almería" to Pair(36.8340, -2.4637),
        "Aranjuez" to Pair(40.0332, -3.6028),
        "Arrecife" to Pair(28.9630, -13.5477),
        "Ávila" to Pair(40.6566, -4.6815),
        "Avilés" to Pair(43.5547, -5.9248),
        "Badajoz" to Pair(38.8794, -6.9707),
        "Badalona" to Pair(41.4501, 2.2474),
        "Barcelona" to Pair(41.3851, 2.1734),
        "Bilbao" to Pair(43.2630, -2.9350),
        "Burgos" to Pair(42.3439, -3.6969),
        "Cáceres" to Pair(39.4753, -6.3724),
        "Cádiz" to Pair(36.5271, -6.2886),
        "Cartagena" to Pair(37.6057, -0.9918),
        "Castellón de la Plana" to Pair(39.9864, -0.0513),
        "Ceuta" to Pair(35.8893, -5.3198),
        "Ciudad Real" to Pair(38.9848, -3.9274),
        "Córdoba" to Pair(37.8882, -4.7794),
        "Cuenca" to Pair(40.0704, -2.1374),
        "Dos Hermanas" to Pair(37.2838, -5.9219),
        "Elche" to Pair(38.2669, -0.6983),
        "El Puerto de Santa María" to Pair(36.5939, -6.2326),
        "Ferrol" to Pair(43.4843, -8.2328),
        "Fuengirola" to Pair(36.5397, -4.6247),
        "Fuenlabrada" to Pair(40.2839, -3.8000),
        "Getafe" to Pair(40.3057, -3.7326),
        "Gijón" to Pair(43.5453, -5.6635),
        "Girona" to Pair(41.9794, 2.8214),
        "Granada" to Pair(37.1773, -3.5986),
        "Guadalajara" to Pair(40.6337, -3.1667),
        "Huelva" to Pair(37.2614, -6.9447),
        "Huesca" to Pair(42.1401, -0.4089),
        "Ibiza" to Pair(38.9067, 1.4206),
        "Jaén" to Pair(37.7796, -3.7849),
        "Jerez de la Frontera" to Pair(36.6866, -6.1370),
        "Las Palmas de Gran Canaria" to Pair(28.1235, -15.4363),
        "Las Rozas de Madrid" to Pair(40.4929, -3.8737),
        "Leganés" to Pair(40.3281, -3.7644),
        "León" to Pair(42.5987, -5.5671),
        "Lérida" to Pair(41.6176, 0.6200),
        "Logroño" to Pair(42.4627, -2.4449),
        "Lorca" to Pair(37.6714, -1.7011),
        "Lugo" to Pair(43.0097, -7.5568),
        "Madrid" to Pair(40.4168, -3.7038),
        "Málaga" to Pair(36.7213, -4.4214),
        "Marbella" to Pair(36.5100, -4.8826),
        "Mataró" to Pair(41.5381, 2.4445),
        "Melilla" to Pair(35.2923, -2.9381),
        "Mérida" to Pair(38.9160, -6.3436),
        "Mollet del Vallès" to Pair(41.5391, 2.2143),
        "Móstoles" to Pair(40.3223, -3.8649),
        "Murcia" to Pair(37.9922, -1.1307),
        "Orense" to Pair(42.3364, -7.8638),
        "Oviedo" to Pair(43.3619, -5.8494),
        "Palencia" to Pair(42.0096, -4.5288),
        "Palma de Mallorca" to Pair(39.5696, 2.6502),
        "Pamplona" to Pair(42.8125, -1.6458),
        "Parla" to Pair(40.2360, -3.7676),
        "Pontevedra" to Pair(42.4310, -8.6444),
        "Pozuelo de Alarcón" to Pair(40.4353, -3.8145),
        "Reus" to Pair(41.1561, 1.1069),
        "Rivas-Vaciamadrid" to Pair(40.3520, -3.5416),
        "Sabadell" to Pair(41.5486, 2.1075),
        "Salamanca" to Pair(40.9701, -5.6635),
        "San Cristóbal de La Laguna" to Pair(28.4853, -16.3156),
        "San Fernando" to Pair(36.4646, -6.1996),
        "San Sebastián" to Pair(43.3183, -1.9812),
        "Santa Coloma de Gramenet" to Pair(41.4517, 2.2080),
        "Santa Cruz de Tenerife" to Pair(28.4636, -16.2518),
        "Santander" to Pair(43.4623, -3.8100),
        "Santiago de Compostela" to Pair(42.8782, -8.5448),
        "Segovia" to Pair(40.9429, -4.1088),
        "Sevilla" to Pair(37.3891, -5.9845),
        "Soria" to Pair(41.7636, -2.4649),
        "Talavera de la Reina" to Pair(39.9635, -4.8309),
        "Tarragona" to Pair(41.1189, 1.2445),
        "Telde" to Pair(27.9924, -15.4198),
        "Terrassa" to Pair(41.5630, 2.0089),
        "Teruel" to Pair(40.3456, -1.1065),
        "Toledo" to Pair(39.8628, -4.0273),
        "Torrejón de Ardoz" to Pair(40.4565, -3.4694),
        "Torremolinos" to Pair(36.6218, -4.4998),
        "Torrent" to Pair(39.4370, -0.4653),
        "Valencia" to Pair(39.4699, -0.3763),
        "Valladolid" to Pair(41.6523, -4.7245),
        "Vélez-Málaga" to Pair(36.7839, -4.1007),
        "Vigo" to Pair(42.2328, -8.7226),
        "Vitoria-Gasteiz" to Pair(42.8467, -2.6726),
        "Zamora" to Pair(41.5034, -5.7467),
        "Zaragoza" to Pair(41.6488, -0.8891),
        "✏️ Introducir manualmente" to Pair(0.0, 0.0)
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
    var searchQuery by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
    var manualCity by remember { mutableStateOf("") }

    // Filtrar ciudades según la búsqueda
    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            locations
        } else {
            locations.filter {
                it.lowercase().contains(searchQuery.lowercase())
            }
        }
    }

    Column {
        Text(
            text = "📍 Tu ubicación",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (showManualInput) {
            // Campo para introducir ciudad manualmente
            OutlinedTextField(
                value = manualCity,
                onValueChange = {
                    manualCity = it
                    onLocationSelected(it)
                },
                label = { Text("Escribe tu ciudad") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = GreenPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        showManualInput = false
                        manualCity = ""
                        onLocationSelected("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Volver a la lista",
                            tint = TextGray
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            Text(
                text = "💡 Escribe el nombre de tu ciudad y pulsa en el botón para volver a la lista",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
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
                    onDismissRequest = {
                        expanded = false
                        searchQuery = ""
                    },
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    // Campo de búsqueda dentro del dropdown
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔍 Buscar ciudad...", color = TextGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Lista de ciudades filtradas
                    filteredLocations.forEach { location ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    location,
                                    fontWeight = if (location == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                    color = if (location == selectedLocation) GreenPrimary else TextGray
                                )
                            },
                            onClick = {
                                if (location == "✏️ Introducir manualmente") {
                                    showManualInput = true
                                    expanded = false
                                    searchQuery = ""
                                } else {
                                    onLocationSelected(location)
                                    expanded = false
                                    searchQuery = ""
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (location == "✏️ Introducir manualmente") Icons.Default.Edit else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (location == selectedLocation) GreenPrimary else TextGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    // Mensaje si no hay resultados
                    if (filteredLocations.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No se encontraron ciudades. Prueba '✏️ Introducir manualmente'",
                                    color = TextGray.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            onClick = {
                                showManualInput = true
                                expanded = false
                                searchQuery = ""
                            }
                        )
                    }
                }
            }
        }
    }
}
