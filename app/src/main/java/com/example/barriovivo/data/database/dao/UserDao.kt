package com.example.barriovivo.data.database.dao

import androidx.room.*
import com.example.barriovivo.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos relacionadas con usuarios.
 *
 * Gestiona el registro, autenticacion y actualizacion de datos de usuarios.
 * Las contrasenas se almacenan como hash SHA-256 por seguridad.
 *
 * Nota: El rol de administrador se determina consultando la tabla 'admins',
 * no el campo 'role' de esta entidad.
 *
 * @see UserEntity
 * @see com.example.barriovivo.data.repository.UserRepository
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String)

    @Query("UPDATE users SET name = :name WHERE id = :userId")
    suspend fun updateUserName(userId: String, name: String)

    @Query("UPDATE users SET city = :city, latitude = :latitude, longitude = :longitude, zipCode = :zipCode WHERE id = :userId")
    suspend fun updateUserLocation(userId: String, city: String, latitude: Double, longitude: Double, zipCode: String)
}

