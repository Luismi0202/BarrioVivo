package com.example.barriovivo.data.database.dao

import androidx.room.*
import com.example.barriovivo.data.database.entity.AdminEntity

/**
 * DAO para operaciones de base de datos relacionadas con administradores.
 *
 * Los administradores se cargan desde un archivo JSON de configuracion
 * al iniciar la aplicacion y se almacenan en la tabla 'admins'.
 *
 * @see AdminEntity
 * @see com.example.barriovivo.data.repository.AdminRepository
 */
@Dao
interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmins(admins: List<AdminEntity>)

    @Delete
    suspend fun deleteAdmin(admin: AdminEntity)

    @Query("SELECT * FROM admins WHERE id = :adminId")
    suspend fun getAdminById(adminId: String): AdminEntity?

    @Query("SELECT * FROM admins WHERE email = :email")
    suspend fun getAdminByEmail(email: String): AdminEntity?

    @Query("SELECT * FROM admins WHERE userId = :userId")
    suspend fun getAdminByUserId(userId: String): AdminEntity?

    @Query("SELECT * FROM admins")
    suspend fun getAllAdmins(): List<AdminEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM admins WHERE email = :email)")
    suspend fun isAdmin(email: String): Boolean

    @Query("DELETE FROM admins")
    suspend fun deleteAllAdmins()
}

