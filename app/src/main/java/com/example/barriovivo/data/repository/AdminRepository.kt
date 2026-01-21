package com.example.barriovivo.data.repository

import android.content.Context
import com.example.barriovivo.data.database.dao.AdminDao
import com.example.barriovivo.data.database.entity.AdminEntity
import com.example.barriovivo.domain.model.Admin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Clase de datos para deserializacion del archivo admin_config.json.
 * Contiene la configuracion de cada administrador del sistema.
 */
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class AdminConfigData(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("userId")
    val userId: String
)

/**
 * Repositorio para gestion de administradores del sistema.
 *
 * Los administradores se definen en el archivo assets/admin_config.json
 * y se cargan en la base de datos al iniciar la aplicacion.
 *
 * La verificacion de rol de admin se realiza consultando este repositorio,
 * no el campo 'role' de UserEntity.
 *
 * @property adminDao DAO para operaciones de base de datos
 * @property context Contexto de aplicacion para acceder a assets
 */
class AdminRepository @Inject constructor(
    private val adminDao: AdminDao,
    @ApplicationContext private val context: Context
) {

    suspend fun initializeAdminsFromJson(): Result<List<AdminConfigData>> = try {
        val jsonContent = readAdminConfigJson()
        val adminConfigs = Json.decodeFromString<List<AdminConfigData>>(jsonContent)

        adminConfigs.forEach { config ->
            val adminEntity = AdminEntity(
                id = config.id,
                email = config.email,
                userId = config.userId
            )
            adminDao.insertAdmin(adminEntity)
        }

        // Retornar la lista de configs para crear usuarios
        Result.success(adminConfigs)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isUserAdmin(email: String): Boolean {
        return adminDao.getAdminByEmail(email) != null
    }

    suspend fun getAdminByEmail(email: String): Admin? {
        return adminDao.getAdminByEmail(email)?.toDomain()
    }

    suspend fun getAllAdmins(): List<Admin> {
        return adminDao.getAllAdmins().map { it.toDomain() }
    }

    private fun readAdminConfigJson(): String {
        return try {
            val inputStream = context.assets.open("admin_config.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun AdminEntity.toDomain(): Admin {
        return Admin(
            id = id,
            email = email,
            userId = userId
        )
    }
}

