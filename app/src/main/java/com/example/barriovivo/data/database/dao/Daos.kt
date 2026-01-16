package com.example.barriovivo.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.barriovivo.data.database.entity.UserEntity
import com.example.barriovivo.data.database.entity.MealPostEntity
import com.example.barriovivo.data.database.entity.NotificationEntity
import com.example.barriovivo.data.database.entity.AdminEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface MealPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPost(mealPost: MealPostEntity)

    @Query("SELECT * FROM meal_posts WHERE id = :id")
    suspend fun getMealPostById(id: String): MealPostEntity?

    @Query("SELECT * FROM meal_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserMealPosts(userId: String): Flow<List<MealPostEntity>>

    // Ahora busca posts ACTIVE (no necesitan aprobación)
    @Query("SELECT * FROM meal_posts WHERE status = 'ACTIVE' AND expiryDate >= :todayDate AND isAvailable = 1 ORDER BY createdAt DESC")
    fun getActiveMealPosts(todayDate: LocalDate): Flow<List<MealPostEntity>>

    // Posts reportados para el admin
    @Query("SELECT * FROM meal_posts WHERE status = 'REPORTED' ORDER BY reportCount DESC, createdAt DESC")
    fun getReportedMealPosts(): Flow<List<MealPostEntity>>

    // Todos los posts activos para el admin (puede ver todo)
    @Query("SELECT * FROM meal_posts WHERE status != 'DELETED' ORDER BY createdAt DESC")
    fun getAllActivePosts(): Flow<List<MealPostEntity>>

    @Query("UPDATE meal_posts SET isAvailable = 0, claimedByUserId = :userId, claimedAt = :claimedAt WHERE id = :postId")
    suspend fun claimMealPost(postId: String, userId: String, claimedAt: java.time.LocalDateTime)

    // Reportar un post
    @Query("UPDATE meal_posts SET reportCount = reportCount + 1, status = 'REPORTED', reportedByUsers = :reportedByUsers, lastReportReason = :reason WHERE id = :postId")
    suspend fun reportMealPost(postId: String, reportedByUsers: String, reason: String)

    // Aprobar post reportado (mantenerlo activo)
    @Query("UPDATE meal_posts SET status = 'ACTIVE', reportCount = 0, reportedByUsers = '', lastReportReason = '' WHERE id = :postId")
    suspend fun approveReportedPost(postId: String)

    // Marcar como borrado
    @Query("UPDATE meal_posts SET status = 'DELETED', adminComment = :reason WHERE id = :postId")
    suspend fun markAsDeleted(postId: String, reason: String)

    @Update
    suspend fun updateMealPost(mealPost: MealPostEntity)

    @Delete
    suspend fun deleteMealPost(mealPost: MealPostEntity)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}

@Dao
interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity)

    @Query("SELECT * FROM admins WHERE email = :email")
    suspend fun getAdminByEmail(email: String): AdminEntity?

    @Query("SELECT * FROM admins")
    suspend fun getAllAdmins(): List<AdminEntity>
}

@Dao
interface ChatConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: com.example.barriovivo.data.database.entity.ChatConversationEntity)

    @Query("SELECT * FROM chat_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): com.example.barriovivo.data.database.entity.ChatConversationEntity?

    @Query("SELECT * FROM chat_conversations WHERE (creatorUserId = :userId OR claimerUserId = :userId) AND isActive = 1 ORDER BY lastMessageAt DESC")
    fun getUserActiveConversations(userId: String): Flow<List<com.example.barriovivo.data.database.entity.ChatConversationEntity>>

    @Query("SELECT * FROM chat_conversations WHERE mealPostId = :mealPostId")
    suspend fun getConversationByMealPostId(mealPostId: String): com.example.barriovivo.data.database.entity.ChatConversationEntity?

    @Query("UPDATE chat_conversations SET isActive = 0, closedAt = :closedAt WHERE id = :conversationId")
    suspend fun closeConversation(conversationId: String, closedAt: java.time.LocalDateTime)

    @Query("UPDATE chat_conversations SET unreadCountCreator = :count WHERE id = :conversationId")
    suspend fun updateUnreadCountCreator(conversationId: String, count: Int)

    @Query("UPDATE chat_conversations SET unreadCountClaimer = :count WHERE id = :conversationId")
    suspend fun updateUnreadCountClaimer(conversationId: String, count: Int)

    @Query("UPDATE chat_conversations SET lastMessageAt = :timestamp WHERE id = :conversationId")
    suspend fun updateLastMessageAt(conversationId: String, timestamp: java.time.LocalDateTime)

    @Update
    suspend fun updateConversation(conversation: com.example.barriovivo.data.database.entity.ChatConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: com.example.barriovivo.data.database.entity.ChatConversationEntity)
}

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: com.example.barriovivo.data.database.entity.ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY sentAt ASC")
    fun getConversationMessages(conversationId: String): Flow<List<com.example.barriovivo.data.database.entity.ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId AND senderId != :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(conversationId: String, userId: String): Int

    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :userId")
    suspend fun markMessagesAsRead(conversationId: String, userId: String)

    @Delete
    suspend fun deleteMessage(message: com.example.barriovivo.data.database.entity.ChatMessageEntity)
}

