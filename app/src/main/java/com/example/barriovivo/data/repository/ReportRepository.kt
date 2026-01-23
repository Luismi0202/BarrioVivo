package com.example.barriovivo.data.repository

import android.content.Context
import android.os.Environment
import com.example.barriovivo.data.database.dao.MealPostDao
import com.example.barriovivo.data.database.dao.UserDao
import com.example.barriovivo.data.database.dao.ChatConversationDao
import com.example.barriovivo.data.database.dao.ChatMessageDao
import com.example.barriovivo.domain.model.MealPostStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modelo de datos para estadísticas generales de la aplicación.
 * Contiene todos los datos necesarios para generar informes completos.
 *
 * @property totalUsers Total de usuarios registrados
 * @property totalPosts Total de publicaciones creadas
 * @property activePosts Publicaciones activas disponibles
 * @property claimedPosts Publicaciones que han sido reclamadas
 * @property reportedPosts Publicaciones con reportes pendientes
 * @property deletedPosts Publicaciones eliminadas por admin
 * @property expiredPosts Publicaciones expiradas
 * @property totalConversations Total de conversaciones de chat
 * @property activeConversations Conversaciones activas
 * @property totalMessages Total de mensajes enviados
 * @property postsByCity Publicaciones agrupadas por ciudad
 * @property postsByDay Publicaciones agrupadas por día (últimos 7 días)
 * @property topActiveUsers Usuarios más activos (por número de publicaciones)
 * @property generatedAt Fecha y hora de generación del informe
 */
data class AppStatistics(
    val totalUsers: Int = 0,
    val totalPosts: Int = 0,
    val activePosts: Int = 0,
    val claimedPosts: Int = 0,
    val reportedPosts: Int = 0,
    val deletedPosts: Int = 0,
    val expiredPosts: Int = 0,
    val totalConversations: Int = 0,
    val activeConversations: Int = 0,
    val totalMessages: Int = 0,
    val postsByCity: Map<String, Int> = emptyMap(),
    val postsByDay: Map<String, Int> = emptyMap(),
    val topActiveUsers: List<Pair<String, Int>> = emptyList(),
    val generatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Modelo para datos de un informe de publicaciones.
 */
data class PostReportData(
    val id: String,
    val title: String,
    val userName: String,
    val city: String,
    val status: String,
    val createdAt: String,
    val expiryDate: String,
    val isAvailable: Boolean,
    val reportCount: Int,
    val lastReportReason: String
)

/**
 * Repositorio para la generación de informes y estadísticas de la aplicación.
 *
 * Este repositorio centraliza toda la lógica de recopilación de datos y generación
 * de informes en diferentes formatos (CSV, texto plano). Los informes incluyen:
 * - Estadísticas generales de uso
 * - Listado de publicaciones con filtros
 * - Datos de actividad por zona
 * - Métricas de moderación
 *
 * @property context Contexto de la aplicación para acceso a almacenamiento
 * @property mealPostDao DAO para acceso a publicaciones
 * @property userDao DAO para acceso a usuarios
 * @property chatConversationDao DAO para acceso a conversaciones
 * @property chatMessageDao DAO para acceso a mensajes
 */
@Singleton
class ReportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mealPostDao: MealPostDao,
    private val userDao: UserDao,
    private val chatConversationDao: ChatConversationDao,
    private val chatMessageDao: ChatMessageDao
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    /**
     * Genera estadísticas completas de la aplicación.
     * Recopila datos de todas las tablas de la base de datos y calcula métricas.
     *
     * @return AppStatistics con todos los datos calculados
     */
    suspend fun generateStatistics(): AppStatistics = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val allPosts = mealPostDao.getAllActivePosts().first()
        val reportedPosts = mealPostDao.getReportedMealPosts().first()
        val activeConversationsList = chatConversationDao.getAllActiveConversations().first()

        // Calcular posts por estado
        val activePosts = allPosts.filter { post ->
            post.status == MealPostStatus.ACTIVE.name &&
            post.isAvailable &&
            post.expiryDate >= today
        }
        val claimedPosts = allPosts.filter { post -> !post.isAvailable && post.claimedByUserId != null }
        val expiredPosts = allPosts.filter { post -> post.expiryDate < today }
        val deletedPosts = allPosts.filter { post -> post.status == MealPostStatus.DELETED.name }

        // Calcular posts por ciudad
        val postsByCity = allPosts
            .groupBy { post -> post.city.ifBlank { "Sin ciudad" } }
            .mapValues { entry -> entry.value.size }
            .toList()
            .sortedByDescending { pair -> pair.second }
            .toMap()

        // Calcular posts por día (últimos 7 días)
        val lastWeek = today.minusDays(7)
        val postsByDay = allPosts
            .filter { post -> post.createdAt.toLocalDate() >= lastWeek }
            .groupBy { post -> post.createdAt.toLocalDate().format(dateFormatter) }
            .mapValues { entry -> entry.value.size }

        // Top usuarios activos
        val topActiveUsers = allPosts
            .groupBy { post -> post.userName.ifBlank { "Usuario desconocido" } }
            .mapValues { entry -> entry.value.size }
            .toList()
            .sortedByDescending { pair -> pair.second }
            .take(5)

        // Total de mensajes (aproximación basada en conversaciones activas)
        val totalMessages = activeConversationsList.sumOf { conv ->
            conv.unreadCountCreator + conv.unreadCountClaimer
        }

        AppStatistics(
            totalUsers = 0, // Se podría obtener del UserDao si se necesita
            totalPosts = allPosts.size,
            activePosts = activePosts.size,
            claimedPosts = claimedPosts.size,
            reportedPosts = reportedPosts.size,
            deletedPosts = deletedPosts.size,
            expiredPosts = expiredPosts.size,
            totalConversations = activeConversationsList.size,
            activeConversations = activeConversationsList.size,
            totalMessages = totalMessages,
            postsByCity = postsByCity,
            postsByDay = postsByDay,
            topActiveUsers = topActiveUsers,
            generatedAt = LocalDateTime.now()
        )
    }

    /**
     * Genera un informe en formato CSV con todas las publicaciones.
     * El archivo se guarda en el directorio de documentos de la app.
     *
     * @param filterStatus Filtrar por estado (null = todos)
     * @param filterCity Filtrar por ciudad (null = todas)
     * @return Result con la ruta del archivo generado o error
     */
    suspend fun generatePostsReportCSV(
        filterStatus: String? = null,
        filterCity: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val allPosts = mealPostDao.getAllActivePosts().first()

            // Aplicar filtros
            val filteredPosts = allPosts.filter { post ->
                val statusMatch = filterStatus == null || post.status == filterStatus
                val cityMatch = filterCity == null || post.city.equals(filterCity, ignoreCase = true)
                statusMatch && cityMatch
            }

            // Crear directorio si no existe
            val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "informes")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            // Nombre del archivo con timestamp
            val fileName = "informe_publicaciones_${LocalDateTime.now().format(fileNameFormatter)}.csv"
            val file = File(reportsDir, fileName)

            // Escribir CSV
            FileWriter(file).use { writer ->
                // Cabecera
                writer.append("ID,Título,Usuario,Ciudad,Estado,Fecha Creación,Fecha Caducidad,Disponible,Reportes,Último Motivo Reporte\n")

                // Datos
                filteredPosts.forEach { post ->
                    writer.append("${post.id},")
                    writer.append("\"${post.title.replace("\"", "\"\"")}\",")
                    writer.append("\"${post.userName.replace("\"", "\"\"")}\",")
                    writer.append("\"${post.city.replace("\"", "\"\"")}\",")
                    writer.append("${post.status},")
                    writer.append("${post.createdAt.format(dateTimeFormatter)},")
                    writer.append("${post.expiryDate.format(dateFormatter)},")
                    writer.append("${if (post.isAvailable) "Sí" else "No"},")
                    writer.append("${post.reportCount},")
                    writer.append("\"${post.lastReportReason.replace("\"", "\"\"")}\"\n")
                }
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un informe resumido en formato de texto plano.
     * Incluye estadísticas generales y desglose por categorías.
     *
     * @return Result con el contenido del informe o error
     */
    suspend fun generateSummaryReport(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val stats = generateStatistics()

            val report = buildString {
                appendLine("═══════════════════════════════════════════════════════════")
                appendLine("           INFORME DE ESTADÍSTICAS - BARRIOVIVO")
                appendLine("═══════════════════════════════════════════════════════════")
                appendLine()
                appendLine("Fecha de generación: ${stats.generatedAt.format(dateTimeFormatter)}")
                appendLine()
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("                    PUBLICACIONES")
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("Total de publicaciones:     ${stats.totalPosts}")
                appendLine("  • Activas:                ${stats.activePosts}")
                appendLine("  • Reclamadas:             ${stats.claimedPosts}")
                appendLine("  • Reportadas:             ${stats.reportedPosts}")
                appendLine("  • Eliminadas por admin:   ${stats.deletedPosts}")
                appendLine("  • Expiradas:              ${stats.expiredPosts}")
                appendLine()
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("                    CHAT Y COMUNICACIÓN")
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("Total de conversaciones:    ${stats.totalConversations}")
                appendLine("  • Activas:                ${stats.activeConversations}")
                appendLine()
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("                 PUBLICACIONES POR CIUDAD")
                appendLine("───────────────────────────────────────────────────────────")
                if (stats.postsByCity.isEmpty()) {
                    appendLine("  No hay datos disponibles")
                } else {
                    stats.postsByCity.entries.take(10).forEach { (city, count) ->
                        appendLine("  • $city: $count publicaciones")
                    }
                }
                appendLine()
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("              ACTIVIDAD ÚLTIMOS 7 DÍAS")
                appendLine("───────────────────────────────────────────────────────────")
                if (stats.postsByDay.isEmpty()) {
                    appendLine("  No hay datos disponibles")
                } else {
                    stats.postsByDay.entries.sortedByDescending { it.key }.forEach { (day, count) ->
                        appendLine("  • $day: $count publicaciones")
                    }
                }
                appendLine()
                appendLine("───────────────────────────────────────────────────────────")
                appendLine("                 USUARIOS MÁS ACTIVOS")
                appendLine("───────────────────────────────────────────────────────────")
                if (stats.topActiveUsers.isEmpty()) {
                    appendLine("  No hay datos disponibles")
                } else {
                    stats.topActiveUsers.forEachIndexed { index, (user, count) ->
                        appendLine("  ${index + 1}. $user: $count publicaciones")
                    }
                }
                appendLine()
                appendLine("═══════════════════════════════════════════════════════════")
                appendLine("                    FIN DEL INFORME")
                appendLine("═══════════════════════════════════════════════════════════")
            }

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exporta el informe resumido a un archivo de texto.
     *
     * @return Result con la ruta del archivo generado o error
     */
    suspend fun exportSummaryReportToFile(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val reportContent = generateSummaryReport().getOrThrow()

            val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "informes")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            val fileName = "resumen_estadisticas_${LocalDateTime.now().format(fileNameFormatter)}.txt"
            val file = File(reportsDir, fileName)

            FileWriter(file).use { writer ->
                writer.write(reportContent)
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un informe de moderación con los posts reportados.
     *
     * @return Result con la lista de datos de posts reportados
     */
    suspend fun generateModerationReport(): Result<List<PostReportData>> = withContext(Dispatchers.IO) {
        try {
            val reportedPosts = mealPostDao.getReportedMealPosts().first()

            val reportData = reportedPosts.map { post ->
                PostReportData(
                    id = post.id,
                    title = post.title,
                    userName = post.userName,
                    city = post.city,
                    status = post.status,
                    createdAt = post.createdAt.format(dateTimeFormatter),
                    expiryDate = post.expiryDate.format(dateFormatter),
                    isAvailable = post.isAvailable,
                    reportCount = post.reportCount,
                    lastReportReason = post.lastReportReason
                )
            }

            Result.success(reportData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

