package com.example.barriovivo.data.database.dao

import androidx.room.*
import com.example.barriovivo.data.database.entity.ChatConversationEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO para operaciones de base de datos relacionadas con conversaciones de chat.
 *
 * Una conversacion se crea cuando un usuario reclama una comida publicada por otro.
 * Cada conversacion tiene dos participantes: el creador del post y el reclamante.
 *
 * Funcionalidades principales:
 * - Crear y gestionar conversaciones entre usuarios
 * - Controlar contadores de mensajes no leidos por cada participante
 * - Actualizar estado de ultima actividad
 * - Cerrar conversaciones inactivas
 *
 * @see ChatConversationEntity
 * @see com.example.barriovivo.data.repository.ChatRepository
 */
@Dao
interface ChatConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ChatConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ChatConversationEntity)

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ChatConversationEntity?

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    fun getConversationByIdFlow(conversationId: String): Flow<ChatConversationEntity?>

    // Obtener conversaciones donde el usuario es creador o reclamante
    @Query("SELECT * FROM chat_conversations WHERE creatorUserId = :userId OR claimerUserId = :userId ORDER BY lastMessageAt DESC")
    fun getUserConversations(userId: String): Flow<List<ChatConversationEntity>>

    // Obtener conversaciones activas del usuario
    @Query("SELECT * FROM chat_conversations WHERE (creatorUserId = :userId OR claimerUserId = :userId) AND isActive = 1 ORDER BY lastMessageAt DESC")
    fun getUserActiveConversations(userId: String): Flow<List<ChatConversationEntity>>

    // Verificar si ya existe conversación para un post y reclamante específico
    @Query("SELECT * FROM chat_conversations WHERE mealPostId = :mealPostId AND claimerUserId = :claimerUserId LIMIT 1")
    suspend fun getExistingConversation(mealPostId: String, claimerUserId: String): ChatConversationEntity?

    // Alias para compatibilidad con ChatRepository
    @Query("SELECT * FROM chat_conversations WHERE mealPostId = :mealPostId AND claimerUserId = :claimerUserId LIMIT 1")
    suspend fun getConversationByMealPostAndClaimer(mealPostId: String, claimerUserId: String): ChatConversationEntity?

    // Obtener conversación por mealPostId
    @Query("SELECT * FROM chat_conversations WHERE mealPostId = :mealPostId LIMIT 1")
    suspend fun getConversationByMealPostId(mealPostId: String): ChatConversationEntity?

    // Actualizar último mensaje y fecha
    @Query("UPDATE chat_conversations SET lastMessage = :lastMessage, lastMessageAt = :lastMessageAt WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, lastMessageAt: LocalDateTime)

    // Actualizar info del último mensaje
    @Query("UPDATE chat_conversations SET lastMessageAt = :lastMessageAt, lastMessage = :lastMessage WHERE id = :conversationId")
    suspend fun updateLastMessageInfo(conversationId: String, lastMessageAt: LocalDateTime, lastMessage: String)

    // Incrementar contador de no leídos para el creador
    @Query("UPDATE chat_conversations SET unreadCountCreator = unreadCountCreator + 1 WHERE id = :conversationId")
    suspend fun incrementUnreadCreator(conversationId: String)

    // Incrementar contador de no leídos para el reclamante
    @Query("UPDATE chat_conversations SET unreadCountClaimer = unreadCountClaimer + 1 WHERE id = :conversationId")
    suspend fun incrementUnreadClaimer(conversationId: String)

    // Actualizar contador de no leídos para el creador
    @Query("UPDATE chat_conversations SET unreadCountCreator = :count WHERE id = :conversationId")
    suspend fun updateUnreadCountCreator(conversationId: String, count: Int)

    // Actualizar contador de no leídos para el reclamante
    @Query("UPDATE chat_conversations SET unreadCountClaimer = :count WHERE id = :conversationId")
    suspend fun updateUnreadCountClaimer(conversationId: String, count: Int)

    // Resetear contador de no leídos para el creador
    @Query("UPDATE chat_conversations SET unreadCountCreator = 0 WHERE id = :conversationId")
    suspend fun resetUnreadCreator(conversationId: String)

    // Resetear contador de no leídos para el reclamante
    @Query("UPDATE chat_conversations SET unreadCountClaimer = 0 WHERE id = :conversationId")
    suspend fun resetUnreadClaimer(conversationId: String)

    // Obtener total de mensajes no leídos para un usuario
    @Query("SELECT SUM(CASE WHEN creatorUserId = :userId THEN unreadCountCreator WHEN claimerUserId = :userId THEN unreadCountClaimer ELSE 0 END) FROM chat_conversations WHERE creatorUserId = :userId OR claimerUserId = :userId")
    fun getTotalUnreadCount(userId: String): Flow<Int?>

    // Obtener todas las conversaciones activas (para cerrar inactivas)
    @Query("SELECT * FROM chat_conversations WHERE isActive = 1")
    fun getAllActiveConversations(): Flow<List<ChatConversationEntity>>

    // Cerrar conversación
    @Query("UPDATE chat_conversations SET isActive = 0, closedAt = :closedAt WHERE id = :conversationId")
    suspend fun closeConversation(conversationId: String, closedAt: LocalDateTime)
}

