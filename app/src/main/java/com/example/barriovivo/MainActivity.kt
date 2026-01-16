package com.example.barriovivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.barriovivo.ui.screen.AuthScreen
import com.example.barriovivo.ui.screen.ChatConversationScreen
import com.example.barriovivo.ui.screen.ChatListScreen
import com.example.barriovivo.ui.screen.CreateMealScreen
import com.example.barriovivo.ui.screen.HomeScreen
import com.example.barriovivo.ui.screen.MealDetailScreen
import com.example.barriovivo.ui.screen.NotificationScreen
import com.example.barriovivo.ui.screen.ProfileScreen
import com.example.barriovivo.ui.theme.BarrioVivoTheme
import com.example.barriovivo.ui.viewmodel.AuthViewModel
import com.example.barriovivo.ui.viewmodel.ChatViewModel
import com.example.barriovivo.ui.viewmodel.HomeViewModel
import com.example.barriovivo.ui.viewmodel.MealDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

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

                androidx.compose.material3.Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (authState.isLoggedIn) {
                            if (authState.userRole == com.example.barriovivo.domain.model.UserRole.ADMIN) {
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

                            // Navegación controlada por efecto según el rol
                            LaunchedEffect(authState.isLoggedIn, authState.userRole) {
                                if (authState.isLoggedIn) {
                                    val destination = if (authState.userRole == com.example.barriovivo.domain.model.UserRole.ADMIN) {
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
                            val notificationViewModel: com.example.barriovivo.ui.viewmodel.NotificationViewModel = hiltViewModel()
                            val notificationState by notificationViewModel.uiState.collectAsState()

                            // Observar el resultado de la pantalla de creación
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
                            LaunchedEffect(authState.currentUser) {
                                authState.currentUser?.let { user ->
                                    homeViewModel.loadUserMealPosts(user.id)
                                    // Usar ubicación del usuario o Madrid por defecto
                                    val lat = if (user.location.latitude != 0.0) user.location.latitude else 40.4168
                                    val lon = if (user.location.longitude != 0.0) user.location.longitude else -3.7038
                                    homeViewModel.loadNearbyMealPosts(lat, lon)
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
                                    navController.navigate("chat_conversation/$conversationId")
                                },
                                onReportClick = { reason ->
                                    authState.currentUser?.let { user ->
                                        mealDetailViewModel.reportMealPost(mealId, user.id, reason)
                                    }
                                }
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
                            com.example.barriovivo.ui.screen.AdminDashboardScreen(
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
