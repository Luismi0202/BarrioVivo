package com.example.barriovivo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.barriovivo.domain.model.MealPostStatus
import com.example.barriovivo.domain.model.UserRole
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidad de usuario para almacenamiento en base de datos.
 *
 * @property id Identificador unico UUID
 * @property email Correo electronico (unico)
 * @property name Nombre mostrado del usuario
 * @property passwordHash Hash SHA-256 de la contrasena
 * @property city Ciudad de ubicacion
 * @property latitude Coordenada de latitud
 * @property longitude Coordenada de longitud
 * @property zipCode Codigo postal
 * @property role Rol del usuario (USER por defecto, ADMIN se determina por tabla admins)
 * @property createdAt Fecha de registro
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String = "",
    val passwordHash: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val zipCode: String,
    val role: String = UserRole.USER.name,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Entidad de publicacion de comida para almacenamiento en base de datos.
 *
 * @property id Identificador unico UUID
 * @property userId ID del usuario creador
 * @property userName Nombre del usuario creador (denormalizado para eficiencia)
 * @property title Titulo de la publicacion
 * @property description Descripcion detallada
 * @property photoUris URIs de fotos separadas por comas
 * @property expiryDate Fecha limite de disponibilidad
 * @property latitude Coordenada de latitud
 * @property longitude Coordenada de longitud
 * @property city Ciudad de la publicacion
 * @property createdAt Fecha de creacion
 * @property status Estado actual (ACTIVE, REPORTED, DELETED)
 * @property adminComment Comentario del admin si fue moderada
 * @property isAvailable Si esta disponible para reclamar
 * @property claimedByUserId ID del usuario que la reclamo
 * @property claimedAt Fecha de reclamacion
 * @property reportCount Numero de reportes recibidos
 * @property reportedByUsers IDs de usuarios que reportaron (separados por comas)
 * @property lastReportReason Ultimo motivo de reporte
 */
@Entity(tableName = "meal_posts")
data class MealPostEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val title: String,
    val description: String,
    val photoUris: String,
    val expiryDate: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: String = MealPostStatus.ACTIVE.name,
    val adminComment: String = "",
    val isAvailable: Boolean = true,
    val claimedByUserId: String? = null,
    val claimedAt: LocalDateTime? = null,
    val reportCount: Int = 0,
    val reportedByUsers: String = "",
    val lastReportReason: String = ""
)

/**
 * Entidad de notificacion para almacenamiento en base de datos.
 *
 * @property id Identificador unico UUID
 * @property userId ID del usuario destinatario
 * @property title Titulo de la notificacion
 * @property message Contenido del mensaje
 * @property type Tipo de notificacion (ver NotificationType)
 * @property relatedPostId ID del post relacionado (si aplica)
 * @property createdAt Fecha de creacion
 * @property isRead Si ha sido leida
 */
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

/**
 * Entidad de administrador para almacenamiento en base de datos.
 * Se carga desde admin_config.json al iniciar la aplicacion.
 *
 * @property id Identificador unico del admin
 * @property email Email del administrador (usado para verificacion)
 * @property userId ID de usuario asociado
 */
@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val userId: String
)

/**
 * Entidad de conversacion de chat para almacenamiento en base de datos.
 *
 * Una conversacion se crea cuando un usuario reclama la comida de otro.
 * Tiene exactamente dos participantes: creador del post y reclamante.
 *
 * @property id Identificador unico UUID
 * @property mealPostId ID de la publicacion que origino la conversacion
 * @property mealPostTitle Titulo de la comida (denormalizado)
 * @property creatorUserId ID del creador del post
 * @property creatorUserName Nombre del creador (denormalizado)
 * @property claimerUserId ID del usuario que reclamo
 * @property claimerUserName Nombre del reclamante (denormalizado)
 * @property createdAt Fecha de creacion
 * @property lastMessageAt Fecha del ultimo mensaje
 * @property isActive Si la conversacion esta activa
 * @property closedAt Fecha de cierre (si aplica)
 * @property unreadCountCreator Mensajes no leidos para el creador
 * @property unreadCountClaimer Mensajes no leidos para el reclamante
 * @property lastMessage Preview del ultimo mensaje
 */
@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey
    val id: String,
    val mealPostId: String,
    val mealPostTitle: String = "",
    val creatorUserId: String,
    val creatorUserName: String = "",
    val claimerUserId: String,
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
 * Entidad de mensaje de chat para almacenamiento en base de datos.
 *
 * Soporta tres tipos de contenido: texto, imagen y audio.
 * El campo messageType determina como interpretar message y mediaUri.
 *
 * @property id Identificador unico UUID
 * @property conversationId ID de la conversacion padre
 * @property senderId ID del usuario remitente
 * @property senderName Nombre del remitente (denormalizado)
 * @property message Texto del mensaje o caption para media
 * @property mediaUri URI del archivo multimedia (null para texto)
 * @property messageType Tipo: TEXT, IMAGE o AUDIO
 * @property sentAt Fecha y hora de envio
 * @property isRead Si ha sido leido por el destinatario
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val mediaUri: String? = null,
    val messageType: String = "TEXT",
    val sentAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)
