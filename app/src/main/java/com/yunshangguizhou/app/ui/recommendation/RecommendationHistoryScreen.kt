package com.yunshangguizhou.app.ui.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.repository.RecommendationRepository
import com.yunshangguizhou.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val recommendations: List<RecommendationEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RecommendationHistoryViewModel @Inject constructor(
    private val repo: RecommendationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val list = repo.getRecentRecommendations(30)
                _state.update { it.copy(recommendations = list, isLoading = false) }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationHistoryScreen(onBack: () -> Unit, viewModel: RecommendationHistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("推荐历史", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.recommendations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = RoundedCornerShape(20.dp), color = PrimaryContainer.copy(alpha = 0.3f), modifier = Modifier.size(72.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.History, null, Modifier.size(36.dp), tint = Primary.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("暂无推荐记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("AI推荐后将会在这里显示", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.recommendations) { rec -> HistoryCard(rec) }
            }
        }
    }
}

@Composable
fun HistoryCard(rec: RecommendationEntity) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(rec.date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = PrimaryContainer.copy(alpha = 0.4f)) {
                    Text("${rec.weatherDesc}  ${rec.temperature}",
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(rec.reasoning, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight, maxLines = 4)
        }
    }
}
