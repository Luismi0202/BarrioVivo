package com.example.barriovivo.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.barriovivo.data.database.dao.UserDao
import com.example.barriovivo.data.database.entity.UserEntity
import com.example.barriovivo.domain.model.Location
import com.example.barriovivo.domain.model.User
import com.example.barriovivo.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

/**
 * Repositorio para gestion de usuarios.
 *
 * Maneja registro, autenticacion y actualizacion de datos de usuarios.
 *
 * Seguridad:
 * - Las contrasenas se almacenan como hash SHA-256
 * - La sesion se persiste en DataStore con ID de usuario y rol
 *
 * Nota importante sobre roles:
 * El rol ADMIN no se determina por el campo 'role' de UserEntity,
 * sino consultando AdminRepository.isUserAdmin() con el email.
 * Esto permite que los admins se configuren externamente en admin_config.json
 *
 * @property userDao DAO para operaciones de base de datos
 * @property dataStore DataStore para persistencia de sesion
 */
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        private val CURRENT_USER_ROLE = stringPreferencesKey("current_user_role")
    }

    val currentUserIdFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID]
    }

    val currentUserRoleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ROLE]
    }

    suspend fun registerUser(
        email: String,
        password: String,
        location: Location
    ): Result<User> = try {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            Result.failure(Exception("El email ya está registrado"))
        } else {
            val userId = UUID.randomUUID().toString()
            val passwordHash = hashPassword(password)
            val userEntity = UserEntity(
                id = userId,
                email = email,
                passwordHash = passwordHash,
                city = location.city,
                latitude = location.latitude,
                longitude = location.longitude,
                zipCode = location.zipCode,
                role = UserRole.USER.name
            )
            userDao.insertUser(userEntity)
            saveCurrentUser(userId, UserRole.USER.name)
            Result.success(userEntity.toDomain())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginUser(email: String, password: String): Result<User> = try {
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            Result.failure(Exception("Usuario no encontrado"))
        } else if (!verifyPassword(password, user.passwordHash)) {
            Result.failure(Exception("Contraseña incorrecta"))
        } else {
            saveCurrentUser(user.id, user.role)
            Result.success(user.toDomain())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCurrentUser(): User? = try {
        val preferences = dataStore.data.first()
        val currentUserId = preferences[CURRENT_USER_ID]
        currentUserId?.let { userDao.getUserById(it)?.toDomain() }
    } catch (e: Exception) {
        null
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    suspend fun updateUserLocation(userId: String, location: Location): Result<Unit> = try {
        val user = userDao.getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(
                city = location.city,
                latitude = location.latitude,
                longitude = location.longitude,
                zipCode = location.zipCode
            )
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun logoutUser() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
            preferences.remove(CURRENT_USER_ROLE)
        }
    }

    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = try {
        val user = userDao.getUserById(userId)
        if (user == null) {
            Result.failure(Exception("Usuario no encontrado"))
        } else if (!verifyPassword(currentPassword, user.passwordHash)) {
            Result.failure(Exception("Contraseña actual incorrecta"))
        } else {
            val newPasswordHash = hashPassword(newPassword)
            val updatedUser = user.copy(passwordHash = newPasswordHash)
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = try {
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            Result.failure(Exception("Usuario no encontrado"))
        } else {
            val newPasswordHash = hashPassword(newPassword)
            val updatedUser = user.copy(passwordHash = newPasswordHash)
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(userId: String): Result<Unit> = try {
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.deleteUser(user)
            logoutUser()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun saveCurrentUser(userId: String, role: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
            preferences[CURRENT_USER_ROLE] = role
        }
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    private fun UserEntity.toDomain(): User {
        return User(
            id = id,
            email = email,
            name = name,
            password = "", // No devolvemos la contraseña
            location = Location(
                city = city,
                latitude = latitude,
                longitude = longitude,
                zipCode = zipCode
            ),
            role = UserRole.valueOf(role),
            createdAt = createdAt
        )
    }
}

