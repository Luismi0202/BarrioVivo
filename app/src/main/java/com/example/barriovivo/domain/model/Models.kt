package com.example.barriovivo.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de dominio para representar un usuario.
 *
 * @property id Identificador unico
 * @property email Correo electronico
 * @property name Nombre mostrado
 * @property password Campo vacio por seguridad (no se expone)
 * @property location Ubicacion del usuario
 * @property role Rol: USER o ADMIN
 * @property createdAt Fecha de registro
 */
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val location: Location = Location(),
    val role: UserRole = UserRole.USER,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Roles de usuario en el sistema.
 * - ADMIN: Acceso a panel de administracion y moderacion
 * - USER: Usuario estandar
 */
enum class UserRole {
    ADMIN, USER
}

/**
 * Modelo de ubicacion geografica.
 *
 * @property city Nombre de la ciudad
 * @property latitude Coordenada de latitud
 * @property longitude Coordenada de longitud
 * @property country Pais (por defecto Espana)
 * @property zipCode Codigo postal
 */
data class Location(
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "Spain",
    val zipCode: String = ""
)

/**
 * Modelo de dominio para publicacion de comida.
 *
 * @property id Identificador unico
 * @property userId ID del creador
 * @property userName Nombre del creador
 * @property title Titulo de la publicacion
 * @property description Descripcion detallada
 * @property photoUris Lista de URIs de fotos
 * @property expiryDate Fecha de caducidad
 * @property location Ubicacion de la comida
 * @property createdAt Fecha de creacion
 * @property status Estado actual
 * @property adminComment Comentario de moderacion
 * @property isAvailable Disponibilidad para reclamar
 * @property claimedByUserId Usuario que reclamo
 * @property claimedAt Fecha de reclamacion
 * @property reportCount Numero de reportes
 * @property reportedByUsers IDs de reportantes
 * @property lastReportReason Motivo del ultimo reporte
 */
data class MealPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val expiryDate: LocalDate = LocalDate.now(),
    val location: Location = Location(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: MealPostStatus = MealPostStatus.ACTIVE,
    val adminComment: String = "",
    val isAvailable: Boolean = true,
    val claimedByUserId: String? = null,
    val claimedAt: LocalDateTime? = null,
    val reportCount: Int = 0,
    val reportedByUsers: List<String> = emptyList(),
    val lastReportReason: String = ""
)

/**
 * Estados posibles de una publicacion.
 * - ACTIVE: Visible y disponible
 * - REPORTED: Reportada, pendiente de revision
 * - DELETED: Eliminada por administrador
 */
enum class MealPostStatus {
    ACTIVE,
    REPORTED,
    DELETED
}

/**
 * Modelo de dominio para notificaciones.
 */
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

/**
 * Tipos de notificacion del sistema.
 * Cada tipo determina el icono y comportamiento en la UI.
 */
enum class NotificationType {
    INFO,
    FOOD_CLAIMED,
    NEW_MESSAGE,
    CHAT_CLOSED,
    POST_DELETED_BY_ADMIN,
    POST_REPORTED
}

/**
 * Modelo de administrador.
 */
data class Admin(
    val id: String = "",
    val email: String = "",
    val userId: String = ""
)

/**
 * Modelo de conversacion de chat.
 * Representa el canal de comunicacion entre dos usuarios.
 */
data class ChatConversation(
    val id: String = "",
    val mealPostId: String = "",
    val mealPostTitle: String = "",
    val creatorUserId: String = "",
    val creatorUserName: String = "",
    val claimerUserId: String = "",
    val claimerUserName: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastMessageAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val closedAt: LocalDateTime? = null,
    val unreadCountCreator: Int = 0,
    val unreadCountClaimer: Int = 0,
    val lastMessage: String = ""
)

/**
 * Modelo basico de mensaje de chat (solo texto).
 */
data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

/**
 * Tipos de contenido de mensaje.
 * - TEXT: Mensaje de texto plano
 * - IMAGE: Imagen adjunta
 * - AUDIO: Nota de voz
 */
enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO
}

/**
 * Modelo de mensaje con soporte multimedia.
 * Extiende ChatMessage con campos para archivos adjuntos.
 *
 * @property mediaUri URI del archivo (null para texto)
 * @property type Tipo de contenido
 */
data class ChatMessageWithMedia(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val mediaUri: String? = null,
    val type: MessageType = MessageType.TEXT,
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)
