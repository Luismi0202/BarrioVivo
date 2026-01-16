package com.example.barriovivo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
import androidx.compose.material.icons.automirrored.filled.Logout

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(title = { Text("BarrioVivo") }, actions = {
                IconButton(onClick = onNotificationsClick) { Icon(Icons.Default.Notifications, contentDescription = null) }
                IconButton(onClick = onProfileClick) { Icon(Icons.Default.Person, contentDescription = null) }
                IconButton(onClick = onLogoutClick) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
            })
        },
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
            if (isLoading) {
                LoadingScreen()
            } else {
                when (selectedTab) {
                    0 -> {
                        if (nearbyMealPosts.isEmpty()) {
                            EmptyStateScreen("No hay comidas cerca de ti aún")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(8.dp)) {
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
                    1 -> {
                        if (userMealPosts.isEmpty()) {
                            EmptyStateScreen("Aún no has publicado comidas")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(8.dp)) {
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
}
