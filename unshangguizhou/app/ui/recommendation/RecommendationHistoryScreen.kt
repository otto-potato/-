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

data class HistoryState(val list: List<RecommendationEntity> = emptyList(), val loading: Boolean = false)

@HiltViewModel
class HistoryVM @Inject constructor(private val repo: RecommendationRepository) : ViewModel() {
    private val _s = MutableStateFlow(HistoryState()); val state: StateFlow<HistoryState> = _s.asStateFlow()
    init { viewModelScope.launch { _s.update { it.copy(loading = true) }; try { _s.update { it.copy(list = repo.getRecent(50), loading = false) } } catch (_: Exception) { _s.update { it.copy(loading = false) } } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationHistoryScreen(onBack: () -> Unit, vm: HistoryVM = hiltViewModel()) {
    val s by vm.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("推荐历史", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (s.loading) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (s.list.isEmpty()) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(20.dp), color = PrimaryContainer.copy(alpha = 0.3f), modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.History, "历史", Modifier.size(36.dp), tint = Primary.copy(alpha = 0.5f)) }
                }
                Spacer(Modifier.height(16.dp)); Text("暂无推荐记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp)); Text("AI推荐后将在这里显示", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        } else LazyColumn(modifier = Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(s.list) { r ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(r.date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryContainer.copy(alpha = 0.4f)) {
                                Text("${r.weatherDesc} ${r.temperature}", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(r.reasoning, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, maxLines = 4)
                    }
                }
            }
        }
    }
}
