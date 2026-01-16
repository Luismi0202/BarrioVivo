package com.example.barriovivo.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val location: Location = Location(),
    val role: UserRole = UserRole.USER,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    ADMIN, USER
}

data class Location(
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "Spain",
    val zipCode: String = ""
)

data class MealPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(), // Múltiples fotos
    val expiryDate: LocalDate = LocalDate.now(),
    val location: Location = Location(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: MealPostStatus = MealPostStatus.PENDING,
    val adminComment: String = "",
    val isAvailable: Boolean = true, // Si está disponible para reclamar
    val claimedByUserId: String? = null, // Usuario que la reclamó
    val claimedAt: LocalDateTime? = null // Fecha de reclamación
)

enum class MealPostStatus {
    PENDING, APPROVED, REJECTED
}

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val relatedPostId: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

enum class NotificationType {
    POST_APPROVED, POST_REJECTED, NEW_NEARBY_POST, INFO,
    FOOD_CLAIMED, // Cuando alguien reclama tu comida
    NEW_MESSAGE, // Cuando recibes un mensaje
    CHAT_CLOSED, // Cuando se cierra un chat
    POST_DELETED_BY_ADMIN // Cuando un admin borra tu post
}

data class Admin(
    val id: String = "",
    val email: String = "",
    val userId: String = ""
)

// Modelos de Chat
data class ChatConversation(
    val id: String = "",
    val mealPostId: String = "",
    val creatorUserId: String = "", // Usuario que creó el post
    val claimerUserId: String = "", // Usuario que reclamó la comida
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastMessageAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val closedAt: LocalDateTime? = null,
    val unreadCountCreator: Int = 0, // Mensajes sin leer del creador
    val unreadCountClaimer: Int = 0 // Mensajes sin leer del reclamante
)

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

