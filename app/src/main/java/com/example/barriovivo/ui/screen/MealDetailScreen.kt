package com.example.barriovivo.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.barriovivo.domain.model.MealPost
import com.example.barriovivo.ui.component.BarrioVivoButton
import com.example.barriovivo.ui.theme.GreenPrimary
import com.example.barriovivo.ui.theme.TextDark
import com.example.barriovivo.ui.theme.TextGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MealDetailScreen(
    mealPost: MealPost? = null,
    onBack: () -> Unit = {},
    onClaimClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de la Comida") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (mealPost != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = mealPost.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextDark
                )
                Text(
                    text = mealPost.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ubicación: ${mealPost.location.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )

                Text(
                    text = "Caduca: ${mealPost.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (mealPost.photoUris.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { mealPost.photoUris.size })
                    val scope = rememberCoroutineScope()

                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(240.dp)) { page ->
                        AsyncImage(model = mealPost.photoUris[page], contentDescription = "Foto $page")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(text = "${pagerState.currentPage + 1} / ${mealPost.photoUris.size}")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                BarrioVivoButton(
                    text = "Reclamar esta Comida",
                    onClick = onClaimClick,
                    isLoading = isLoading
                )
            }
        } else {
            Text("Comida no encontrada")
        }
    }
}
