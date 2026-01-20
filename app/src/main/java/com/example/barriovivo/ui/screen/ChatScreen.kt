package com.example.barriovivo.ui.screen

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.barriovivo.domain.model.ChatConversation
import com.example.barriovivo.domain.model.ChatMessage
import com.example.barriovivo.domain.model.ChatMessageWithMedia
import com.example.barriovivo.domain.model.MessageType
import com.example.barriovivo.ui.theme.*
import com.example.barriovivo.ui.viewmodel.AuthViewModel
import com.example.barriovivo.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onConversationClick: (String) -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val currentUserId = authState.currentUser?.id ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadConversations()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "💬 Mis Chats",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tienes conversaciones activas",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                        Text(
                            "Reclama una comida para empezar a chatear",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            currentUserId = currentUserId,
                            onClick = { onConversationClick(conversation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ChatConversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val unreadCount = if (currentUserId == conversation.creatorUserId) {
        conversation.unreadCountCreator
    } else {
        conversation.unreadCountClaimer
    }

    val isCreator = currentUserId == conversation.creatorUserId
    val otherUserName = if (isCreator) {
        conversation.claimerUserName.ifEmpty { "Usuario" }
    } else {
        conversation.creatorUserName.ifEmpty { "Usuario" }
    }
    val displayTitle = otherUserName
    val mealTitle = conversation.mealPostTitle.ifEmpty { "Comida" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = otherUserName.firstOrNull()?.uppercase() ?: "👤",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = TextDark,
                        maxLines = 1
                    )
                    Text(
                        text = "🍽️ $mealTitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenDark,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (conversation.lastMessage.isNotEmpty()) {
                        Text(
                            text = conversation.lastMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = formatDateTime(conversation.lastMessageAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatDateTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(GreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    conversationId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val messagesWithMedia by viewModel.currentMessagesWithMedia.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val currentUserId = authState.currentUser?.id ?: ""

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showCloseDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Estado para grabación de audio
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
    val audioFile = remember { mutableStateOf<File?>(null) }

    // Estado para reproducción de audio
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    val otherUserName = currentConversation?.let { conv ->
        if (currentUserId == conv.creatorUserId) {
            conv.claimerUserName.ifEmpty { "Usuario" }
        } else {
            conv.creatorUserName.ifEmpty { "Usuario" }
        }
    } ?: "Chat"

    val mealTitle = currentConversation?.mealPostTitle ?: ""

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        viewModel.markMessagesAsRead(conversationId)
    }

    LaunchedEffect(messagesWithMedia.size) {
        if (messagesWithMedia.isNotEmpty()) {
            listState.animateScrollToItem(messagesWithMedia.size - 1)
        }
    }

    // Contador de tiempo de grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0
            while (isRecording) {
                delay(1000)
                recordingTime++
            }
        }
    }

    // Limpiar recursos al salir
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder.value?.release()
            mediaPlayer.value?.release()
        }
    }

    // Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { viewModel.sendImage(conversationId, it.toString()) }
    }

    // Camera launcher
    val outputUriState = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success: Boolean ->
        if (success) {
            outputUriState.value?.let { viewModel.sendImage(conversationId, it.toString()) }
        }
    }

    // Funciones de grabación de audio
    fun startRecording() {
        try {
            val audioDir = File(context.cacheDir, "audio")
            if (!audioDir.exists()) audioDir.mkdirs()
            val file = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            audioFile.value = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder.value = recorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecordingAndSend() {
        try {
            mediaRecorder.value?.apply {
                stop()
                release()
            }
            mediaRecorder.value = null
            isRecording = false

            audioFile.value?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        file
                    )
                    viewModel.sendAudio(conversationId, uri.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder.value?.apply {
                stop()
                release()
            }
            mediaRecorder.value = null
            isRecording = false
            audioFile.value?.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            otherUserName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        if (mealTitle.isNotEmpty()) {
                            Text(
                                "🍽️ $mealTitle",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar conversación", color = ErrorRed) },
                            onClick = {
                                showMenu = false
                                showCloseDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed)
                            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0))
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messagesWithMedia.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👋", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "¡Empieza la conversación!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
                items(messagesWithMedia) { message ->
                    MessageBubbleWithMedia(
                        message = message,
                        isCurrentUser = message.senderId == currentUserId,
                        onPlayAudio = { uri ->
                            if (currentlyPlayingUri == uri) {
                                // Parar reproducción
                                mediaPlayer.value?.stop()
                                mediaPlayer.value?.release()
                                mediaPlayer.value = null
                                currentlyPlayingUri = null
                            } else {
                                // Iniciar reproducción
                                mediaPlayer.value?.release()
                                mediaPlayer.value = MediaPlayer().apply {
                                    setDataSource(context, Uri.parse(uri))
                                    prepare()
                                    start()
                                    setOnCompletionListener {
                                        currentlyPlayingUri = null
                                    }
                                }
                                currentlyPlayingUri = uri
                            }
                        },
                        isPlaying = currentlyPlayingUri
                    )
                }
            }

            // Barra de entrada de mensajes
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Indicador de grabación
                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Grabando... ${recordingTime}s",
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(onClick = { cancelRecording() }) {
                                Text("Cancelar", color = TextGray)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Botones de media (galería y cámara)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón galería
                            IconButton(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    )
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Photo,
                                    contentDescription = "Galería",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Botón cámara
                            IconButton(
                                onClick = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    val imagesDir = File(context.cacheDir, "images")
                                    if (!imagesDir.exists()) imagesDir.mkdirs()
                                    val imageFile = File(imagesDir, "IMG_${System.currentTimeMillis()}.jpg")
                                    val photoUri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".fileprovider",
                                        imageFile
                                    )
                                    outputUriState.value = photoUri
                                    cameraLauncher.launch(photoUri)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Cámara",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Campo de texto
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 120.dp),
                            placeholder = { Text("Escribe un mensaje...", color = TextGray) },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color(0xFFF8F8F8),
                                unfocusedContainerColor = Color(0xFFF8F8F8)
                            ),
                            maxLines = 4,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        // Botón de audio (mantener pulsado) o enviar
                        if (messageText.isBlank()) {
                            // Botón de audio
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) ErrorRed else GreenPrimary)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                // Solicitar permiso y empezar a grabar
                                                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                                startRecording()
                                                // Esperar a que se suelte
                                                tryAwaitRelease()
                                                // Al soltar, enviar
                                                if (isRecording) {
                                                    stopRecordingAndSend()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Grabar audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            // Botón de enviar
                            FloatingActionButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(conversationId, messageText)
                                        messageText = ""
                                    }
                                },
                                containerColor = GreenPrimary,
                                contentColor = Color.White,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para cerrar conversación
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Cerrar conversación") },
            text = {
                Text("¿Estás seguro de que quieres cerrar esta conversación? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeConversation(conversationId)
                        showCloseDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Cerrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MessageBubbleWithMedia(
    message: ChatMessageWithMedia,
    isCurrentUser: Boolean,
    onPlayAudio: (String) -> Unit,
    isPlaying: String?
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = if (isCurrentUser) GreenPrimary else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        color = GreenDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when (message.type) {
                    MessageType.IMAGE -> {
                        // Mostrar imagen con Coil
                        message.mediaUri?.let { uri ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagen",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (message.message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.message,
                                color = if (isCurrentUser) Color.White else TextDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    MessageType.AUDIO -> {
                        // Reproductor de audio
                        message.mediaUri?.let { uri ->
                            val isThisPlaying = isPlaying == uri
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isCurrentUser) Color.White.copy(alpha = 0.2f)
                                        else GreenPrimary.copy(alpha = 0.1f)
                                    )
                                    .clickable { onPlayAudio(uri) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisPlaying) "Parar" else "Reproducir",
                                    tint = if (isCurrentUser) Color.White else GreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "🎤 Mensaje de voz",
                                        color = if (isCurrentUser) Color.White else TextDark,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isThisPlaying) "Reproduciendo..." else "Toca para reproducir",
                                        color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else TextGray,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.message,
                            color = if (isCurrentUser) Color.White else TextDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.sentAt),
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = if (isCurrentUser) GreenPrimary else Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        color = GreenDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.message,
                    color = if (isCurrentUser) Color.White else TextDark,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.sentAt),
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    return dateTime.format(formatter)
}

private fun formatTime(dateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(formatter)
}

