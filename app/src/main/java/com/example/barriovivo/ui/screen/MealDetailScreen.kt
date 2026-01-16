package com.example.barriovivo.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.theme.ErrorRed
import com.example.barriovivo.ui.theme.GreenDark
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.OrangePrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MealDetailScreen(
    mealPost: MealPost? = null,
    isLoading: Boolean = false,
    isClaimLoading: Boolean = false,
    claimSuccess: Boolean = false,
    error: String? = null,
    currentUserId: String = "",
    isReportLoading: Boolean = false,
    reportSuccess: Boolean = false,
    onBack: () -> Unit = {},
    onClaimClick: () -> Unit = {},
    onGoToChat: (String) -> Unit = {},
    onReportClick: (reason: String) -> Unit = {}
) {
    var showClaimSuccessDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showReportSuccessDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    LaunchedEffect(claimSuccess) {
        if (claimSuccess) {
            showClaimSuccessDialog = true
        }
    }

    LaunchedEffect(reportSuccess) {
        if (reportSuccess) {
            showReportSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Botón de reportar (solo si no es el propio usuario)
                    if (mealPost != null && mealPost.userId != currentUserId) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = "Reportar",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            mealPost == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Comida no encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Carrusel de fotos
                    if (mealPost.photoUris.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { mealPost.photoUris.size })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
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

                            // Gradiente inferior
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                        )
                                    )
                            )

                            // Indicador de página
                            if (mealPost.photoUris.size > 1) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Black.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(mealPost.photoUris.size) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
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

                            // Badge de estado
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (mealPost.isAvailable) GreenPrimary else TextGray
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (mealPost.isAvailable) "✓ Disponible" else "Reclamada",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GreenPrimary.copy(alpha = 0.3f), GreenDark.copy(alpha = 0.5f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍽️", fontSize = 64.sp)
                        }
                    }

                    // Contenido
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Título
                        Text(
                            text = mealPost.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Ubicación
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = GreenPrimary.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = mealPost.location.city,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GreenDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Fecha caducidad
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = OrangePrimary.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${mealPost.expiryDate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Descripción
                        if (mealPost.description.isNotBlank()) {
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = mealPost.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextGray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Publicado por
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Publicado por",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGray
                                    )
                                    Text(
                                        text = mealPost.userName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }

                        // Error message
                        if (!error.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = ErrorRed.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón de reclamar (solo si está disponible y no es el propio usuario)
                        if (mealPost.isAvailable && mealPost.userId != currentUserId) {
                            Button(
                                onClick = onClaimClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isClaimLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (isClaimLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "¡Quiero esta comida!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (!mealPost.isAvailable) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = TextGray.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Esta comida ya ha sido reclamada",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextGray
                                    )
                                }
                            }
                        } else if (mealPost.userId == currentUserId) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = GreenPrimary.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ Esta es tu publicación",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = GreenDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Diálogo de éxito al reclamar
    if (showClaimSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showClaimSuccessDialog = false },
            icon = {
                Text("🎉", fontSize = 48.sp)
            },
            title = {
                Text(
                    "¡Comida reclamada!",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    "Se ha abierto un chat con el propietario de la comida. Coordina la recogida con él.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClaimSuccessDialog = false
                        // Ir al chat si hay conversationId
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ir al chat")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClaimSuccessDialog = false
                    onBack()
                }) {
                    Text("Volver")
                }
            }
        )
    }

    // Diálogo para reportar
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Reportar publicación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Por qué quieres reportar esta publicación?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Motivo del reporte") },
                        placeholder = { Text("Ej: Comida en mal estado, información falsa...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            onReportClick(reportReason)
                            showReportDialog = false
                            reportReason = ""
                        }
                    },
                    enabled = reportReason.isNotBlank() && !isReportLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    if (isReportLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar reporte")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    reportReason = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de éxito al reportar
    if (showReportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showReportSuccessDialog = false },
            icon = {
                Text("✅", fontSize = 48.sp)
            },
            title = {
                Text(
                    "Reporte enviado",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    "Gracias por ayudarnos a mantener la comunidad segura. Un administrador revisará tu reporte.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showReportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}
