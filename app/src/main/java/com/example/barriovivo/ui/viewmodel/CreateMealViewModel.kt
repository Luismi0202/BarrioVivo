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
        // Reiniciar el estado de éxito para permitir nuevas publicaciones
        _uiState.value = _uiState.value.copy(success = false, error = null, expiryDateError = null)

        // Validar que la fecha de caducidad no sea anterior a hoy
        if (expiryDate.isBefore(LocalDate.now())) {
            _uiState.value = CreateMealUiState(expiryDateError = "La fecha de caducidad no puede ser anterior a hoy")
            return
        }

        if (title.isBlank()) {
            _uiState.value = CreateMealUiState(error = "El nombre de la comida no puede estar vacío")
            return
        }

        // Validación: al menos una foto obligatoria
        if (photoUris.isEmpty() || photoUris.all { it.isBlank() }) {
            _uiState.value = CreateMealUiState(error = "Es obligatorio añadir al menos una foto")
            return
        }

        viewModelScope.launch {
            // Indicar que la carga ha comenzado
            _uiState.value = CreateMealUiState(isLoading = true)

            val result = mealPostRepository.createMealPost(
                userId = userId,
                userName = userName,
                title = title,
                description = description,
                photoUris = photoUris,
                expiryDate = expiryDate,
                location = location
            )

            // Actualizar el estado de la UI con el resultado
            result.fold(
                onSuccess = {
                    _uiState.value = CreateMealUiState(isLoading = false, success = true)
                },
                onFailure = { exception ->
                    _uiState.value = CreateMealUiState(
                        isLoading = false,
                        error = exception.message ?: "Error desconocido al crear la comida"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null,
            expiryDateError = null
        )
    }

    fun resetState() {
        _uiState.value = CreateMealUiState()
    }
}
