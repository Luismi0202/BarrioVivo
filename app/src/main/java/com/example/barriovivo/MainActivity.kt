package com.example.barriovivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.barriovivo.domain.model.UserRole
import com.example.barriovivo.ui.screen.*
import com.example.barriovivo.ui.theme.BarrioVivoTheme
import com.example.barriovivo.ui.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de la aplicacion BarrioVivo.
 *
 * Implementa navegacion con Jetpack Compose Navigation.
 * Las rutas principales son:
 * - auth: Pantalla de login/registro
 * - home: Feed principal de publicaciones
 * - create_meal: Crear nueva publicacion
 * - meal_detail/{id}: Detalle de publicacion
 * - chat_list: Lista de conversaciones
 * - chat/{id}: Conversacion individual
 * - notifications: Centro de notificaciones
 * - profile: Perfil del usuario
 * - admin_dashboard: Panel de administracion (solo admins)
 *
 * La navegacion inicial depende del estado de autenticacion:
 * - Usuario no logueado: auth
 * - Usuario normal logueado: home
 * - Administrador logueado: admin_dashboard
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarrioVivoTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (authState.isLoggedIn) {
                            if (authState.userRole == UserRole.ADMIN) {
                                "admin_dashboard"
                            } else {
                                "home"
                            }
                        } else {
                            "auth"
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("auth") {
                            AuthScreen(
                                isLoading = authState.isLoading,
                                error = authState.error,
                                onLoginClick = { email, password ->
                                    authViewModel.login(email, password)
                                },
                                onRegisterClick = { email, password, confirmPassword, city, latitude, longitude, zipCode ->
                                    authViewModel.register(email, password, confirmPassword, city, latitude, longitude, zipCode)
                                },
                                onErrorDismiss = { authViewModel.clearError() }
                            )

                            // Navegacion controlada por efecto segun el rol
                            LaunchedEffect(authState.isLoggedIn, authState.userRole) {
                                if (authState.isLoggedIn) {
                                    val destination = if (authState.userRole == UserRole.ADMIN) {
                                        "admin_dashboard"
                                    } else {
                                        "home"
                                    }
                                    navController.navigate(destination) {
                                        popUpTo("auth") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        composable("home") {
                            val homeViewModel: HomeViewModel = hiltViewModel()
                            val homeState by homeViewModel.uiState.collectAsState()
                            val chatViewModel: ChatViewModel = hiltViewModel()
                            val totalUnreadCount by chatViewModel.totalUnreadCount.collectAsState()
                            val notificationViewModel: NotificationViewModel = hiltViewModel()
                            val notificationState by notificationViewModel.uiState.collectAsState()

                            // Observar el resultado de la pantalla de creacion
                            val newPostCreatedState = navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.getLiveData<Boolean>("new_post_created")
                                ?.observeAsState()

                            LaunchedEffect(newPostCreatedState?.value) {
                                if (newPostCreatedState?.value == true) {
                                    homeViewModel.refresh()
                                    // Limpiar el estado para no refrescar de nuevo
                                    navController.currentBackStackEntry?.savedStateHandle?.set("new_post_created", false)
                                }
                            }

                            // Cargar datos solo cuando haya usuario y una vez
                            LaunchedEffect(authState.currentUser, authState.userRole) {
                                authState.currentUser?.let { user ->
                                    // Verificar si es administrador
                                    val isAdmin = authState.userRole == UserRole.ADMIN
                                    homeViewModel.setIsAdmin(isAdmin)

                                    homeViewModel.loadUserMealPosts(user.id)

                                    if (isAdmin) {
                                        // Administradores ven TODAS las comidas sin filtro de ubicación
                                        homeViewModel.loadAllMealPosts()
                                    } else {
                                        // Usuarios normales ven solo comidas cercanas
                                        val lat = if (user.location.latitude != 0.0) user.location.latitude else 40.4168
                                        val lon = if (user.location.longitude != 0.0) user.location.longitude else -3.7038
                                        homeViewModel.loadNearbyMealPosts(lat, lon)
                                    }
                                    // Cargar notificaciones no leídas
                                    notificationViewModel.loadUnreadNotifications(user.id)
                                }
                            }

                            HomeScreen(
                                nearbyMealPosts = homeState.nearbyMealPosts,
                                userMealPosts = homeState.userMealPosts,
                                isLoading = homeState.isLoading,
                                unreadChatCount = totalUnreadCount,
                                unreadNotificationCount = notificationState.unreadCount,
                                isAdmin = homeState.isAdmin,
                                onCreateMealClick = { navController.navigate("create_meal") },
                                onMealClick = { mealId ->
                                    navController.navigate("meal_detail/$mealId")
                                },
                                onNotificationsClick = { navController.navigate("notifications") },
                                onProfileClick = { navController.navigate("profile") },
                                onChatClick = { navController.navigate("chat_list") },
                                onLogoutClick = {
                                    authViewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )

                            // Si el estado cambia a deslogueado, garantizar regreso a auth
                            LaunchedEffect(authState.isLoggedIn) {
                                if (!authState.isLoggedIn) {
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        composable("create_meal") {
                            CreateMealScreen(onClose = {
                                // Indicar que se creó un nuevo post
                                navController.previousBackStackEntry?.savedStateHandle?.set("new_post_created", true)
                                navController.popBackStack()
                            })
                        }

                        composable(
                            route = "meal_detail/{mealId}",
                            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
                            val mealDetailViewModel: MealDetailViewModel = hiltViewModel()
                            val mealDetailState by mealDetailViewModel.uiState.collectAsState()

                            LaunchedEffect(mealId) {
                                mealDetailViewModel.loadMealPost(mealId)
                            }

                            MealDetailScreen(
                                mealPost = mealDetailState.mealPost,
                                isLoading = mealDetailState.isLoading,
                                isClaimLoading = mealDetailState.isClaimLoading,
                                claimSuccess = mealDetailState.claimSuccess,
                                conversationId = mealDetailState.conversationId,
                                error = mealDetailState.error,
                                currentUserId = authState.currentUser?.id ?: "",
                                isReportLoading = mealDetailState.isReportLoading,
                                reportSuccess = mealDetailState.reportSuccess,
                                onBack = { navController.popBackStack() },
                                onClaimClick = {
                                    authState.currentUser?.let { user ->
                                        mealDetailViewModel.claimMealPost(mealId, user.id, user.email)
                                    }
                                },
                                onGoToChat = { conversationId ->
                                    mealDetailViewModel.clearClaimSuccess()
                                    navController.navigate("chat_conversation/$conversationId")
                                },
                                onReportClick = { reason ->
                                    authState.currentUser?.let { user ->
                                        mealDetailViewModel.reportMealPost(mealId, user.id, reason)
                                    }
                                },
                                onResetClaimSuccess = { mealDetailViewModel.clearClaimSuccess() },
                                onResetReportSuccess = { mealDetailViewModel.clearReportSuccess() }
                            )
                        }

                        composable("notifications") {
                            NotificationScreen(
                                onBack = { navController.popBackStack() },
                                onNotificationClick = { mealId ->
                                    navController.navigate("meal_detail/$mealId")
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() }, onLogout = {
                                authViewModel.logout()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            })
                        }

                        // Chat List Screen
                        composable("chat_list") {
                            ChatListScreen(
                                onBack = { navController.popBackStack() },
                                onConversationClick = { conversationId ->
                                    navController.navigate("chat_conversation/$conversationId")
                                }
                            )
                        }

                        // Chat Conversation Screen
                        composable(
                            route = "chat_conversation/{conversationId}",
                            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                            ChatConversationScreen(
                                conversationId = conversationId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_dashboard") {
                            AdminDashboardScreen(
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo("admin_dashboard") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onProfileClick = { navController.navigate("admin_profile") },
                                onNotificationsClick = { navController.navigate("admin_notifications") }
                            )
                        }

                        // Admin Notifications
                        composable("admin_notifications") {
                            NotificationScreen(
                                onBack = { navController.popBackStack() },
                                onNotificationClick = { mealId ->
                                    navController.navigate("meal_detail/$mealId")
                                }
                            )
                        }

                        // Admin Profile Screen
                        composable("admin_profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo("admin_dashboard") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    // Mostrar mensajes de éxito
                    LaunchedEffect(authState.successMessage) {
                        val msg = authState.successMessage
                        if (msg != null) {
                            snackbarHostState.showSnackbar(msg)
                            authViewModel.consumeSuccessMessage()
                        }
                    }

                    // Mostrar errores
                    LaunchedEffect(authState.error) {
                        val err = authState.error
                        if (!err.isNullOrEmpty()) {
                            snackbarHostState.showSnackbar(err)
                        }
                    }
                }
            }
        }
    }
}
