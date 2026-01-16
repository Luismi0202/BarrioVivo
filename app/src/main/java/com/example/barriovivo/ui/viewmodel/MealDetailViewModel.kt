package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.ChatRepository
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

data class MealDetailUiState(
    val mealPost: MealPost? = null,
    val isLoading: Boolean = false,
    val isClaimLoading: Boolean = false,
    val claimSuccess: Boolean = false,
    val conversationId: String? = null,
    val error: String? = null,
    val isReportLoading: Boolean = false,
    val reportSuccess: Boolean = false
)

@HiltViewModel
class MealDetailViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository,
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealDetailUiState())
    val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()

    fun loadMealPost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = mealPostRepository.getMealPostById(postId)

            result.onSuccess { mealPost ->
                _uiState.value = _uiState.value.copy(
                    mealPost = mealPost,
                    isLoading = false,
                    error = null
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al cargar la comida"
                )
            }
        }
    }

    fun claimMealPost(postId: String, claimerId: String, claimerName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClaimLoading = true)

            val mealPost = _uiState.value.mealPost
            if (mealPost == null) {
                _uiState.value = _uiState.value.copy(
                    isClaimLoading = false,
                    error = "No se encontró la comida"
                )
                return@launch
            }

            // Verificar que no sea el propio usuario
            if (mealPost.userId == claimerId) {
                _uiState.value = _uiState.value.copy(
                    isClaimLoading = false,
                    error = "No puedes reclamar tu propia comida"
                )
                return@launch
            }

            // Reclamar el post
            val claimResult = mealPostRepository.claimMealPost(postId, claimerId)

            claimResult.onSuccess {
                // Crear conversación de chat
                val chatResult = chatRepository.createConversation(
                    mealPostId = postId,
                    creatorUserId = mealPost.userId,
                    claimerUserId = claimerId
                )

                chatResult.onSuccess { conversation ->
                    // Crear notificación para el creador del post
                    notificationRepository.createNotification(
                        userId = mealPost.userId,
                        title = "¡Alguien quiere tu comida!",
                        message = "$claimerName ha reclamado tu comida: ${mealPost.title}",
                        type = NotificationType.FOOD_CLAIMED,
                        relatedPostId = postId
                    )

                    _uiState.value = _uiState.value.copy(
                        isClaimLoading = false,
                        claimSuccess = true,
                        conversationId = conversation.id,
                        error = null
                    )

                    // Recargar el post para actualizar el estado
                    loadMealPost(postId)
                }

                chatResult.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isClaimLoading = false,
                        claimSuccess = true,
                        error = "Comida reclamada pero error al crear chat: ${exception.message}"
                    )
                }
            }

            claimResult.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isClaimLoading = false,
                    error = exception.message ?: "Error al reclamar la comida"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearClaimSuccess() {
        _uiState.value = _uiState.value.copy(claimSuccess = false, conversationId = null)
    }

    fun clearReportSuccess() {
        _uiState.value = _uiState.value.copy(reportSuccess = false)
    }

    fun reportMealPost(postId: String, reporterId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReportLoading = true)

            val mealPost = _uiState.value.mealPost
            if (mealPost == null) {
                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    error = "No se encontró la comida"
                )
                return@launch
            }

            // Verificar que no sea el propio usuario
            if (mealPost.userId == reporterId) {
                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    error = "No puedes reportar tu propia publicación"
                )
                return@launch
            }

            // Verificar si ya reportó
            if (mealPost.reportedByUsers.contains(reporterId)) {
                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    error = "Ya has reportado esta publicación"
                )
                return@launch
            }

            val result = mealPostRepository.reportMealPost(postId, reporterId, reason)

            result.onSuccess {
                // Notificar al admin (ID genérico, en un caso real sería una lista de admins)
                notificationRepository.createNotification(
                    userId = "admin_user_id",
                    title = "⚠️ Nueva publicación reportada",
                    message = "La comida '${mealPost.title}' ha sido reportada. Motivo: $reason",
                    type = NotificationType.POST_REPORTED,
                    relatedPostId = postId
                )

                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    reportSuccess = true,
                    error = null
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    error = exception.message ?: "Error al reportar"
                )
            }
        }
    }
}

