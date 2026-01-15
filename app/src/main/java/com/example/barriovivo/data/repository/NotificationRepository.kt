package com.example.barriovivo.data.repository

import com.example.barriovivo.data.database.dao.NotificationDao
import com.example.barriovivo.data.database.entity.NotificationEntity
import com.example.barriovivo.domain.model.Notification
import com.example.barriovivo.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    suspend fun createNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedPostId: String = ""
    ): Result<Notification> = try {
        val notificationId = UUID.randomUUID().toString()
        val notificationEntity = NotificationEntity(
            id = notificationId,
            userId = userId,
            title = title,
            message = message,
            type = type.name,
            relatedPostId = relatedPostId,
            createdAt = LocalDateTime.now(),
            isRead = false
        )
        notificationDao.insertNotification(notificationEntity)
        Result.success(notificationEntity.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getUserNotifications(userId).map { notifications ->
            notifications.map { it.toDomain() }
        }
    }

    fun getUnreadNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications(userId).map { notifications ->
            notifications.map { it.toDomain() }
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = try {
        notificationDao.markAsRead(notificationId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        // Para simplificar, aquí se podría implementar una eliminación
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun NotificationEntity.toDomain(): Notification {
        return Notification(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = NotificationType.valueOf(type),
            relatedPostId = relatedPostId,
            createdAt = createdAt,
            isRead = isRead
        )
    }
}

