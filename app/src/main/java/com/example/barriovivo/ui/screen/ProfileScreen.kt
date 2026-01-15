package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.barriovivo.domain.model.User
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User? = null,
    onBack: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // User avatar/initials
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = GreenPrimary
                ) {
                    Text(
                        text = user.email.first().uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Email
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextDark
                )
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = GreenPrimary
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }

                // Location
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextDark
                )
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = GreenPrimary
                    )
                    Column {
                        Text(
                            text = user.location.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                        if (user.location.zipCode.isNotEmpty()) {
                            Text(
                                text = "CP: ${user.location.zipCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }
                    }
                }

                // Role
                Text(
                    text = "Rol",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextDark
                )
                Text(
                    text = when (user.role) {
                        com.example.barriovivo.domain.model.UserRole.ADMIN -> "Administrador"
                        com.example.barriovivo.domain.model.UserRole.USER -> "Usuario"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Logout button
                BarrioVivoButton(
                    text = "Cerrar Sesión",
                    onClick = onLogoutClick,
                    isPrimary = false
                )
            }
        } else {
            Text("Usuario no encontrado")
        }
    }
}
