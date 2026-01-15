package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.ui.component.ErrorMessage
import com.example.barriovivo.ui.component.LoadingScreen
import com.example.barriovivo.ui.theme.ErrorRed
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.OrangePrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    pendingPosts: List<MealPost> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    successMessage: String? = null,
    onBack: () -> Unit = {},
    onApprove: (postId: String) -> Unit = {},
    onReject: (postId: String, reason: String) -> Unit = { _, _ -> },
    onErrorDismiss: () -> Unit = {},
    onSuccessDismiss: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Moderación") },
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
        ) {
            // Error message
            if (!error.isNullOrEmpty()) {
                ErrorMessage(message = error, onDismiss = onErrorDismiss)
            }

            // Success message
            if (!successMessage.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = successMessage,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onSuccessDismiss, modifier = Modifier.width(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                LoadingScreen()
            } else if (pendingPosts.isEmpty()) {
                Text(
                    text = "No hay publicaciones pendientes",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            } else {
                LazyColumn {
                    items(pendingPosts) { post ->
                        AdminMealPostCard(
                            mealPost = post,
                            onApprove = { onApprove(post.id) },
                            onReject = { reason -> onReject(post.id, reason) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMealPostCard(
    mealPost: MealPost,
    onApprove: () -> Unit = {},
    onReject: (String) -> Unit = {}
) {
    var showRejectReason by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = mealPost.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextDark
            )

            Text(
                text = "Por: ${mealPost.userName}",
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Descripción: ${mealPost.description}",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Ubicación: ${mealPost.location.city}",
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Caduca: ${mealPost.expiryDate}",
                style = MaterialTheme.typography.labelSmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showRejectReason) {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Motivo del rechazo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Aprobar")
                }

                if (showRejectReason) {
                    Button(
                        onClick = {
                            if (rejectReason.isNotEmpty()) {
                                onReject(rejectReason)
                                showRejectReason = false
                                rejectReason = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Enviar")
                    }
                } else {
                    Button(
                        onClick = { showRejectReason = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

