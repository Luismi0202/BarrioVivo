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
        private const val DEFAULT_RADIUS_KM = 5.0 // Radio por defecto de 5km
    }

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
            photoUris = photoUris.joinToString(","), // Guardar como string separado por comas
            expiryDate = expiryDate,
            latitude = location.latitude,
            longitude = location.longitude,
            city = location.city,
            createdAt = LocalDateTime.now(),
            status = MealPostStatus.PENDING.name
        )
        mealPostDao.insertMealPost(mealPostEntity)
        Result.success(mealPostEntity.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getNearbyApprovedMealPosts(
        userLatitude: Double,
        userLongitude: Double,
        radiusKm: Double = DEFAULT_RADIUS_KM
    ): Flow<List<MealPost>> {
        val todayDate = LocalDate.now()
        return mealPostDao.getApprovedMealPosts(todayDate).map { posts ->
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

    fun getPendingMealPosts(): Flow<List<MealPost>> {
        return mealPostDao.getPendingMealPosts().map { posts ->
            posts.map { it.toDomain() }
        }
    }

    suspend fun approveMealPost(postId: String, adminComment: String = ""): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null) {
            val updatedPost = post.copy(
                status = MealPostStatus.APPROVED.name,
                adminComment = adminComment
            )
            mealPostDao.updateMealPost(updatedPost)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Post no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun rejectMealPost(postId: String, adminComment: String): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null) {
            val updatedPost = post.copy(
                status = MealPostStatus.REJECTED.name,
                adminComment = adminComment
            )
            mealPostDao.updateMealPost(updatedPost)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Post no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun claimMealPost(postId: String, userId: String): Result<Unit> = try {
        val post = mealPostDao.getMealPostById(postId)
        if (post != null && post.isAvailable) {
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
            photoUris = photoUris.split(",").filter { it.isNotBlank() }, // Convertir de string a lista
            expiryDate = expiryDate,
            location = Location(
                city = city,
                latitude = latitude,
                longitude = longitude
            ),
            createdAt = createdAt,
            status = MealPostStatus.valueOf(status),
            adminComment = adminComment,
            isAvailable = isAvailable,
            claimedByUserId = claimedByUserId,
            claimedAt = claimedAt
        )
    }
}

