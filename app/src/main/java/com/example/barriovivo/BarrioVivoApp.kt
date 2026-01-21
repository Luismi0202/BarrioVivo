package com.example.barriovivo

import android.app.Application
import com.example.barriovivo.data.repository.AdminRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.Location
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Clase Application principal de BarrioVivo.
 *
 * Inicializa componentes globales al arrancar la aplicacion:
 * - Timber para logging en desarrollo
 * - Carga de administradores desde admin_config.json
 * - Creacion automatica de usuarios admin si no existen
 *
 * Utiliza HiltAndroidApp para inyeccion de dependencias.
 */
@HiltAndroidApp
class BarrioVivoApp : Application() {

    @Inject
    lateinit var adminRepository: AdminRepository

    @Inject
    lateinit var userRepository: UserRepository

    // Scope con SupervisorJob para que fallos individuales no cancelen otras tareas
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber solo en debug
        Timber.plant(Timber.DebugTree())

        // Cargar administradores en segundo plano
        initializeAdmins()
    }

    /**
     * Carga los administradores desde el archivo JSON de configuracion
     * y crea sus cuentas de usuario si no existen.
     *
     * El archivo admin_config.json debe estar en assets/ con formato:
     * [{"id": "...", "email": "...", "password": "...", "userId": "..."}]
     */
    private fun initializeAdmins() {
        applicationScope.launch {
            try {
                val result = adminRepository.initializeAdminsFromJson()
                result.onSuccess { adminConfigs ->
                    Timber.d("${adminConfigs.size} administradores cargados")

                    // Crear usuario para cada admin si no existe
                    adminConfigs.forEach { config ->
                        createAdminUserIfNotExists(config)
                    }
                }
                result.onFailure { e ->
                    Timber.e(e, "Error al cargar administradores")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar administradores")
            }
        }
    }

    /**
     * Crea un usuario para el administrador si no existe en la base de datos.
     * Los admins se crean con ubicacion por defecto en Madrid.
     */
    private suspend fun createAdminUserIfNotExists(config: com.example.barriovivo.data.repository.AdminConfigData) {
        try {
            val location = Location(
                city = "Madrid",
                latitude = 40.4168,
                longitude = -3.7038,
                zipCode = "28001"
            )
            val registerResult = userRepository.registerUser(config.email, config.password, location)
            registerResult.onSuccess {
                Timber.d("Usuario admin creado: ${config.email}")
            }
            registerResult.onFailure {
                Timber.d("Usuario admin ya existe: ${config.email}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al crear usuario admin ${config.email}")
        }
    }
}
