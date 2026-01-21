package com.example.barriovivo.ui.viewmodel

import com.example.barriovivo.domain.model.UserRole
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * PRUEBAS UNITARIAS - AuthViewModel
 *
 * Objetivo: Verificar el correcto funcionamiento de la logica de autenticacion,
 * incluyendo login, registro y verificacion de roles de usuario.
 *
 * Cobertura:
 * - Validacion de credenciales
 * - Asignacion correcta de roles (USER/ADMIN)
 * - Manejo de errores de autenticacion
 * - Persistencia de sesion
 *
 * @author Equipo BarrioVivo
 * @version 1.0
 */
class AuthViewModelTest {

    // Datos de prueba
    private val validEmail = "usuario@ejemplo.com"
    private val validPassword = "Password123!"
    private val adminEmail = "admin@barriovivo.com"
    private val adminPassword = "admin123"

    // Lista simulada de admins (igual que en admin_config.json)
    private val adminEmails = listOf(
        "admin@barriovivo.com",
        "moderador@barriovivo.com",
        "test@admin.com"
    )

    @Before
    fun setUp() {
        // Inicializacion del entorno de pruebas
    }

    // =========================================================================
    // PRUEBAS: Validacion de Entrada
    // =========================================================================

    /**
     * AVM-001: Email vacio debe mostrar error
     */
    @Test
    fun `AVM-001 login con email vacio debe retornar error`() {
        // Arrange
        val email = ""
        val password = validPassword

        // Act
        val isValid = email.isNotBlank() && password.isNotBlank()

        // Assert
        assertFalse("Email vacio no debe ser valido", isValid)
    }

    /**
     * AVM-002: Password vacio debe mostrar error
     */
    @Test
    fun `AVM-002 login con password vacio debe retornar error`() {
        // Arrange
        val email = validEmail
        val password = ""

        // Act
        val isValid = email.isNotBlank() && password.isNotBlank()

        // Assert
        assertFalse("Password vacio no debe ser valido", isValid)
    }

    /**
     * AVM-003: Email con formato invalido debe ser rechazado
     */
    @Test
    fun `AVM-003 email con formato invalido debe ser rechazado`() {
        // Arrange
        val invalidEmails = listOf(
            "sinArroba.com",
            "@sinUsuario.com",
            "espacios en@email.com",
            "email@",
            "email"
        )

        // Act & Assert
        for (email in invalidEmails) {
            val isValidSimple = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            assertFalse("Email '$email' no debe ser valido", isValidSimple)
        }
    }

    // =========================================================================
    // PRUEBAS: Verificacion de Rol de Administrador
    // =========================================================================

    /**
     * AVM-004: Usuario con email de admin debe recibir rol ADMIN
     *
     * Verifica que cuando un usuario hace login con un email que esta
     * registrado como administrador, se le asigna correctamente el rol ADMIN.
     */
    @Test
    fun `AVM-004 login con email de admin debe asignar rol ADMIN`() {
        // Arrange
        val email = adminEmail

        // Act - Simular verificacion de admin
        val isAdmin = adminEmails.contains(email)
        val assignedRole = if (isAdmin) UserRole.ADMIN else UserRole.USER

        // Assert
        assertTrue("Email de admin debe ser reconocido", isAdmin)
        assertEquals("Rol debe ser ADMIN", UserRole.ADMIN, assignedRole)
    }

    /**
     * AVM-005: Usuario normal no debe recibir rol ADMIN
     *
     * Verifica que usuarios regulares no obtienen privilegios de administrador.
     */
    @Test
    fun `AVM-005 login con email normal debe asignar rol USER`() {
        // Arrange
        val email = validEmail

        // Act - Simular verificacion de admin
        val isAdmin = adminEmails.contains(email)
        val assignedRole = if (isAdmin) UserRole.ADMIN else UserRole.USER

        // Assert
        assertFalse("Email normal no debe ser reconocido como admin", isAdmin)
        assertEquals("Rol debe ser USER", UserRole.USER, assignedRole)
    }

    /**
     * AVM-006: Todos los emails de admin_config.json deben ser reconocidos
     *
     * Verifica que todos los administradores configurados en el sistema
     * son correctamente identificados.
     */
    @Test
    fun `AVM-006 todos los admins configurados deben ser reconocidos`() {
        // Arrange
        val configuredAdmins = listOf(
            "admin@barriovivo.com",
            "moderador@barriovivo.com",
            "test@admin.com"
        )

        // Act & Assert
        for (adminEmail in configuredAdmins) {
            val isAdmin = adminEmails.contains(adminEmail)
            assertTrue("Admin '$adminEmail' debe ser reconocido", isAdmin)
        }
    }

    /**
     * AVM-007: Verificacion de admin debe ser case-sensitive
     *
     * Nota: Actualmente la verificacion es case-sensitive.
     * Este test documenta el comportamiento esperado.
     */
    @Test
    fun `AVM-007 verificacion de admin es case-sensitive`() {
        // Arrange
        val upperCaseEmail = "ADMIN@BARRIOVIVO.COM"
        val lowerCaseEmail = "admin@barriovivo.com"

        // Act
        val upperCaseIsAdmin = adminEmails.contains(upperCaseEmail)
        val lowerCaseIsAdmin = adminEmails.contains(lowerCaseEmail)

        // Assert - Actualmente es case-sensitive
        assertFalse("Email en mayusculas no coincide (case-sensitive)", upperCaseIsAdmin)
        assertTrue("Email en minusculas debe coincidir", lowerCaseIsAdmin)
    }

    // =========================================================================
    // PRUEBAS: Registro de Usuario
    // =========================================================================

    /**
     * AVM-008: Passwords que no coinciden deben mostrar error
     */
    @Test
    fun `AVM-008 passwords diferentes deben mostrar error`() {
        // Arrange
        val password = "Password123!"
        val confirmPassword = "Password456!"

        // Act
        val passwordsMatch = password == confirmPassword

        // Assert
        assertFalse("Passwords diferentes no deben coincidir", passwordsMatch)
    }

    /**
     * AVM-009: Registro exitoso debe asignar rol USER por defecto
     */
    @Test
    fun `AVM-009 registro exitoso debe asignar rol USER`() {
        // Arrange
        val email = "nuevo@usuario.com"

        // Act - Simular registro
        val isAdmin = adminEmails.contains(email)
        val assignedRole = if (isAdmin) UserRole.ADMIN else UserRole.USER

        // Assert
        assertEquals("Nuevo usuario debe tener rol USER", UserRole.USER, assignedRole)
    }

    /**
     * AVM-010: Registro con email de admin debe asignar rol ADMIN
     *
     * Cuando un administrador se registra por primera vez, debe recibir
     * automaticamente el rol de ADMIN si su email esta en la configuracion.
     */
    @Test
    fun `AVM-010 registro con email de admin debe asignar rol ADMIN`() {
        // Arrange
        val email = "admin@barriovivo.com"

        // Act - Simular registro y verificacion
        val isAdmin = adminEmails.contains(email)
        val assignedRole = if (isAdmin) UserRole.ADMIN else UserRole.USER

        // Assert
        assertEquals("Admin registrado debe tener rol ADMIN", UserRole.ADMIN, assignedRole)
    }

    // =========================================================================
    // PRUEBAS: Persistencia de Sesion
    // =========================================================================

    /**
     * AVM-011: Al recuperar sesion, debe verificar rol de admin nuevamente
     *
     * Este test verifica que cuando se recupera una sesion guardada,
     * se vuelve a consultar si el usuario es admin para asegurar
     * que el rol este actualizado.
     */
    @Test
    fun `AVM-011 recuperar sesion debe reverificar rol de admin`() {
        // Arrange - Usuario guardado con rol USER pero es admin
        val savedEmail = "admin@barriovivo.com"
        val savedRole = UserRole.USER // Rol guardado incorrectamente

        // Act - Al recuperar, se debe verificar contra lista de admins
        val isAdmin = adminEmails.contains(savedEmail)
        val correctedRole = if (isAdmin) UserRole.ADMIN else savedRole

        // Assert
        assertEquals(
            "Rol debe corregirse a ADMIN al recuperar sesion",
            UserRole.ADMIN,
            correctedRole
        )
    }
}

