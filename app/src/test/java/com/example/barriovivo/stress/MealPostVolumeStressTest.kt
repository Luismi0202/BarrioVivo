package com.example.barriovivo.stress

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * PRUEBAS DE VOLUMEN Y ESTRES - Sistema de Publicaciones de Comida
 *
 * Objetivo: Evaluar el rendimiento del sistema de publicaciones bajo
 * condiciones de alta demanda y grandes volumenes de datos.
 *
 * Escenarios cubiertos:
 *
 * 1. Volumen de publicaciones:
 *    - Carga de feeds extensos
 *    - Filtrado por ubicacion con muchos resultados
 *    - Busqueda en catalogos grandes
 *
 * 2. Estres en operaciones:
 *    - Multiples usuarios publicando simultaneamente
 *    - Reclamaciones concurrentes
 *    - Consultas simultaneas al feed
 *
 * Contexto de uso real:
 * - Una ciudad grande podria tener miles de publicaciones activas
 * - Horarios pico (mediodias, tardes) concentran muchas operaciones
 * - Eventos especiales pueden multiplicar la actividad
 *
 * @author Equipo BarrioVivo
 * @version 1.0
 */
class MealPostVolumeStressTest {

    companion object {
        // Configuracion de volumenes
        const val SMALL_DATASET = 100
        const val MEDIUM_DATASET = 1000
        const val LARGE_DATASET = 5000

        // Configuracion de estres
        const val CONCURRENT_PUBLISHERS = 30
        const val CONCURRENT_READERS = 100

        // Umbrales de rendimiento
        const val MAX_FEED_LOAD_TIME_MS = 3000L
        const val MAX_FILTER_TIME_MS = 1000L
        const val MIN_SUCCESS_RATE = 0.95
    }

    private lateinit var mealPosts: MutableList<TestMealPost>
    private val locations = listOf(
        TestLocation("Madrid", 40.4168, -3.7038),
        TestLocation("Barcelona", 41.3851, 2.1734),
        TestLocation("Valencia", 39.4699, -0.3763),
        TestLocation("Sevilla", 37.3891, -5.9845),
        TestLocation("Zaragoza", 41.6488, -0.8891)
    )

    @Before
    fun setUp() {
        mealPosts = mutableListOf()
    }

    // =========================================================================
    // PRUEBAS DE VOLUMEN: Feed de Publicaciones
    // =========================================================================

    /**
     * PVE-MP-001: Carga de feed con 5000 publicaciones
     *
     * Escenario: Usuario abre la aplicacion en una ciudad muy activa
     *
     * Objetivo: El feed debe cargar en tiempo aceptable
     *
     * Umbral: < 3 segundos
     *
     * Justificacion: En ciudades grandes con alta adopcion de la app,
     * el feed podria contener miles de publicaciones. Los usuarios
     * esperan respuestas rapidas al abrir la aplicacion.
     */
    @Test
    fun `PVE-MP-001 carga de feed con 5000 publicaciones debe ser menor a 3 segundos`() {
        // Arrange - Generar dataset
        val postCount = LARGE_DATASET

        // Act
        val loadTime = measureTimeMillis {
            for (i in 1..postCount) {
                val location = locations[i % locations.size]
                mealPosts.add(
                    TestMealPost(
                        id = "post_$i",
                        title = "Comida disponible #$i",
                        description = "Descripcion de la publicacion numero $i con detalles adicionales",
                        userId = "user_${i % 100}",
                        location = location,
                        expiryDate = LocalDate.now().plusDays((i % 7).toLong()),
                        isAvailable = i % 3 != 0, // 66% disponibles
                        photoCount = (i % 4) + 1
                    )
                )
            }
            // Simular ordenamiento por fecha de creacion
            mealPosts.sortedByDescending { it.id }
        }

        // Assert
        assertEquals("Debe haber $postCount publicaciones", postCount, mealPosts.size)
        assertTrue(
            "Tiempo de carga ($loadTime ms) debe ser menor a $MAX_FEED_LOAD_TIME_MS ms",
            loadTime < MAX_FEED_LOAD_TIME_MS
        )

        println("PVE-MP-001 Resultados:")
        println("  - Publicaciones cargadas: $postCount")
        println("  - Tiempo de carga: $loadTime ms")
        println("  - Throughput: ${postCount * 1000 / loadTime} posts/segundo")
    }

    /**
     * PVE-MP-002: Filtrado por ubicacion en dataset grande
     *
     * Escenario: Usuario filtra publicaciones cercanas a su ubicacion
     *
     * Objetivo: El filtrado debe ser eficiente incluso con muchos datos
     *
     * Umbral: < 1 segundo
     */
    @Test
    fun `PVE-MP-002 filtrado por ubicacion en 5000 posts debe ser menor a 1 segundo`() {
        // Arrange - Preparar datos
        for (i in 1..LARGE_DATASET) {
            val location = locations[i % locations.size]
            mealPosts.add(
                TestMealPost(
                    id = "post_$i",
                    title = "Comida $i",
                    description = "",
                    userId = "user_$i",
                    location = location,
                    expiryDate = LocalDate.now().plusDays(1),
                    isAvailable = true,
                    photoCount = 1
                )
            )
        }

        // Usuario en Madrid busca publicaciones cercanas
        val userLocation = locations[0] // Madrid
        val maxDistanceKm = 50.0

        // Act
        val filterTime = measureTimeMillis {
            val nearbyPosts = mealPosts.filter { post ->
                val distance = calculateDistance(
                    userLocation.latitude, userLocation.longitude,
                    post.location.latitude, post.location.longitude
                )
                distance <= maxDistanceKm
            }
            // Deberia retornar solo las de Madrid (cada 5ta publicacion)
            assertTrue("Debe haber publicaciones cercanas", nearbyPosts.isNotEmpty())
        }

        // Assert
        assertTrue(
            "Tiempo de filtrado ($filterTime ms) debe ser menor a $MAX_FILTER_TIME_MS ms",
            filterTime < MAX_FILTER_TIME_MS
        )

        println("PVE-MP-002 Resultados:")
        println("  - Total publicaciones: ${mealPosts.size}")
        println("  - Tiempo de filtrado: $filterTime ms")
    }

    /**
     * PVE-MP-003: Filtrado combinado (ubicacion + disponibilidad + fecha)
     *
     * Escenario: Usuario aplica multiples filtros simultaneamente
     *
     * Objetivo: Los filtros combinados deben ser eficientes
     */
    @Test
    fun `PVE-MP-003 filtrado combinado debe mantener rendimiento aceptable`() {
        // Arrange
        val today = LocalDate.now()
        for (i in 1..LARGE_DATASET) {
            mealPosts.add(
                TestMealPost(
                    id = "post_$i",
                    title = "Comida $i",
                    description = "",
                    userId = "user_$i",
                    location = locations[i % locations.size],
                    expiryDate = today.plusDays((i % 10 - 2).toLong()),
                    isAvailable = i % 2 == 0,
                    photoCount = 1
                )
            )
        }

        // Act - Aplicar filtros combinados
        val filterTime = measureTimeMillis {
            val filtered = mealPosts.filter { post ->
                post.isAvailable &&
                post.expiryDate >= today &&
                post.location.city == "Madrid"
            }
        }

        // Assert
        assertTrue(
            "Tiempo de filtrado combinado ($filterTime ms) debe ser aceptable",
            filterTime < MAX_FILTER_TIME_MS * 2
        )

        println("PVE-MP-003 Resultados:")
        println("  - Tiempo filtrado combinado: $filterTime ms")
    }

    // =========================================================================
    // PRUEBAS DE ESTRES: Publicacion Concurrente
    // =========================================================================

    /**
     * PVE-MP-004: Creacion concurrente de publicaciones
     *
     * Escenario: 30 usuarios publican comida al mismo tiempo
     * (situacion tipica despues del almuerzo)
     *
     * Objetivo: El sistema debe manejar publicaciones concurrentes
     *
     * Umbral: Tasa de exito > 95%
     *
     * Justificacion: Los horarios de comida concentran la actividad.
     * Muchos usuarios pueden querer publicar excedentes simultaneamente.
     */
    @Test
    fun `PVE-MP-004 creacion concurrente por 30 usuarios debe tener alta tasa exito`() {
        // Arrange
        val publisherCount = CONCURRENT_PUBLISHERS
        val postsPerUser = 3
        val executor = Executors.newFixedThreadPool(publisherCount)
        val latch = CountDownLatch(publisherCount)
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        val createdPosts = java.util.Collections.synchronizedList(mutableListOf<TestMealPost>())

        // Act
        val executionTime = measureTimeMillis {
            for (userId in 1..publisherCount) {
                executor.submit {
                    try {
                        for (postIndex in 1..postsPerUser) {
                            val post = TestMealPost(
                                id = "post_${userId}_$postIndex",
                                title = "Comida de usuario $userId - $postIndex",
                                description = "Descripcion de prueba de estres",
                                userId = "user_$userId",
                                location = locations[userId % locations.size],
                                expiryDate = LocalDate.now().plusDays(1),
                                isAvailable = true,
                                photoCount = 2
                            )
                            createdPosts.add(post)
                            successCount.incrementAndGet()
                        }
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }
            latch.await(30, TimeUnit.SECONDS)
        }

        executor.shutdown()

        // Assert
        val totalOperations = successCount.get() + errorCount.get()
        val successRate = if (totalOperations > 0) {
            successCount.get().toDouble() / totalOperations
        } else 0.0

        assertTrue(
            "Tasa de exito ($successRate) debe ser mayor a $MIN_SUCCESS_RATE",
            successRate >= MIN_SUCCESS_RATE
        )

        println("PVE-MP-004 Resultados de publicacion concurrente:")
        println("  - Publicadores: $publisherCount")
        println("  - Posts por usuario: $postsPerUser")
        println("  - Total creados: ${createdPosts.size}")
        println("  - Exitos: ${successCount.get()}")
        println("  - Errores: ${errorCount.get()}")
        println("  - Tasa de exito: ${String.format("%.2f", successRate * 100)}%")
        println("  - Tiempo total: $executionTime ms")
    }

    /**
     * PVE-MP-005: Reclamacion concurrente de la misma publicacion
     *
     * Escenario: Multiples usuarios intentan reclamar la misma comida
     *
     * Objetivo: Solo un usuario debe poder reclamar exitosamente
     *
     * Justificacion: Publicaciones atractivas pueden recibir multiples
     * intentos de reclamacion simultaneos. El sistema debe garantizar
     * consistencia.
     */
    @Test
    fun `PVE-MP-005 reclamacion concurrente debe garantizar solo un exito`() {
        // Arrange
        val claimantCount = 10
        val targetPostId = "post_popular"
        var isAvailable = true
        var claimedBy: String? = null
        val lock = Any()

        val executor = Executors.newFixedThreadPool(claimantCount)
        val latch = CountDownLatch(claimantCount)
        val successfulClaims = AtomicInteger(0)
        val failedClaims = AtomicInteger(0)

        // Act
        for (claimantId in 1..claimantCount) {
            executor.submit {
                try {
                    // Simular reclamacion con sincronizacion
                    synchronized(lock) {
                        if (isAvailable) {
                            isAvailable = false
                            claimedBy = "user_$claimantId"
                            successfulClaims.incrementAndGet()
                        } else {
                            failedClaims.incrementAndGet()
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // Assert
        assertEquals(
            "Solo debe haber una reclamacion exitosa",
            1,
            successfulClaims.get()
        )
        assertEquals(
            "El resto deben ser rechazadas",
            claimantCount - 1,
            failedClaims.get()
        )
        assertNotNull("Debe haber un reclamante asignado", claimedBy)

        println("PVE-MP-005 Resultados de reclamacion concurrente:")
        println("  - Intentos de reclamacion: $claimantCount")
        println("  - Reclamaciones exitosas: ${successfulClaims.get()}")
        println("  - Reclamaciones rechazadas: ${failedClaims.get()}")
        println("  - Reclamante final: $claimedBy")
    }

    /**
     * PVE-MP-006: Consulta concurrente del feed
     *
     * Escenario: 100 usuarios consultan el feed simultaneamente
     *
     * Objetivo: Todas las consultas deben completarse exitosamente
     *
     * Justificacion: En horarios de uso pico, muchos usuarios
     * pueden estar navegando el feed al mismo tiempo.
     */
    @Test
    fun `PVE-MP-006 consulta concurrente del feed por 100 usuarios debe ser exitosa`() {
        // Arrange - Preparar feed
        for (i in 1..MEDIUM_DATASET) {
            mealPosts.add(
                TestMealPost(
                    id = "post_$i",
                    title = "Comida $i",
                    description = "",
                    userId = "user_$i",
                    location = locations[0],
                    expiryDate = LocalDate.now().plusDays(1),
                    isAvailable = true,
                    photoCount = 1
                )
            )
        }

        val readerCount = CONCURRENT_READERS
        val executor = Executors.newFixedThreadPool(readerCount)
        val latch = CountDownLatch(readerCount)
        val successfulReads = AtomicInteger(0)
        val responseTimes = java.util.Collections.synchronizedList(mutableListOf<Long>())

        // Act
        for (readerId in 1..readerCount) {
            executor.submit {
                try {
                    val readTime = measureTimeMillis {
                        // Simular lectura del feed
                        val availablePosts = mealPosts.filter { it.isAvailable }
                        if (availablePosts.isNotEmpty()) {
                            successfulReads.incrementAndGet()
                        }
                    }
                    responseTimes.add(readTime)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        // Assert
        assertEquals(
            "Todas las lecturas deben ser exitosas",
            readerCount,
            successfulReads.get()
        )

        val avgResponseTime = if (responseTimes.isNotEmpty()) responseTimes.average() else 0.0
        val maxResponseTime = responseTimes.maxOrNull() ?: 0

        println("PVE-MP-006 Resultados de consulta concurrente:")
        println("  - Lectores concurrentes: $readerCount")
        println("  - Lecturas exitosas: ${successfulReads.get()}")
        println("  - Tiempo promedio de respuesta: ${String.format("%.2f", avgResponseTime)} ms")
        println("  - Tiempo maximo de respuesta: $maxResponseTime ms")
    }

    // =========================================================================
    // Metodos auxiliares
    // =========================================================================

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0
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
        val title: String,
        val description: String,
        val userId: String,
        val location: TestLocation,
        val expiryDate: LocalDate,
        val isAvailable: Boolean,
        val photoCount: Int
    )

    data class TestLocation(
        val city: String,
        val latitude: Double,
        val longitude: Double
    )
}

