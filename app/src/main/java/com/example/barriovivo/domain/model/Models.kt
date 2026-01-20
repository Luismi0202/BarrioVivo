package com.example.barriovivo.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
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
    val status: MealPostStatus = MealPostStatus.ACTIVE, // Ahora por defecto ACTIVE
    val adminComment: String = "",
    val isAvailable: Boolean = true, // Si está disponible para reclamar
    val claimedByUserId: String? = null, // Usuario que la reclamó
    val claimedAt: LocalDateTime? = null, // Fecha de reclamación
    val reportCount: Int = 0, // Número de reportes
    val reportedByUsers: List<String> = emptyList(), // IDs de usuarios que reportaron
    val lastReportReason: String = "" // Último motivo de reporte
)

enum class MealPostStatus {
    ACTIVE, // Publicado y visible (antes era APPROVED)
    REPORTED, // Ha sido reportado y está pendiente de revisión
    DELETED // Borrado por admin
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
    INFO,
    FOOD_CLAIMED, // Cuando alguien reclama tu comida
    NEW_MESSAGE, // Cuando recibes un mensaje
    CHAT_CLOSED, // Cuando se cierra un chat
    POST_DELETED_BY_ADMIN, // Cuando un admin borra tu post
    POST_REPORTED // Notificación para admin cuando reportan un post
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
    val mealPostTitle: String = "", // Título de la comida
    val creatorUserId: String = "", // Usuario que creó el post
    val creatorUserName: String = "", // Nombre del creador
    val claimerUserId: String = "", // Usuario que reclamó la comida
    val claimerUserName: String = "", // Nombre del reclamante
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastMessageAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val closedAt: LocalDateTime? = null,
    val unreadCountCreator: Int = 0, // Mensajes sin leer del creador
    val unreadCountClaimer: Int = 0, // Mensajes sin leer del reclamante
    val lastMessage: String = "" // Último mensaje para preview
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

// Tipo de mensaje para soportar texto, imagen y audio
enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO
}

// Extender ChatMessage para soportar mediaUri y tipo
data class ChatMessageWithMedia(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val mediaUri: String? = null, // URI a imagen o audio
    val type: MessageType = MessageType.TEXT,
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)
