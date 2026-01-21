package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.AdminRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.Location
import com.example.barriovivo.domain.model.User
import com.example.barriovivo.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de autenticacion.
 *
 * @property isLoading Indica si hay una operacion en progreso
 * @property error Mensaje de error a mostrar
 * @property currentUser Usuario autenticado actual
 * @property isLoggedIn Si hay sesion activa
 * @property userRole Rol del usuario (USER o ADMIN)
 * @property successMessage Mensaje de exito temporal
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole = UserRole.USER,
    val successMessage: String? = null
)

/**
 * ViewModel para gestion de autenticacion.
 *
 * Maneja login, registro, recuperacion de sesion y logout.
 *
 * Verificacion de rol de administrador:
 * El rol ADMIN se determina consultando AdminRepository, no el campo
 * 'role' de la entidad de usuario. Esto permite que los administradores
 * se configuren externamente en admin_config.json.
 *
 * @property userRepository Repositorio para operaciones de usuario
 * @property adminRepository Repositorio para verificacion de admin
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    /**
     * Verifica si existe una sesion guardada y recupera los datos del usuario.
     * Tambien reverifica el rol de admin consultando la tabla de administradores.
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            userRepository.currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        // Verificar si el usuario es administrador
                        val isAdmin = adminRepository.isUserAdmin(user.email)
                        val role = if (isAdmin) UserRole.ADMIN else user.role

                        _uiState.value = _uiState.value.copy(
                            currentUser = user.copy(role = role),
                            isLoggedIn = true,
                            userRole = role
                        )
                    } else {
                        // Usuario no encontrado, asegurar estado limpio
                        _uiState.value = AuthUiState()
                    }
                } else {
                    // No hay usuario logueado
                    _uiState.value = AuthUiState()
                }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        city: String,
        latitude: Double,
        longitude: Double,
        zipCode: String
    ) {
        if (!validateInputs(email, password, confirmPassword)) {
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                error = "Las contraseñas no coinciden"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val location = Location(
                city = city,
                latitude = latitude,
                longitude = longitude,
                zipCode = zipCode
            )

            val result = userRepository.registerUser(email, password, location)

            result.onSuccess { user ->
                // Verificar si es admin después de registrarse
                val isAdmin = adminRepository.isUserAdmin(email)
                val role = if (isAdmin) UserRole.ADMIN else UserRole.USER

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    currentUser = user.copy(role = role),
                    isLoggedIn = true,
                    userRole = role
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error en el registro"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Por favor completa todos los campos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = userRepository.loginUser(email, password)

            result.onSuccess { user ->
                // Verificar si es admin
                val isAdmin = adminRepository.isUserAdmin(email)
                val role = if (isAdmin) UserRole.ADMIN else UserRole.USER

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    currentUser = user.copy(role = role),
                    isLoggedIn = true,
                    userRole = role
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error en el login"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logoutUser()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "El email no puede estar vacío")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Email inválido")
                return false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
                return false
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "La contraseña no puede estar vacía")
                return false
            }
        }
        return true
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El email no puede estar vacío")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "La nueva contraseña debe tener al menos 6 caracteres")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.resetPassword(email, newPassword)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    successMessage = "Contraseña actualizada"
                )
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo actualizar la contraseña"
                )
            }
        }
    }

    fun consumeSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun changePassword(userId: String, currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa las contraseñas")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "La nueva contraseña debe tener al menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.changePassword(userId, currentPassword, newPassword)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    successMessage = "Contraseña cambiada"
                )
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo cambiar la contraseña"
                )
            }
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.deleteAccount(userId)
            result.onSuccess {
                _uiState.value = AuthUiState()
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo eliminar la cuenta"
                )
            }
        }
    }
}
