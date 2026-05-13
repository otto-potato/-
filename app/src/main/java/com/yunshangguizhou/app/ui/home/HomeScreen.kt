package com.yunshangguizhou.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.remote.WeatherInfo
import com.yunshangguizhou.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWardrobe: () -> Unit,
    onNavigateToAddClothing: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("云上柜周", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) { Icon(Icons.Outlined.History, "历史") }
                    IconButton(onClick = onNavigateToSettings) {
                        BadgedBox(badge = { if (!uiState.aiConfigured) Badge { Text("!") } }) {
                            Icon(Icons.Outlined.Settings, "设置")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddClothing,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("添加衣物") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI config warning
            if (!uiState.aiConfigured && !uiState.isLoading) item {
                Card(colors = CardDefaults.cardColors(containerColor = WarningOrangeBg), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WarningAmber, null, tint = WarningOrange)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("AI未配置", fontWeight = FontWeight.SemiBold, color = WarningOrange)
                            Text("前往设置配置后启用", style = MaterialTheme.typography.bodySmall, color = WarningOrange)
                        }
                        FilledTonalButton(onClick = onNavigateToSettings,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = WarningOrange)) {
                            Text("配置", color = Color.White)
                        }
                    }
                }
            }

            // Weather
            item { WeatherBanner(weather = uiState.todayWeather, weatherError = uiState.weatherError) }

            // Recommendation header
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("今日推荐", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    FilledTonalButton(
                        onClick = { viewModel.generateRecommendation() },
                        enabled = !uiState.isGenerating && uiState.aiConfigured && uiState.clothingCount > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isGenerating) { CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                        Icon(Icons.Filled.AutoAwesome, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI推荐", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            val rec = uiState.todayRecommendation
            if (rec != null) {
                item { RecommendationCard(rec, uiState.recommendedClothes, onNavigateToDetail) }
            } else if (!uiState.isGenerating && !uiState.isLoading) {
                item { EmptyRecommendation(onNavigateToAddClothing, uiState.clothingCount) }
            }

            uiState.error?.let { err -> item {
                Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(err, Modifier.padding(12.dp), color = OnErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }}

            // Quick actions
            item { Text("快捷入口", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashCard(Icons.Outlined.Checkroom, "衣柜", "${uiState.clothingCount}件", onNavigateToWardrobe, Modifier.weight(1f))
                    DashCard(Icons.Outlined.AddCircleOutline, "录入", "添加衣物", onNavigateToAddClothing, Modifier.weight(1f))
                    DashCard(Icons.Outlined.History, "历史", "推荐记录", onNavigateToHistory, Modifier.weight(1f))
                }
            }

            // Empty wardrobe
            if (uiState.clothingCount == 0 && !uiState.isLoading) item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.3f))) {
                    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = PrimaryContainer, modifier = Modifier.size(72.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Checkroom, null, Modifier.size(36.dp), tint = Primary) }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("开始构建你的云衣柜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("AI会分析每件衣物并为你\n智能推荐每日穿搭", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = OnSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onNavigateToAddClothing, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("添加第一件衣物")
                        }
                    }
                }
            }
        }
    }
}

// ====== Weather Banner ======
@Composable
fun WeatherBanner(weather: WeatherInfo?, weatherError: Boolean) {
    val weatherColor = if (weather != null) weatherColor(weather.weatherCode) else Primary
    Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(weatherColor.copy(alpha = 0.12f), weatherColor.copy(alpha = 0.03f))))) {
            Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = weatherColor.copy(alpha = 0.1f), modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(weatherIcon(weather?.weatherCode), null, Modifier.size(32.dp), tint = weatherColor)
                    }
                }
                Spacer(Modifier.width(16.dp))
                if (weather != null) {
                    Column(Modifier.weight(1f)) {
                        Text(weather.weatherDesc, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row {
                            InfoChip("湿度 ${weather.humidity}%"); Spacer(Modifier.width(8.dp)); InfoChip("风力 ${weather.windSpeed.toInt()}km/h")
                        }
                    }
                    Column {
                        Text("${weather.maxTemp.toInt()}°", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("${weather.minTemp.toInt()}°", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                } else if (weatherError) {
                    Column(Modifier.weight(1f)) { Text("天气获取失败", style = MaterialTheme.typography.titleMedium); Text("请检查网络和配置", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) }
                } else {
                    Column(Modifier.weight(1f)) { Text("加载中...", style = MaterialTheme.typography.titleMedium) }
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = weatherColor)
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

// ====== Recommendation Card ======
@Composable
fun RecommendationCard(rec: RecommendationEntity, clothes: List<ClothingEntity>, onDetail: (Long) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(20.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryContainer.copy(alpha = 0.5f)) {
                Text("${rec.weatherDesc}  ·  ${rec.temperature}", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = OnPrimaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            Text(rec.reasoning, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, maxLines = 6, overflow = TextOverflow.Ellipsis)
            if (clothes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Divider(color = OutlineVariant)
                Spacer(Modifier.height(12.dp))
                Text("推荐搭配", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(clothes) { c -> OutfitChip(c, onDetail) } }
            }
        }
    }
}

@Composable
fun OutfitChip(clothing: ClothingEntity, onClick: (Long) -> Unit) {
    Surface(Modifier.width(130.dp).clickable { onClick(clothing.id) }, shape = RoundedCornerShape(14.dp), color = SurfaceVariant.copy(alpha = 0.5f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Checkroom, null, Modifier.size(24.dp), tint = Primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(clothing.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text(clothing.category, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            if (clothing.isWashing) { Surface(color = WashRedBg, shape = RoundedCornerShape(4.dp)) { Text("清洗中", Modifier.padding(horizontal = 6.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = WashRed) } }
            if (clothing.consecutiveWearDays >= 2) { Text("连穿${clothing.consecutiveWearDays}天", style = MaterialTheme.typography.labelSmall, color = Tertiary) }
        }
    }
}

// ====== Empty Rec ======
@Composable
fun EmptyRecommendation(onAdd: () -> Unit, count: Int) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))) {
        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = PrimaryContainer.copy(alpha = 0.4f), modifier = Modifier.size(60.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Style, null, Modifier.size(30.dp), tint = Primary.copy(alpha = 0.5f)) }
            }
            Spacer(Modifier.height(16.dp))
            Text("暂无今日推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(if (count == 0) "先添加衣物再获取AI推荐" else "点击 AI推荐 获取今日穿搭", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = OnSurfaceVariant)
        }
    }
}

// ====== Dashboard ======
@Composable
fun DashCard(icon: ImageVector, title: String, sub: String, onClick: () -> Unit, mod: Modifier) {
    Card(mod.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.4f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(28.dp), tint = Primary); Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

// ====== Weather Helpers ======
fun weatherIcon(code: Int?): ImageVector = when (code) {
    in 0..1 -> Icons.Filled.WbSunny
    2 -> Icons.Filled.WbCloudy
    3 -> Icons.Filled.Cloud
    in 45..48 -> Icons.Filled.Cloud
    in 51..57, in 61..67, in 80..82 -> Icons.Filled.Water
    in 71..77, in 85..86 -> Icons.Filled.AcUnit
    in 95..99 -> Icons.Filled.Thunderstorm
    else -> Icons.Filled.WbSunny
}

fun weatherColor(code: Int?): Color = when (code) {
    in 0..1 -> SunnyColor
    2, 3 -> CloudyColor
    in 45..48 -> FogColor
    in 51..57, in 61..67, in 80..82 -> RainColor
    in 71..77, in 85..86 -> SnowColor
    in 95..99 -> StormColor
    else -> Primary
}
