package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.AdminRepository
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

data class AdminUiState(
    val pendingPosts: List<MealPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository,
    private val notificationRepository: NotificationRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadPendingPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                mealPostRepository.getPendingMealPosts().collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        pendingPosts = posts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando publicaciones pendientes"
                )
            }
        }
    }

    fun approveMealPost(postId: String, adminComment: String = "") {
        viewModelScope.launch {
            val result = mealPostRepository.approveMealPost(postId, adminComment)
            result.onSuccess {
                // Notificar al usuario
                val post = _uiState.value.pendingPosts.find { it.id == postId }
                post?.let {
                    notificationRepository.createNotification(
                        userId = it.userId,
                        title = "¡Tu comida fue aprobada!",
                        message = "La comida '${it.title}' ha sido aprobada y está visible para otros usuarios",
                        type = NotificationType.POST_APPROVED,
                        relatedPostId = postId
                    )
                }
                // Recargar posts
                loadPendingPosts()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Publicación aprobada correctamente"
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error al aprobar la publicación"
                )
            }
        }
    }

    fun rejectMealPost(postId: String, reason: String) {
        viewModelScope.launch {
            val result = mealPostRepository.rejectMealPost(postId, reason)
            result.onSuccess {
                // Notificar al usuario
                val post = _uiState.value.pendingPosts.find { it.id == postId }
                post?.let {
                    notificationRepository.createNotification(
                        userId = it.userId,
                        title = "Tu comida fue rechazada",
                        message = "Motivo: $reason",
                        type = NotificationType.POST_REJECTED,
                        relatedPostId = postId
                    )
                }
                // Recargar posts
                loadPendingPosts()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Publicación rechazada correctamente"
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error al rechazar la publicación"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}

