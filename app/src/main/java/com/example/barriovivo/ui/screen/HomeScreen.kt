package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.ui.component.EmptyStateScreen
import com.example.barriovivo.ui.component.LoadingScreen
import com.example.barriovivo.ui.component.MealCard
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.OrangePrimary

@Composable
fun HomeScreen(
    nearbyMealPosts: List<MealPost> = emptyList(),
    userMealPosts: List<MealPost> = emptyList(),
    isLoading: Boolean = false,
    onCreateMealClick: () -> Unit = {},
    onMealClick: (mealId: String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cerca de ti", "Mis Comidas")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateMealClick,
                containerColor = OrangePrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear comida"
                )
            }
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones"
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen()
                    }
                }
                selectedTab == 0 -> {
                    if (nearbyMealPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateScreen("No hay comidas cercanas disponibles")
                        }
                    } else {
                        LazyColumn {
                            items(nearbyMealPosts) { meal ->
                                MealCard(
                                    title = meal.title,
                                    location = meal.location.city,
                                    expiryDate = meal.expiryDate.toString(),
                                    onClick = { onMealClick(meal.id) }
                                )
                            }
                        }
                    }
                }
                selectedTab == 1 -> {
                    if (userMealPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateScreen("No has publicado comidas aún")
                        }
                    } else {
                        LazyColumn {
                            items(userMealPosts) { meal ->
                                MealCard(
                                    title = meal.title,
                                    location = meal.location.city,
                                    expiryDate = meal.expiryDate.toString(),
                                    onClick = { onMealClick(meal.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

