package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.ChatRepository
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.ChatConversation
import com.example.barriovivo.domain.model.ChatMessage
import com.example.barriovivo.domain.model.ChatMessageWithMedia
import com.example.barriovivo.domain.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestion del sistema de chat.
 *
 * Funcionalidades principales:
 * - Carga de conversaciones del usuario
 * - Carga de mensajes por conversacion
 * - Envio de mensajes de texto y multimedia
 * - Control de mensajes no leidos
 * - Marcado automatico como leido al abrir conversacion
 *
 * @property chatRepository Repositorio para operaciones de chat
 * @property userRepository Repositorio para obtener usuario actual
 * @property mealPostRepository Repositorio para datos de publicaciones
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val mealPostRepository: MealPostRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    private val _currentMessagesWithMedia = MutableStateFlow<List<ChatMessageWithMedia>>(emptyList())
    val currentMessagesWithMedia: StateFlow<List<ChatMessageWithMedia>> = _currentMessagesWithMedia.asStateFlow()

    private val _currentConversation = MutableStateFlow<ChatConversation?>(null)
    val currentConversation: StateFlow<ChatConversation?> = _currentConversation.asStateFlow()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadConversations()
    }

    /**
     * Carga las conversaciones activas del usuario actual.
     */
    fun loadConversations() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.getUserActiveConversations(currentUser.id).collect { convList ->
                    _conversations.value = convList
                    updateTotalUnreadCount(currentUser.id, convList)
                }
            }
        }
    }

    private fun updateTotalUnreadCount(userId: String, conversations: List<ChatConversation>) {
        val total = conversations.sumOf { conv ->
            if (userId == conv.creatorUserId) {
                conv.unreadCountCreator
            } else {
                conv.unreadCountClaimer
            }
        }
        _totalUnreadCount.value = total
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            // Cargar la conversación para obtener info de los participantes
            chatRepository.getConversationById(conversationId).onSuccess { conversation ->
                _currentConversation.value = conversation
            }

            // Suscribirse a mensajes con media
            chatRepository.getConversationMessagesWithMedia(conversationId).collect { messages ->
                _currentMessagesWithMedia.value = messages
                // También actualizar la lista legacy para compatibilidad
                _currentMessages.value = messages.map { msg ->
                    ChatMessage(
                        id = msg.id,
                        conversationId = msg.conversationId,
                        senderId = msg.senderId,
                        senderName = msg.senderName,
                        message = msg.message,
                        sentAt = msg.sentAt,
                        isRead = msg.isRead
                    )
                }
            }
        }
    }

    fun sendMessage(conversationId: String, messageText: String) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null && messageText.isNotBlank()) {
                val result = chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUser.id,
                    senderName = currentUser.email,
                    messageText = messageText.trim()
                )

                if (result.isFailure) {
                    _errorMessage.value = "Error al enviar mensaje"
                }
            }
        }
    }

    fun sendImage(conversationId: String, imageUri: String, caption: String = "") {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                val result = chatRepository.sendMediaMessage(
                    conversationId = conversationId,
                    senderId = currentUser.id,
                    senderName = currentUser.email,
                    mediaUri = imageUri,
                    type = MessageType.IMAGE,
                    caption = caption
                )

                if (result.isFailure) {
                    _errorMessage.value = "Error al enviar imagen"
                }
            }
        }
    }

    fun sendAudio(conversationId: String, audioUri: String, caption: String = "") {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                val result = chatRepository.sendMediaMessage(
                    conversationId = conversationId,
                    senderId = currentUser.id,
                    senderName = currentUser.email,
                    mediaUri = audioUri,
                    type = MessageType.AUDIO,
                    caption = caption
                )

                if (result.isFailure) {
                    _errorMessage.value = "Error al enviar audio"
                }
            }
        }
    }

    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.markMessagesAsRead(conversationId, currentUser.id)
            }
        }
    }

    fun closeConversation(conversationId: String) {
        viewModelScope.launch {
            val result = chatRepository.closeConversation(conversationId)
            if (result.isSuccess) {
                loadConversations()
            } else {
                _errorMessage.value = "Error al cerrar conversación"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
