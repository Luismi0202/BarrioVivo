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

    @Query("SELECT * FROM meal_posts WHERE status = 'APPROVED' AND expiryDate >= :todayDate ORDER BY createdAt DESC")
    fun getApprovedMealPosts(todayDate: LocalDate): Flow<List<MealPostEntity>>

    @Query("SELECT * FROM meal_posts WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingMealPosts(): Flow<List<MealPostEntity>>

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

