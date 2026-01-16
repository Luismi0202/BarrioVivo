package com.example.barriovivo.data.repository

import com.example.barriovivo.data.database.dao.MealPostDao
import com.example.barriovivo.data.database.entity.MealPostEntity
import com.example.barriovivo.domain.model.Location
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.domain.model.MealPostStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MealPostRepository @Inject constructor(
    private val mealPostDao: MealPostDao
) {
    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val DEFAULT_RADIUS_KM = 10.0 // Radio por defecto de 10km
    }

    // Crear post - ahora se publica directamente como ACTIVE
    suspend fun createMealPost(
        userId: String,
        userName: String,
        title: String,
        description: String,
        photoUris: List<String>,
        expiryDate: LocalDate,
        location: Location
    ): Result<MealPost> = try {
        val postId = UUID.randomUUID().toString()
        val mealPostEntity = MealPostEntity(
            id = postId,
            userId = userId,
            userName = userName,
            title = title,
            description = description,
            photoUris = photoUris.joinToString(","),
            expiryDate = expiryDate,
            latitude = location.latitude,
            longitude = location.longitude,
            city = location.city,
            createdAt = LocalDateTime.now(),
            status = MealPostStatus.ACTIVE.name // Publicación directa!
        )
        mealPostDao.insertMealPost(mealPostEntity)
        Result.success(mealPostEntity.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Obtener posts activos cercanos (ahora usa ACTIVE en vez de APPROVED)
    fun getNearbyActiveMealPosts(
        userLatitude: Double,
        userLongitude: Double,
        radiusKm: Double = DEFAULT_RADIUS_KM
    ): Flow<List<MealPost>> {
        val todayDate = LocalDate.now()
        return mealPostDao.getActiveMealPosts(todayDate).map { posts ->
            posts.filter { post ->
                val distance = calculateDistance(
                    userLatitude, userLongitude,
                    post.latitude, post.longitude
                )
                distance <= radiusKm
            }.map { it.toDomain() }
        }
    }

    fun getUserMealPosts(userId: String): Flow<List<MealPost>> {
        return mealPostDao.getUserMealPosts(userId).map { posts ->
            posts.map { it.toDomain() }
        }
    }

    // Posts reportados para el admin
    fun getReportedMealPosts(): Flow<List<MealPost>> {
        return mealPostDao.getReportedMealPosts().map { posts ->
            posts.map { it.toDomain() }
        }
    }

    // Todos los posts para el admin
    fun getAllActivePosts(): Flow<List<MealPost>> {
        return mealPostDao.getAllActivePosts().map { posts ->
            posts.map { it.toDomain() }
        }
    }

    // Reportar un post
    suspend fun reportMealPost(postId: String, reporterId: String, reason: String): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null) {
            val currentReporters = if (post.reportedByUsers.isBlank()) {
                reporterId
            } else {
                "${post.reportedByUsers},$reporterId"
            }
            mealPostDao.reportMealPost(postId, currentReporters, reason)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Post no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Aprobar post reportado (el admin decide mantenerlo)
    suspend fun approveReportedPost(postId: String): Result<Unit> = try {
        mealPostDao.approveReportedPost(postId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Borrar post (admin)
    suspend fun deletePostByAdmin(postId: String, reason: String): Result<Unit> = try {
        mealPostDao.markAsDeleted(postId, reason)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun claimMealPost(postId: String, userId: String): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null && post.isAvailable && post.status == MealPostStatus.ACTIVE.name) {
            mealPostDao.claimMealPost(postId, userId, LocalDateTime.now())
            Result.success(Unit)
        } else {
            Result.failure(Exception("Post no disponible"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMealPostById(postId: String): Result<MealPost?> = try {
        val post = mealPostDao.getMealPostById(postId)
        Result.success(post?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMealPost(postId: String): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null) {
            mealPostDao.deleteMealPost(post)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Post no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    private fun MealPostEntity.toDomain(): MealPost {
        return MealPost(
            id = id,
            userId = userId,
            userName = userName,
            title = title,
            description = description,
            photoUris = photoUris.split(",").filter { it.isNotBlank() },
            expiryDate = expiryDate,
            location = Location(
                city = city,
                latitude = latitude,
                longitude = longitude
            ),
            createdAt = createdAt,
            status = try { MealPostStatus.valueOf(status) } catch (e: Exception) { MealPostStatus.ACTIVE },
            adminComment = adminComment,
            isAvailable = isAvailable,
            claimedByUserId = claimedByUserId,
            claimedAt = claimedAt,
            reportCount = reportCount,
            reportedByUsers = reportedByUsers.split(",").filter { it.isNotBlank() },
            lastReportReason = lastReportReason
        )
    }
}

