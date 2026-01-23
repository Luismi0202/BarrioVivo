package com.example.barriovivo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barriovivo.data.repository.AppStatistics
import com.example.barriovivo.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de informes.
 *
 * @property statistics Estadísticas actuales de la app
 * @property summaryReport Contenido del informe resumido en texto
 * @property isLoading Indica si hay una operación en curso
 * @property error Mensaje de error si algo falla
 * @property successMessage Mensaje de éxito después de una operación
 * @property exportedFilePath Ruta del último archivo exportado
 */
data class ReportUiState(
    val statistics: AppStatistics? = null,
    val summaryReport: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val exportedFilePath: String? = null
)

/**
 * ViewModel para la gestión de informes y estadísticas.
 *
 * Proporciona funcionalidades para:
 * - Cargar estadísticas en tiempo real
 * - Generar informes resumidos
 * - Exportar informes a archivos CSV y texto
 * - Filtrar datos por diferentes criterios
 *
 * @property reportRepository Repositorio para generación de informes
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * Carga las estadísticas generales de la aplicación.
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val stats = reportRepository.generateStatistics()
                _uiState.value = _uiState.value.copy(
                    statistics = stats,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }

    /**
     * Genera el informe resumido en formato de texto.
     */
    fun generateSummaryReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reportRepository.generateSummaryReport()
            result.onSuccess { report ->
                _uiState.value = _uiState.value.copy(
                    summaryReport = report,
                    isLoading = false,
                    error = null
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al generar informe"
                )
            }
        }
    }

    /**
     * Exporta el informe de publicaciones a un archivo CSV.
     *
     * @param filterStatus Filtrar por estado (opcional)
     * @param filterCity Filtrar por ciudad (opcional)
     */
    fun exportPostsToCSV(filterStatus: String? = null, filterCity: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reportRepository.generatePostsReportCSV(filterStatus, filterCity)
            result.onSuccess { filePath ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportedFilePath = filePath,
                    successMessage = "Informe CSV exportado correctamente",
                    error = null
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al exportar CSV"
                )
            }
        }
    }

    /**
     * Exporta el informe resumido a un archivo de texto.
     */
    fun exportSummaryToFile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reportRepository.exportSummaryReportToFile()
            result.onSuccess { filePath ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportedFilePath = filePath,
                    successMessage = "Informe exportado correctamente",
                    error = null
                )
            }
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al exportar informe"
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Limpia el mensaje de éxito.
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Limpia el informe resumido.
     */
    fun clearSummaryReport() {
        _uiState.value = _uiState.value.copy(summaryReport = null)
    }
}

