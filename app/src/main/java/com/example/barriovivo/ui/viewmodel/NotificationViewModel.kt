package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.NotificationRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de notificaciones.
 *
 * @property notifications Lista de notificaciones del usuario
 * @property unreadCount Cantidad de notificaciones no leidas
 * @property isLoading Indica carga en progreso
 * @property error Mensaje de error
 */
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para gestion de notificaciones.
 *
 * Carga y gestiona las notificaciones del usuario actual:
 * - Lista de notificaciones ordenadas por fecha
 * - Contador de no leidas para badge
 * - Marcado como leido al abrir
 *
 * @property notificationRepository Repositorio de notificaciones
 * @property userRepository Repositorio para obtener usuario actual
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.getUserNotifications(userId).collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error cargando notificaciones",
                    isLoading = false
                )
            }
        }
    }

    fun loadUnreadNotifications(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.getUnreadNotifications(userId).collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        unreadCount = notifications.size,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error cargando notificaciones no leídas"
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notificationId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    unreadCount = maxOf(0, _uiState.value.unreadCount - 1)
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
