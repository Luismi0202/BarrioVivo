package com.example.barriovivo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.component.PasswordTextField
import com.example.barriovivo.ui.theme.ErrorRed
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray
import com.example.barriovivo.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()
    val user = uiState.currentUser

    var showChangePassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar y nombre
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(GreenPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = GreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre del usuario (extraído del email o nombre real si existe)
                        Text(
                            text = user.name.ifBlank { user.email.substringBefore("@") },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Ubicación del usuario
                        if (user.location.city.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = user.location.city,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = GreenPrimary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Rol: ${user.role}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Opciones
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileOption(
                            icon = Icons.Default.Lock,
                            title = "Cambiar contraseña",
                            onClick = { showChangePassword = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        ProfileOption(
                            icon = Icons.Default.Logout,
                            title = "Cerrar sesión",
                            onClick = onLogout
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        ProfileOption(
                            icon = Icons.Default.Delete,
                            title = "Eliminar cuenta",
                            textColor = ErrorRed,
                            iconColor = ErrorRed,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Usuario no encontrado")
            }
        }
    }

    // Dialogs existentes...
    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { current, new ->
                if (user != null) {
                    authViewModel.changePassword(user.id, current, new)
                }
                showChangePassword = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                if (user != null) {
                    authViewModel.deleteAccount(user.id)
                }
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color = TextDark,
    iconColor: Color = GreenPrimary,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    // Validaciones
    val isNewPasswordValid = newPassword.length >= 6
    val doPasswordsMatch = newPassword == confirmNewPassword
    val canSubmit = currentPassword.isNotBlank() &&
                    newPassword.isNotBlank() &&
                    confirmNewPassword.isNotBlank() &&
                    isNewPasswordValid &&
                    doPasswordsMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (canSubmit) {
                        onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = canSubmit
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Contraseña actual",
                    isPasswordVisible = isVisible,
                    onTogglePasswordVisibility = { isVisible = !isVisible }
                )
                PasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nueva contraseña",
                    isPasswordVisible = isVisible,
                    onTogglePasswordVisibility = { isVisible = !isVisible }
                )
                // Mostrar error si la nueva contraseña es muy corta
                if (newPassword.isNotEmpty() && !isNewPasswordValid) {
                    Text(
                        text = "La contraseña debe tener al menos 6 caracteres",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                PasswordTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = "Confirmar nueva contraseña",
                    isPasswordVisible = isVisible,
                    onTogglePasswordVisibility = { isVisible = !isVisible }
                )
                // Mostrar error si las contraseñas no coinciden
                if (confirmNewPassword.isNotEmpty() && !doPasswordsMatch) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var timer by remember { mutableStateOf(10) }

    LaunchedEffect(Unit) {
        while (timer > 0) {
            kotlinx.coroutines.delay(1000)
            timer--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = timer == 0,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ErrorRed
                )
            ) {
                Text(if (timer == 0) "Eliminar" else "Eliminar (${timer}s)")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("⚠️ Eliminar cuenta") },
        text = {
            Text("¿Seguro que quieres eliminar la cuenta? Esta acción no se puede deshacer y perderás todos tus datos.")
        }
    )
}
