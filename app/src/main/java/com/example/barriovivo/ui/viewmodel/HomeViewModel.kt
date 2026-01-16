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

data class HomeUiState(
    val nearbyMealPosts: List<MealPost> = emptyList(),
    val userMealPosts: List<MealPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealPostRepository: MealPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var nearbyPostsJob: Job? = null
    private var userPostsJob: Job? = null

    // Guardar las coordenadas para poder refrescar
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private var lastUserId: String = ""

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
        if (lastLatitude != 0.0 || lastLongitude != 0.0) {
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

