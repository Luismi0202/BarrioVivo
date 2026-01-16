package com.example.barriovivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barriovivo.ui.screen.AuthScreen
import com.example.barriovivo.ui.screen.CreateMealScreen
import com.example.barriovivo.ui.screen.HomeScreen
import com.example.barriovivo.ui.screen.NotificationScreen
import com.example.barriovivo.ui.screen.ProfileScreen
import com.example.barriovivo.ui.theme.BarrioVivoTheme
import com.example.barriovivo.ui.viewmodel.AuthViewModel
import com.example.barriovivo.ui.viewmodel.HomeViewModel
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
                ) {
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
                        modifier = Modifier.fillMaxSize()
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

                            // Cargar datos solo cuando haya usuario y una vez
                            LaunchedEffect(authState.currentUser) {
                                authState.currentUser?.let { user ->
                                    homeViewModel.loadUserMealPosts(user.id)
                                    val lat = user.location.latitude
                                    val lon = user.location.longitude
                                    if (lat != 0.0 || lon != 0.0) {
                                        homeViewModel.loadNearbyMealPosts(lat, lon)
                                    }
                                }
                            }

                            HomeScreen(
                                nearbyMealPosts = homeState.nearbyMealPosts,
                                userMealPosts = homeState.userMealPosts,
                                isLoading = homeState.isLoading,
                                onCreateMealClick = { navController.navigate("create_meal") },
                                onMealClick = { mealId ->
                                    navController.navigate("meal_detail/$mealId")
                                },
                                onNotificationsClick = { navController.navigate("notifications") },
                                onProfileClick = { navController.navigate("profile") },
                                onLogoutClick = {
                                    authViewModel.logout()
                                    // Navegar a auth limpiando el backstack
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
                            CreateMealScreen(onClose = { navController.popBackStack() })
                        }

                        composable("meal_detail/{mealId}") {
                            // Placeholder para detalle de comida
                        }

                        composable("notifications") {
                            NotificationScreen(onBack = { navController.popBackStack() })
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

                        composable("admin_dashboard") {
                            val adminViewModel: com.example.barriovivo.ui.viewmodel.AdminViewModel = hiltViewModel()
                            val adminState by adminViewModel.uiState.collectAsState()

                            com.example.barriovivo.ui.screen.AdminDashboardScreen(
                                pendingPosts = adminState.mealPosts,
                                isLoading = adminState.isLoading,
                                error = adminState.error,
                                onBack = { navController.popBackStack() },
                                onApprove = { postId ->
                                    adminViewModel.approveMealPost(postId)
                                },
                                onReject = { postId, reason ->
                                    adminViewModel.deleteMealPost(postId, reason)
                                },
                                onErrorDismiss = { adminViewModel.clearError() },
                                onSuccessDismiss = { adminViewModel.clearSuccess() }
                            )

                            // Cargar todos los posts para revisión
                            LaunchedEffect(Unit) {
                                adminViewModel.loadAllMealPosts()
                            }
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
