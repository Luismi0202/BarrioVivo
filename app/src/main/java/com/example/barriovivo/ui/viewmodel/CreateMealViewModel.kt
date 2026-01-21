package com.example.barriovivo.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.MealPostRepository
import com.example.barriovivo.data.repository.UserRepository
import com.example.barriovivo.domain.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de creacion de publicacion.
 *
 * @property isLoading Indica operacion en progreso
 * @property isSuccess Creacion exitosa
 * @property error Mensaje de error
 */
data class CreateMealUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la creacion de publicaciones de comida.
 *
 * Gestiona el proceso de crear una nueva publicacion:
 * - Validacion de campos requeridos
 * - Obtencion automatica de ubicacion del usuario
 * - Almacenamiento de URIs de fotos
 *
 * La publicacion se crea con estado ACTIVE directamente,
 * sin necesidad de aprobacion previa.
 *
 * @property mealPostRepository Repositorio de publicaciones
 * @property userRepository Repositorio para obtener datos del usuario
 */
@HiltViewModel
class CreateMealViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository,
    private val userRepository: UserRepository
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
        // Evitar doble publicación si ya está en proceso
        if (_uiState.value.isLoading) {
            return
        }

        // Reiniciar el estado de éxito para permitir nuevas publicaciones
        _uiState.value = _uiState.value.copy(isSuccess = false, error = null)

        // Validar que la fecha de caducidad no sea anterior a hoy
        if (expiryDate.isBefore(LocalDate.now())) {
            _uiState.value = CreateMealUiState(error = "La fecha de caducidad no puede ser anterior a hoy")
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
                    _uiState.value = CreateMealUiState(isLoading = false, isSuccess = true)
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
            error = null
        )
    }

    fun resetState() {
        _uiState.value = CreateMealUiState()
    }
}
