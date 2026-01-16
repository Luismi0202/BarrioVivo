package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.ChatRepository
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.ChatConversation
import com.example.barriovivo.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadConversations()
    }

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
            chatRepository.getConversationMessages(conversationId).collect { messages ->
                _currentMessages.value = messages
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

