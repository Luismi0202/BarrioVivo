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

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole = UserRole.USER
)

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

    private fun checkCurrentUser() {
        viewModelScope.launch {
            userRepository.currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(
                            currentUser = user,
                            isLoggedIn = true,
                            userRole = user.role
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
}
