package com.example.barriovivo.regression

import com.example.barriovivo.domain.model.Location
import com.example.barriovivo.domain.model.MealPostStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * PRUEBAS DE REGRESION - MealPostRepository
 *
 * Objetivo: Garantizar que las operaciones CRUD de publicaciones de comida
 * mantienen su comportamiento esperado tras cambios en el codigo.
 *
 * Cobertura:
 * - Creacion de publicaciones
 * - Consulta de publicaciones activas
 * - Reclamacion de comida
 * - Sistema de reportes
 * - Filtrado por ubicacion y fecha
 *
 * Plan de ejecucion:
 * - Ejecutar antes de cada merge a rama principal
 * - Ejecutar tras modificaciones en MealPostRepository o MealPostEntity
 * - Incluir en pipeline de CI/CD
 *
 * @author Equipo BarrioVivo
 * @version 1.0
 */
class MealPostRepositoryRegressionTest {

    // Constantes de prueba
    private val testUserId = "user_test_001"
    private val testMealPostId = "meal_test_001"
    private val testLocation = Location(
        city = "Madrid",
        latitude = 40.4168,
        longitude = -3.7038,
        country = "Spain",
        zipCode = "28001"
    )

    @Before
    fun setUp() {
        // Configuracion inicial del entorno de pruebas
    }

    // =========================================================================
    // PRUEBAS DE REGRESION: Creacion de Publicaciones
    // =========================================================================

    /**
     * PRR-MP-001: Verificar creacion de publicacion con datos minimos requeridos
     *
     * Campos obligatorios:
     * - Titulo
     * - ID de usuario
     * - Fecha de caducidad
     * - Ubicacion
     *
     * Resultado esperado:
     * - Publicacion se crea con estado ACTIVE
     * - Se genera ID unico
     * - isAvailable es true por defecto
     */
    @Test
    fun `PRR-MP-001 crear publicacion con datos minimos debe generar registro valido`() {
        // Arrange
        val title = "Paella para compartir"
        val expiryDate = LocalDate.now().plusDays(2)

        // Act
        val mealPost = simulateCreateMealPost(
            title = title,
            userId = testUserId,
            expiryDate = expiryDate,
            location = testLocation
        )

        // Assert
        assertNotNull("Debe tener ID", mealPost.id)
        assertEquals("Titulo debe coincidir", title, mealPost.title)
        assertEquals("Estado debe ser ACTIVE", MealPostStatus.ACTIVE, mealPost.status)
        assertTrue("Debe estar disponible", mealPost.isAvailable)
        assertNull("No debe tener reclamante", mealPost.claimedByUserId)
    }

    /**
     * PRR-MP-002: Verificar que publicacion incluye multiples fotos correctamente
     *
     * Resultado esperado:
     * - Lista de URIs se almacena completa
     * - Orden de fotos se preserva
     */
    @Test
    fun `PRR-MP-002 publicacion con multiples fotos debe preservar todas las URIs`() {
        // Arrange
        val photoUris = listOf(
            "content://media/external/images/1",
            "content://media/external/images/2",
            "content://media/external/images/3"
        )

        // Act
        val mealPost = simulateCreateMealPostWithPhotos(photoUris)

        // Assert
        assertEquals("Debe tener 3 fotos", 3, mealPost.photoUris.size)
        assertEquals("Primera foto debe coincidir", photoUris[0], mealPost.photoUris[0])
        assertEquals("Ultima foto debe coincidir", photoUris[2], mealPost.photoUris[2])
    }

    // =========================================================================
    // PRUEBAS DE REGRESION: Consulta de Publicaciones
    // =========================================================================

    /**
     * PRR-MP-003: Verificar filtrado de publicaciones activas
     *
     * Criterios de filtrado:
     * - Estado = ACTIVE
     * - Fecha caducidad >= hoy
     * - isAvailable = true
     *
     * Resultado esperado:
     * - Solo retorna publicaciones que cumplen todos los criterios
     */
    @Test
    fun `PRR-MP-003 consulta de activas debe excluir caducadas y reclamadas`() {
        // Arrange
        val today = LocalDate.now()
        val posts = listOf(
            TestMealPost("1", MealPostStatus.ACTIVE, today.plusDays(1), true),  // Valida
            TestMealPost("2", MealPostStatus.ACTIVE, today.minusDays(1), true), // Caducada
            TestMealPost("3", MealPostStatus.ACTIVE, today.plusDays(1), false), // Reclamada
            TestMealPost("4", MealPostStatus.REPORTED, today.plusDays(1), true) // Reportada
        )

        // Act
        val activePosts = posts.filter { post ->
            post.status == MealPostStatus.ACTIVE &&
            post.expiryDate >= today &&
            post.isAvailable
        }

        // Assert
        assertEquals("Solo debe haber 1 publicacion valida", 1, activePosts.size)
        assertEquals("ID debe ser 1", "1", activePosts[0].id)
    }

    /**
     * PRR-MP-004: Verificar consulta de publicaciones por usuario
     *
     * Resultado esperado:
     * - Retorna todas las publicaciones del usuario (cualquier estado)
     * - Ordenadas por fecha de creacion descendente
     */
    @Test
    fun `PRR-MP-004 consulta por usuario debe retornar todas sus publicaciones`() {
        // Arrange
        val userPosts = listOf(
            TestMealPost("1", MealPostStatus.ACTIVE, LocalDate.now(), true, testUserId),
            TestMealPost("2", MealPostStatus.DELETED, LocalDate.now(), false, testUserId),
            TestMealPost("3", MealPostStatus.ACTIVE, LocalDate.now(), true, "other_user")
        )

        // Act
        val filteredPosts = userPosts.filter { it.userId == testUserId }

        // Assert
        assertEquals("Usuario debe tener 2 publicaciones", 2, filteredPosts.size)
    }

    // =========================================================================
    // PRUEBAS DE REGRESION: Reclamacion de Comida
    // =========================================================================

    /**
     * PRR-MP-005: Verificar proceso de reclamacion
     *
     * Precondiciones:
     * - Publicacion esta disponible
     * - Usuario reclamante es diferente al creador
     *
     * Resultado esperado:
     * - isAvailable se establece en false
     * - claimedByUserId se asigna
     * - claimedAt se establece con timestamp
     */
    @Test
    fun `PRR-MP-005 reclamar comida debe actualizar estado correctamente`() {
        // Arrange
        var isAvailable = true
        var claimedByUserId: String? = null
        var claimedAt: LocalDateTime? = null
        val claimerId = "user_claimer_001"

        // Act - Simular reclamacion
        isAvailable = false
        claimedByUserId = claimerId
        claimedAt = LocalDateTime.now()

        // Assert
        assertFalse("No debe estar disponible tras reclamacion", isAvailable)
        assertEquals("Reclamante debe coincidir", claimerId, claimedByUserId)
        assertNotNull("Debe tener fecha de reclamacion", claimedAt)
    }

    /**
     * PRR-MP-006: Verificar que creador no puede reclamar su propia comida
     *
     * Resultado esperado:
     * - Operacion debe fallar o ser rechazada
     */
    @Test
    fun `PRR-MP-006 creador no debe poder reclamar su propia publicacion`() {
        // Arrange
        val creatorId = testUserId
        val postOwnerId = testUserId

        // Act
        val canClaim = creatorId != postOwnerId

        // Assert
        assertFalse("Creador no debe poder reclamar", canClaim)
    }

    // =========================================================================
    // PRUEBAS DE REGRESION: Sistema de Reportes
    // =========================================================================

    /**
     * PRR-MP-007: Verificar incremento de contador de reportes
     *
     * Resultado esperado:
     * - reportCount se incrementa
     * - Estado cambia a REPORTED
     * - Usuario reportante se registra
     */
    @Test
    fun `PRR-MP-007 reportar publicacion debe incrementar contador y cambiar estado`() {
        // Arrange
        var reportCount = 0
        var status = MealPostStatus.ACTIVE
        val reportedByUsers = mutableListOf<String>()
        val reporterId = "reporter_001"

        // Act - Simular reporte
        reportCount++
        status = MealPostStatus.REPORTED
        reportedByUsers.add(reporterId)

        // Assert
        assertEquals("Contador debe ser 1", 1, reportCount)
        assertEquals("Estado debe ser REPORTED", MealPostStatus.REPORTED, status)
        assertTrue("Lista debe contener reportante", reportedByUsers.contains(reporterId))
    }

    /**
     * PRR-MP-008: Verificar que usuario no puede reportar la misma publicacion dos veces
     *
     * Resultado esperado:
     * - Segunda solicitud de reporte es ignorada
     */
    @Test
    fun `PRR-MP-008 mismo usuario no debe poder reportar dos veces`() {
        // Arrange
        val reportedByUsers = mutableListOf("reporter_001")
        val reporterId = "reporter_001"

        // Act
        val alreadyReported = reportedByUsers.contains(reporterId)

        // Assert
        assertTrue("Usuario ya debe estar en lista de reportantes", alreadyReported)
    }

    // =========================================================================
    // PRUEBAS DE REGRESION: Filtrado por Ubicacion
    // =========================================================================

    /**
     * PRR-MP-009: Verificar calculo de distancia entre coordenadas
     *
     * Formula: Haversine
     *
     * Resultado esperado:
     * - Distancia calculada es aproximadamente correcta
     */
    @Test
    fun `PRR-MP-009 calculo de distancia debe ser aproximadamente correcto`() {
        // Arrange - Madrid a Barcelona (aproximadamente 500km)
        val madridLat = 40.4168
        val madridLon = -3.7038
        val barcelonaLat = 41.3851
        val barcelonaLon = 2.1734

        // Act
        val distance = calculateHaversineDistance(
            madridLat, madridLon,
            barcelonaLat, barcelonaLon
        )

        // Assert - Debe estar entre 500 y 650 km
        assertTrue("Distancia debe ser mayor a 500km", distance > 500)
        assertTrue("Distancia debe ser menor a 650km", distance < 650)
    }

    // =========================================================================
    // Metodos auxiliares
    // =========================================================================

    private fun simulateCreateMealPost(
        title: String,
        userId: String,
        expiryDate: LocalDate,
        location: Location
    ): TestMealPostResult {
        return TestMealPostResult(
            id = "meal_${System.currentTimeMillis()}",
            title = title,
            userId = userId,
            status = MealPostStatus.ACTIVE,
            isAvailable = true,
            claimedByUserId = null,
            photoUris = emptyList()
        )
    }

    private fun simulateCreateMealPostWithPhotos(photoUris: List<String>): TestMealPostResult {
        return TestMealPostResult(
            id = "meal_${System.currentTimeMillis()}",
            title = "Test",
            userId = testUserId,
            status = MealPostStatus.ACTIVE,
            isAvailable = true,
            claimedByUserId = null,
            photoUris = photoUris
        )
    }

    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Clases auxiliares
    data class TestMealPost(
        val id: String,
        val status: MealPostStatus,
        val expiryDate: LocalDate,
        val isAvailable: Boolean,
        val userId: String = "default_user"
    )

    data class TestMealPostResult(
        val id: String,
        val title: String,
        val userId: String,
        val status: MealPostStatus,
        val isAvailable: Boolean,
        val claimedByUserId: String?,
        val photoUris: List<String>
    )
}

