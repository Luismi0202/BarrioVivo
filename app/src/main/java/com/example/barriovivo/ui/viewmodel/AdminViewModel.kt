package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.data.repository.NotificationRepository
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.domain.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para el panel de administracion.
 *
 * @property reportedPosts Publicaciones reportadas pendientes de revision
 * @property allPosts Todas las publicaciones del sistema
 * @property isLoading Indica carga en progreso
 * @property error Mensaje de error
 * @property successMessage Mensaje de exito
 * @property selectedTab Tab seleccionada (0=Reportados, 1=Todos)
 */
data class AdminUiState(
    val reportedPosts: List<MealPost> = emptyList(),
    val allPosts: List<MealPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedTab: Int = 0 // 0 = Reportados, 1 = Todos
)

/**
 * ViewModel para el panel de administracion.
 *
 * Funcionalidades de moderacion:
 * - Ver publicaciones reportadas por usuarios
 * - Ver todas las publicaciones del sistema
 * - Aprobar publicacion reportada (limpia los reportes)
 * - Eliminar publicacion (notifica al autor)
 *
 * @property mealPostRepository Repositorio de publicaciones
 * @property notificationRepository Repositorio para crear notificaciones
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadReportedPosts()
        loadAllPosts()
    }

    // Cargar posts reportados
    fun loadReportedPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                mealPostRepository.getReportedMealPosts().collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        reportedPosts = posts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando reportes"
                )
            }
        }
    }

    // Cargar todos los posts (para vista general)
    fun loadAllPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                mealPostRepository.getAllActivePosts().collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        allPosts = posts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando publicaciones"
                )
            }
        }
    }

    // Borrar post (admin)
    fun deletePost(postId: String, reason: String = "") {
        viewModelScope.launch {
            val post = _uiState.value.reportedPosts.find { it.id == postId }
                ?: _uiState.value.allPosts.find { it.id == postId }

            val result = mealPostRepository.deletePostByAdmin(postId, reason)
            result.onSuccess {
                // Notificar al usuario del borrado
                post?.let {
                    notificationRepository.createNotification(
                        userId = it.userId,
                        title = "Tu publicación fue eliminada",
                        message = if (reason.isNotEmpty()) "Motivo: $reason" else "Fue eliminada por incumplimiento de las normas de la comunidad",
                        type = NotificationType.POST_DELETED_BY_ADMIN,
                        relatedPostId = postId
                    )
                }
                // Recargar posts
                loadReportedPosts()
                loadAllPosts()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Publicación eliminada correctamente"
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error al eliminar la publicación"
                )
            }
        }
    }

    // Aprobar post reportado (mantenerlo visible, limpiar reportes)
    fun approveReportedPost(postId: String) {
        viewModelScope.launch {
            val result = mealPostRepository.approveReportedPost(postId)
            result.onSuccess {
                // Recargar posts
                loadReportedPosts()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Publicación aprobada - reportes eliminados"
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error al aprobar la publicación"
                )
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        // Tab 0 = Todos los posts, Tab 1 = Reportados
        if (tab == 0) loadAllPosts() else loadReportedPosts()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
