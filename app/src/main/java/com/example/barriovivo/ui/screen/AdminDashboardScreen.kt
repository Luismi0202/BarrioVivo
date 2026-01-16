package com.example.barriovivo.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.ui.theme.*
import com.example.barriovivo.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🛡️ Panel de Admin",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenDark
                ),
                actions = {
                    // Notificaciones
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                    // Perfil
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                    // Cerrar sesión
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Stats card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚠️",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "${uiState.reportedPosts.size}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        Text(
                            text = "Reportados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }
            }

            // Título sección
            Text(
                text = "Posts Reportados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (uiState.reportedPosts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay reportes pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                        Text(
                            text = "¡Todo está bajo control!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.reportedPosts) { post ->
                        ReportedPostCard(
                            mealPost = post,
                            onApprove = { viewModel.approveReportedPost(post.id) },
                            onDelete = { reason -> viewModel.deletePost(post.id, reason) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportedPostCard(
    mealPost: MealPost,
    onApprove: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteReason by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Badge de reporte
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRed.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Reportado ${mealPost.reportCount} ${if (mealPost.reportCount == 1) "vez" else "veces"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Carrusel de imágenes
            if (mealPost.photoUris.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { mealPost.photoUris.size })

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = mealPost.photoUris[page],
                            contentDescription = "Foto ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Indicador de fotos
                    if (mealPost.photoUris.size > 1) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(mealPost.photoUris.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == pagerState.currentPage)
                                                    Color.White
                                                else
                                                    Color.White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Contenido
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = mealPost.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = GreenPrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "👤 ${mealPost.userName}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenDark
                        )
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = OrangePrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📍 ${mealPost.location.city}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary
                        )
                    }
                }

                if (mealPost.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mealPost.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        maxLines = 3
                    )
                }

                // Motivo del reporte
                if (mealPost.lastReportReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📝 Último motivo de reporte:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mealPost.lastReportReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Aprobar (mantener visible)
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mantener")
                    }

                    // Borrar
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar")
                    }
                }
            }
        }
    }

    // Diálogo para borrar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "🗑️ Borrar publicación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Por qué borras esta publicación?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Motivo (se enviará al usuario)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(deleteReason)
                        showDeleteDialog = false
                        deleteReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteReason = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Mantener compatibilidad con MainActivity existente
@Composable
fun AdminDashboardScreen(
    pendingPosts: List<MealPost> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    successMessage: String? = null,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onApprove: (postId: String) -> Unit = {},
    onReject: (postId: String, reason: String) -> Unit = { _, _ -> },
    onErrorDismiss: () -> Unit = {},
    onSuccessDismiss: () -> Unit = {}
) {
    // Usar el nuevo componente con ViewModel
    AdminDashboardScreen(
        onLogout = onBack,
        onProfileClick = onProfileClick,
        onNotificationsClick = {}
    )
}

