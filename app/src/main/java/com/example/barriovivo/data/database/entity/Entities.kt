package com.example.barriovivo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.barriovivo.domain.model.MealPostStatus
import com.example.barriovivo.domain.model.UserRole
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val passwordHash: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val zipCode: String,
    val role: String = UserRole.USER.name,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "meal_posts")
data class MealPostEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val title: String,
    val description: String,
    val photoUris: String, // Almacenado como JSON string separado por comas
    val expiryDate: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: String = MealPostStatus.PENDING.name,
    val adminComment: String = "",
    val isAvailable: Boolean = true,
    val claimedByUserId: String? = null,
    val claimedAt: LocalDateTime? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val relatedPostId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val userId: String
)

@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey
    val id: String,
    val mealPostId: String,
    val creatorUserId: String,
    val claimerUserId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastMessageAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val closedAt: LocalDateTime? = null,
    val unreadCountCreator: Int = 0,
    val unreadCountClaimer: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

