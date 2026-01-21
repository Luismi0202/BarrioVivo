package com.example.barriovivo.data.database.dao

import androidx.room.*
import com.example.barriovivo.data.database.entity.MealPostEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DAO para operaciones de base de datos relacionadas con publicaciones de comida.
 *
 * Gestiona el ciclo de vida completo de las publicaciones:
 * - Creacion con estado ACTIVE por defecto
 * - Consultas filtradas por ubicacion, usuario y estado
 * - Reclamacion por otros usuarios
 * - Sistema de reportes con contadores
 * - Eliminacion por administradores
 *
 * Estados posibles de una publicacion:
 * - ACTIVE: Visible y disponible para reclamar
 * - REPORTED: Ha sido reportada y pendiente de revision
 * - DELETED: Eliminada por un administrador
 *
 * @see MealPostEntity
 * @see com.example.barriovivo.domain.model.MealPostStatus
 */
@Dao
interface MealPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPost(mealPost: MealPostEntity)

    @Update
    suspend fun updateMealPost(mealPost: MealPostEntity)

    @Delete
    suspend fun deleteMealPost(mealPost: MealPostEntity)

    @Query("SELECT * FROM meal_posts WHERE id = :postId")
    suspend fun getMealPostById(postId: String): MealPostEntity?

    // Posts activos y no expirados (para usuarios normales)
    @Query("SELECT * FROM meal_posts WHERE status = 'ACTIVE' AND isAvailable = 1 AND expiryDate >= :today ORDER BY createdAt DESC")
    fun getActiveMealPosts(today: LocalDate): Flow<List<MealPostEntity>>

    // Posts del usuario
    @Query("SELECT * FROM meal_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserMealPosts(userId: String): Flow<List<MealPostEntity>>

    // Posts reportados (para admin)
    @Query("SELECT * FROM meal_posts WHERE reportCount > 0 AND status != 'DELETED' ORDER BY reportCount DESC")
    fun getReportedMealPosts(): Flow<List<MealPostEntity>>

    // Todos los posts activos (para admin - sin filtro de ubicación)
    @Query("SELECT * FROM meal_posts WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getAllActivePosts(): Flow<List<MealPostEntity>>

    // Reclamar comida
    @Query("UPDATE meal_posts SET isAvailable = 0, claimedByUserId = :userId, claimedAt = :claimedAt WHERE id = :postId")
    suspend fun claimMealPost(postId: String, userId: String, claimedAt: LocalDateTime)

    // Reportar post
    @Query("UPDATE meal_posts SET reportCount = reportCount + 1, reportedByUsers = :reporters, lastReportReason = :reason WHERE id = :postId")
    suspend fun reportMealPost(postId: String, reporters: String, reason: String)

    // Aprobar post reportado (limpiar reportes)
    @Query("UPDATE meal_posts SET reportCount = 0, reportedByUsers = '', lastReportReason = '' WHERE id = :postId")
    suspend fun approveReportedPost(postId: String)

    // Marcar como eliminado por admin
    @Query("UPDATE meal_posts SET status = 'DELETED', adminComment = :reason WHERE id = :postId")
    suspend fun markAsDeleted(postId: String, reason: String)

    // Contar posts del usuario
    @Query("SELECT COUNT(*) FROM meal_posts WHERE userId = :userId")
    suspend fun getUserPostCount(userId: String): Int

    // Contar posts reclamados por el usuario
    @Query("SELECT COUNT(*) FROM meal_posts WHERE claimedByUserId = :userId")
    suspend fun getUserClaimedCount(userId: String): Int
}

