package com.example.barriovivo.data.repository

import com.example.barriovivo.data.database.dao.ChatConversationDao
import com.example.barriovivo.data.database.dao.ChatMessageDao
import com.example.barriovivo.data.database.entity.ChatConversationEntity
import com.example.barriovivo.data.database.entity.ChatMessageEntity
import com.example.barriovivo.domain.model.ChatConversation
import com.example.barriovivo.domain.model.ChatMessage
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
        creatorUserId: String,
        claimerUserId: String
    ): Result<ChatConversation> = try {
        val conversationId = UUID.randomUUID().toString()
        val conversation = ChatConversationEntity(
            id = conversationId,
            mealPostId = mealPostId,
            creatorUserId = creatorUserId,
            claimerUserId = claimerUserId,
            createdAt = LocalDateTime.now(),
            lastMessageAt = LocalDateTime.now()
        )
        conversationDao.insertConversation(conversation)
        Result.success(conversation.toDomain())
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
            sentAt = LocalDateTime.now()
        )
        messageDao.insertMessage(message)

        // Actualizar timestamp de última actividad
        conversationDao.updateLastMessageAt(conversationId, LocalDateTime.now())

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

    fun getConversationMessages(conversationId: String): Flow<List<ChatMessage>> {
        return messageDao.getConversationMessages(conversationId).map { messages ->
            messages.map { it.toDomain() }
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
            creatorUserId = creatorUserId,
            claimerUserId = claimerUserId,
            createdAt = createdAt,
            lastMessageAt = lastMessageAt,
            isActive = isActive,
            closedAt = closedAt,
            unreadCountCreator = unreadCountCreator,
            unreadCountClaimer = unreadCountClaimer
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
}

