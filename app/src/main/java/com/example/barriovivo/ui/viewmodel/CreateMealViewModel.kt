package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.data.repository.NotificationRepository
import com.example.barriovivo.domain.model.Location
import com.example.barriovivo.domain.model.MealPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CreateMealUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val expiryDateError: String? = null
)

@HiltViewModel
class CreateMealViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMealUiState())
    val uiState: StateFlow<CreateMealUiState> = _uiState.asStateFlow()

    fun createMealPost(
        userId: String,
        userName: String,
        title: String,
        description: String,
        photoUris: List<String>,
        expiryDate: LocalDate,
        location: Location
    ) {
        // Validar que la fecha de caducidad no sea anterior a hoy
        if (expiryDate.isBefore(LocalDate.now())) {
            _uiState.value = _uiState.value.copy(
                expiryDateError = "La fecha de caducidad no puede ser anterior a hoy"
            )
            return
        }

        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre de la comida no puede estar vacío"
            )
            return
        }

        // Validación: al menos una foto obligatoria
        if (photoUris.isEmpty() || photoUris.all { it.isBlank() }) {
            _uiState.value = _uiState.value.copy(
                error = "Es obligatorio añadir al menos una foto"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = mealPostRepository.createMealPost(
                userId = userId,
                userName = userName,
                title = title,
                description = description,
                photoUris = photoUris,
                expiryDate = expiryDate,
                location = location
            )

            result.onSuccess { mealPost ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    success = true,
                    expiryDateError = null
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al crear la comida"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null,
            expiryDateError = null,
            success = false
        )
    }
}
