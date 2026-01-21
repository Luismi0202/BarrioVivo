package com.example.barriovivo.data.database.dao

import androidx.room.*
import com.example.barriovivo.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos relacionadas con mensajes de chat.
 *
 * Soporta tres tipos de mensajes:
 * - TEXT: Mensajes de texto plano
 * - IMAGE: Imagenes capturadas o seleccionadas de galeria
 * - AUDIO: Notas de voz grabadas
 *
 * Los mensajes se almacenan con referencia a su conversacion padre
 * y mantienen estado de lectura individual.
 *
 * @see ChatMessageEntity
 * @see com.example.barriovivo.domain.model.MessageType
 */
@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    // Obtener mensajes de una conversación ordenados por fecha
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY sentAt ASC")
    fun getConversationMessages(conversationId: String): Flow<List<ChatMessageEntity>>

    // Marcar mensaje como leído
    @Query("UPDATE chat_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    // Marcar todos los mensajes de una conversación como leídos (excepto los del usuario actual)
    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :currentUserId")
    suspend fun markAllAsRead(conversationId: String, currentUserId: String)

    // Alias usado por ChatRepository
    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :userId")
    suspend fun markMessagesAsRead(conversationId: String, userId: String)

    // Obtener número de mensajes no leídos en una conversación
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId AND isRead = 0 AND senderId != :currentUserId")
    suspend fun getUnreadCount(conversationId: String, currentUserId: String): Int

    // Obtener el último mensaje de una conversación
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY sentAt DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): ChatMessageEntity?

    // Eliminar todos los mensajes de una conversación
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversationMessages(conversationId: String)
}

