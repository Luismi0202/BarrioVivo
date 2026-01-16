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

@HiltAndroidApp
class BarrioVivoApp : Application() {

    @Inject
    lateinit var adminRepository: AdminRepository

    @Inject
    lateinit var userRepository: UserRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber para logging
        Timber.plant(Timber.DebugTree())

        // Inicializar administradores desde JSON
        applicationScope.launch {
            try {
                val result = adminRepository.initializeAdminsFromJson()
                result.onSuccess { adminConfigs ->
                    Timber.d("✅ ${adminConfigs.size} administradores cargados desde admin_config.json")

                    // Crear usuarios admin si no existen
                    adminConfigs.forEach { config ->
                        try {
                            // Intentar crear usuario admin
                            val location = Location(
                                city = "Madrid",
                                latitude = 40.4168,
                                longitude = -3.7038,
                                zipCode = "28001"
                            )
                            val registerResult = userRepository.registerUser(config.email, config.password, location)
                            registerResult.onSuccess {
                                Timber.d("👤 Usuario admin creado: ${config.email} / Password: ${config.password}")
                            }
                            registerResult.onFailure {
                                Timber.d("👤 Usuario admin ya existe: ${config.email} / Password: ${config.password}")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Error al crear usuario admin ${config.email}")
                        }
                    }

                    // Mostrar resumen de cuentas de admin
                    Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Timber.d("📋 CUENTAS DE ADMINISTRADOR:")
                    adminConfigs.forEach { config ->
                        Timber.d("   📧 Email: ${config.email}")
                        Timber.d("   🔑 Pass: ${config.password}")
                        Timber.d("   ─────────────────────")
                    }
                    Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                }
                result.onFailure { e ->
                    Timber.e(e, "❌ Error al cargar administradores: ${e.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error al inicializar administradores: ${e.message}")
            }
        }
    }
}
