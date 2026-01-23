package com.example.barriovivo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.barriovivo.data.repository.AppStatistics
import com.example.barriovivo.ui.theme.*
import com.example.barriovivo.ui.viewmodel.ReportViewModel

/**
 * Pantalla de informes y estadísticas para administradores.
 *
 * Muestra estadísticas en tiempo real de la aplicación y permite
 * generar y exportar informes en diferentes formatos (CSV, TXT).
 *
 * @param viewModel ViewModel que gestiona la lógica de informes
 * @param onBack Callback para volver a la pantalla anterior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showReportDialog by remember { mutableStateOf(false) }

    // Mostrar mensajes
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
                        "📊 Informes y Estadísticas",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenDark
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadStatistics() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de resumen principal
                item {
                    uiState.statistics?.let { stats ->
                        MainStatsCard(stats)
                    }
                }

                // Tarjeta de publicaciones
                item {
                    uiState.statistics?.let { stats ->
                        PostsStatsCard(stats)
                    }
                }

                // Tarjeta de chat
                item {
                    uiState.statistics?.let { stats ->
                        ChatStatsCard(stats)
                    }
                }

                // Tarjeta de publicaciones por ciudad
                item {
                    uiState.statistics?.let { stats ->
                        if (stats.postsByCity.isNotEmpty()) {
                            CityStatsCard(stats.postsByCity)
                        }
                    }
                }

                // Tarjeta de usuarios activos
                item {
                    uiState.statistics?.let { stats ->
                        if (stats.topActiveUsers.isNotEmpty()) {
                            TopUsersCard(stats.topActiveUsers)
                        }
                    }
                }

                // Botones de exportación
                item {
                    ExportButtonsCard(
                        onExportCSV = { viewModel.exportPostsToCSV() },
                        onExportSummary = { viewModel.exportSummaryToFile() },
                        onViewReport = {
                            viewModel.generateSummaryReport()
                            showReportDialog = true
                        }
                    )
                }

                // Información del archivo exportado
                item {
                    uiState.exportedFilePath?.let { path ->
                        ExportedFileInfo(path)
                    }
                }
            }
        }
    }

    // Diálogo con el informe de texto
    if (showReportDialog && uiState.summaryReport != null) {
        ReportDialog(
            reportContent = uiState.summaryReport!!,
            onDismiss = {
                showReportDialog = false
                viewModel.clearSummaryReport()
            }
        )
    }
}

@Composable
private fun MainStatsCard(stats: AppStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📈 Resumen General",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    emoji = "📦",
                    value = stats.totalPosts.toString(),
                    label = "Total Posts",
                    color = GreenPrimary
                )
                StatItem(
                    emoji = "✅",
                    value = stats.activePosts.toString(),
                    label = "Activos",
                    color = GreenPrimary
                )
                StatItem(
                    emoji = "⚠️",
                    value = stats.reportedPosts.toString(),
                    label = "Reportados",
                    color = OrangePrimary
                )
            }
        }
    }
}

@Composable
private fun PostsStatsCard(stats: AppStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🍽️ Estado de Publicaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatRow(label = "Publicaciones activas", value = stats.activePosts, color = GreenPrimary)
            StatRow(label = "Reclamadas", value = stats.claimedPosts, color = Color(0xFF2196F3))
            StatRow(label = "Reportadas", value = stats.reportedPosts, color = OrangePrimary)
            StatRow(label = "Eliminadas por admin", value = stats.deletedPosts, color = ErrorRed)
            StatRow(label = "Expiradas", value = stats.expiredPosts, color = TextGray)
        }
    }
}

@Composable
private fun ChatStatsCard(stats: AppStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💬 Comunicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatRow(label = "Total conversaciones", value = stats.totalConversations, color = GreenPrimary)
            StatRow(label = "Conversaciones activas", value = stats.activeConversations, color = Color(0xFF2196F3))
        }
    }
}

@Composable
private fun CityStatsCard(postsByCity: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📍 Publicaciones por Ciudad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            postsByCity.entries.take(5).forEachIndexed { index, (city, count) ->
                val barWidth = (count.toFloat() / (postsByCity.values.maxOrNull() ?: 1))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        color = TextDark
                    )
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(20.dp)
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(barWidth)
                                .background(GreenPrimary, RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark
                    )
                }
            }
        }
    }
}

@Composable
private fun TopUsersCard(topUsers: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🏆 Usuarios Más Activos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            topUsers.forEachIndexed { index, (user, count) ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "${index + 1}."
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = medal,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = user,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        color = TextDark
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$count posts",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = GreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportButtonsCard(
    onExportCSV: () -> Unit,
    onExportSummary: () -> Unit,
    onViewReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📤 Exportar Informes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewReport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Informe Completo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExportCSV,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onExportSummary,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TXT", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ExportedFileInfo(path: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Archivo exportado",
                    style = MaterialTheme.typography.labelMedium,
                    color = GreenDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun ReportDialog(
    reportContent: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📊 Informe de Estadísticas",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = reportContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun StatItem(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 28.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextDark
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

