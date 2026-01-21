package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.domain.model.MealPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla principal.
 *
 * @property nearbyMealPosts Publicaciones cercanas al usuario
 * @property userMealPosts Publicaciones creadas por el usuario
 * @property allMealPosts Todas las publicaciones (solo para admins)
 * @property isLoading Indica carga en progreso
 * @property error Mensaje de error
 * @property isAdmin Si el usuario es administrador
 */
data class HomeUiState(
    val nearbyMealPosts: List<MealPost> = emptyList(),
    val userMealPosts: List<MealPost> = emptyList(),
    val allMealPosts: List<MealPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAdmin: Boolean = false
)

/**
 * ViewModel para la pantalla principal (Home).
 *
 * Carga y gestiona las publicaciones de comida:
 * - Usuarios normales: Solo ven publicaciones cercanas (filtro por ubicacion)
 * - Administradores: Ven todas las publicaciones sin filtro de ubicacion
 *
 * El filtrado por ubicacion usa un radio de 10km por defecto.
 *
 * @property mealPostRepository Repositorio para operaciones de publicaciones
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var nearbyPostsJob: Job? = null
    private var userPostsJob: Job? = null
    private var allPostsJob: Job? = null

    // Coordenadas guardadas para poder refrescar
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private var lastUserId: String = ""
    private var isAdminUser: Boolean = false

    /**
     * Establece si el usuario actual es administrador.
     * Afecta al comportamiento de refresh() y que datos se cargan.
     */
    fun setIsAdmin(isAdmin: Boolean) {
        isAdminUser = isAdmin
        _uiState.value = _uiState.value.copy(isAdmin = isAdmin)
    }

    /**
     * Carga todas las publicaciones activas sin filtro de ubicacion.
     * Uso exclusivo para administradores.
     */
    fun loadAllMealPosts() {
        allPostsJob?.cancel()
        allPostsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                mealPostRepository.getAllActivePosts().collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        allMealPosts = posts,
                        nearbyMealPosts = posts, // También actualizar nearbyMealPosts para compatibilidad
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando todas las comidas"
                )
            }
        }
    }

    fun loadNearbyMealPosts(userLatitude: Double, userLongitude: Double) {
        lastLatitude = userLatitude
        lastLongitude = userLongitude

        nearbyPostsJob?.cancel()
        nearbyPostsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                mealPostRepository.getNearbyActiveMealPosts(
                    userLatitude = userLatitude,
                    userLongitude = userLongitude
                ).collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        nearbyMealPosts = posts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando comidas cercanas"
                )
            }
        }
    }

    fun loadUserMealPosts(userId: String) {
        lastUserId = userId

        userPostsJob?.cancel()
        userPostsJob = viewModelScope.launch {
            try {
                mealPostRepository.getUserMealPosts(userId).collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        userMealPosts = posts,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error cargando tus comidas"
                )
            }
        }
    }

    // Forzar recarga de datos
    fun refresh() {
        if (isAdminUser) {
            // Para administradores, cargar todas las comidas
            loadAllMealPosts()
        } else if (lastLatitude != 0.0 || lastLongitude != 0.0) {
            loadNearbyMealPosts(lastLatitude, lastLongitude)
        }
        if (lastUserId.isNotBlank()) {
            loadUserMealPosts(lastUserId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

