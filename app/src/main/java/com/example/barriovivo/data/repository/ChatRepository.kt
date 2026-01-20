package com.example.barriovivo.data.repository

import com.example.barriovivo.data.database.dao.ChatConversationDao
import com.example.barriovivo.data.database.dao.ChatMessageDao
import com.example.barriovivo.data.database.entity.ChatConversationEntity
import com.example.barriovivo.data.database.entity.ChatMessageEntity
import com.example.barriovivo.domain.model.ChatConversation
import com.example.barriovivo.domain.model.ChatMessage
import com.example.barriovivo.domain.model.ChatMessageWithMedia
import com.example.barriovivo.domain.model.MessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val conversationDao: ChatConversationDao,
    private val messageDao: ChatMessageDao
) {

    suspend fun createConversation(
        mealPostId: String,
        mealPostTitle: String,
        creatorUserId: String,
        creatorUserName: String,
        claimerUserId: String,
        claimerUserName: String
    ): Result<ChatConversation> = try {
        // Verificar si ya existe una conversación para este post y reclamante
        val existingConversation = conversationDao.getConversationByMealPostAndClaimer(mealPostId, claimerUserId)
        if (existingConversation != null && existingConversation.isActive) {
            Result.success(existingConversation.toDomain())
        } else {
            val conversationId = UUID.randomUUID().toString()
            val conversation = ChatConversationEntity(
                id = conversationId,
                mealPostId = mealPostId,
                mealPostTitle = mealPostTitle,
                creatorUserId = creatorUserId,
                creatorUserName = creatorUserName,
                claimerUserId = claimerUserId,
                claimerUserName = claimerUserName,
                createdAt = LocalDateTime.now(),
                lastMessageAt = LocalDateTime.now()
            )
            conversationDao.insertConversation(conversation)
            Result.success(conversation.toDomain())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getConversationByMealPostAndClaimer(mealPostId: String, claimerUserId: String): Result<ChatConversation?> = try {
        val conversation = conversationDao.getConversationByMealPostAndClaimer(mealPostId, claimerUserId)
        Result.success(conversation?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getConversationById(conversationId: String): Result<ChatConversation?> = try {
        val conversation = conversationDao.getConversationById(conversationId)
        Result.success(conversation?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getConversationByMealPostId(mealPostId: String): Result<ChatConversation?> = try {
        val conversation = conversationDao.getConversationByMealPostId(mealPostId)
        Result.success(conversation?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserActiveConversations(userId: String): Flow<List<ChatConversation>> {
        return conversationDao.getUserActiveConversations(userId).map { conversations ->
            conversations.map { it.toDomain() }
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        messageText: String
    ): Result<ChatMessage> = try {
        val messageId = UUID.randomUUID().toString()
        val message = ChatMessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            message = messageText,
            mediaUri = null,
            messageType = MessageType.TEXT.name,
            sentAt = LocalDateTime.now()
        )
        messageDao.insertMessage(message)

        // Actualizar timestamp de última actividad y último mensaje
        conversationDao.updateLastMessageInfo(conversationId, LocalDateTime.now(), messageText)

        // Actualizar contador de no leídos
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            if (senderId == conversation.creatorUserId) {
                conversationDao.updateUnreadCountClaimer(
                    conversationId,
                    conversation.unreadCountClaimer + 1
                )
            } else {
                conversationDao.updateUnreadCountCreator(
                    conversationId,
                    conversation.unreadCountCreator + 1
                )
            }
        }

        Result.success(message.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Enviar mensaje con media (imagen o audio)
    suspend fun sendMediaMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        mediaUri: String,
        type: MessageType,
        caption: String = ""
    ): Result<ChatMessageWithMedia> = try {
        val messageId = UUID.randomUUID().toString()
        val message = ChatMessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            message = caption,
            mediaUri = mediaUri,
            messageType = type.name,
            sentAt = LocalDateTime.now()
        )
        messageDao.insertMessage(message)

        // Actualizar timestamp y último mensaje (mostrar tipo)
        val preview = when (type) {
            MessageType.IMAGE -> "📷 Foto"
            MessageType.AUDIO -> "🎤 Audio"
            else -> caption
        }
        conversationDao.updateLastMessageInfo(conversationId, LocalDateTime.now(), preview)

        // Actualizar contador de no leídos
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            if (senderId == conversation.creatorUserId) {
                conversationDao.updateUnreadCountClaimer(
                    conversationId,
                    conversation.unreadCountClaimer + 1
                )
            } else {
                conversationDao.updateUnreadCountCreator(
                    conversationId,
                    conversation.unreadCountCreator + 1
                )
            }
        }

        Result.success(message.toDomainWithMedia())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getConversationMessages(conversationId: String): Flow<List<ChatMessage>> {
        return messageDao.getConversationMessages(conversationId).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    // Obtener mensajes con información de media
    fun getConversationMessagesWithMedia(conversationId: String): Flow<List<ChatMessageWithMedia>> {
        return messageDao.getConversationMessages(conversationId).map { messages ->
            messages.map { it.toDomainWithMedia() }
        }
    }

    suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit> = try {
        messageDao.markMessagesAsRead(conversationId, userId)

        // Resetear contador de no leídos
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            if (userId == conversation.creatorUserId) {
                conversationDao.updateUnreadCountCreator(conversationId, 0)
            } else {
                conversationDao.updateUnreadCountClaimer(conversationId, 0)
            }
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun closeConversation(conversationId: String): Result<Unit> = try {
        conversationDao.closeConversation(conversationId, LocalDateTime.now())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Cerrar conversaciones del creador de un post
    suspend fun closeConversationsByCreator(creatorUserId: String, mealPostId: String): Result<Unit> = try {
        val conversation = conversationDao.getConversationByMealPostId(mealPostId)
        if (conversation != null && conversation.creatorUserId == creatorUserId) {
            conversationDao.closeConversation(conversation.id, LocalDateTime.now())
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Cerrar chats inactivos (más de 7 días sin actividad)
    suspend fun closeInactiveConversations(): Result<Int> = try {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        var closedCount = 0
        conversationDao.getAllActiveConversations().collect { conversations ->
            conversations.forEach { conv ->
                if (conv.lastMessageAt.isBefore(sevenDaysAgo) && conv.isActive) {
                    conversationDao.closeConversation(conv.id, LocalDateTime.now())
                    closedCount++
                }
            }
        }
        Result.success(closedCount)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTotalUnreadCount(userId: String): Result<Int> = try {
        val conversations = conversationDao.getUserActiveConversations(userId)
        var totalUnread = 0
        conversations.collect { convList ->
            convList.forEach { conv ->
                totalUnread += if (userId == conv.creatorUserId) {
                    conv.unreadCountCreator
                } else {
                    conv.unreadCountClaimer
                }
            }
        }
        Result.success(totalUnread)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ChatConversationEntity.toDomain(): ChatConversation {
        return ChatConversation(
            id = id,
            mealPostId = mealPostId,
            mealPostTitle = mealPostTitle,
            creatorUserId = creatorUserId,
            creatorUserName = creatorUserName,
            claimerUserId = claimerUserId,
            claimerUserName = claimerUserName,
            createdAt = createdAt,
            lastMessageAt = lastMessageAt,
            isActive = isActive,
            closedAt = closedAt,
            unreadCountCreator = unreadCountCreator,
            unreadCountClaimer = unreadCountClaimer,
            lastMessage = lastMessage
        )
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            message = message,
            sentAt = sentAt,
            isRead = isRead
        )
    }

    private fun ChatMessageEntity.toDomainWithMedia(): ChatMessageWithMedia {
        val type = try { MessageType.valueOf(messageType) } catch (e: Exception) { MessageType.TEXT }
        return ChatMessageWithMedia(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            message = message,
            mediaUri = mediaUri,
            type = type,
            sentAt = sentAt,
            isRead = isRead
        )
    }
}
