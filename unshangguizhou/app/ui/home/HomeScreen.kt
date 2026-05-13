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
    onWardrobe: () -> Unit, onAdd: () -> Unit, onHistory: () -> Unit,
    onSettings: () -> Unit, onDebug: () -> Unit, onDetail: (Long) -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("云上柜周", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onHistory) { Icon(Icons.Outlined.History, "历史推荐") }
                    IconButton(onClick = onDebug) { Icon(Icons.Default.BugReport, "调试") }
                    IconButton(onClick = onSettings) {
                        BadgedBox(badge = { if (!s.aiReady) Badge { Text("!") } }) {
                            Icon(Icons.Outlined.Settings, "设置")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd, icon = { Icon(Icons.Default.Add, "添加") },
                text = { Text("添加衣物") },
                containerColor = Primary, contentColor = OnPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!s.aiReady && !s.loading) item {
                Card(colors = CardDefaults.cardColors(containerColor = WarningOrangeBg), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WarningAmber, "警告", tint = WarningOrange)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("AI未配置", fontWeight = FontWeight.SemiBold, color = WarningOrange)
                            Text("前往设置配置后启用AI", style = MaterialTheme.typography.bodySmall, color = WarningOrange)
                        }
                        FilledTonalButton(onClick = onSettings, colors = ButtonDefaults.filledTonalButtonColors(containerColor = WarningOrange)) {
                            Text("配置", color = Color.White)
                        }
                    }
                }
            }

            item { WeatherBanner(s.weather, s.weatherErr) }

            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("今日推荐", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    FilledTonalButton(
                        onClick = { vm.generate() }, shape = RoundedCornerShape(12.dp),
                        enabled = !s.generating && s.aiReady && s.count > 0
                    ) {
                        if (s.generating) { CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                        Icon(Icons.Filled.AutoAwesome, "AI推荐", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("AI推荐", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            val rec = s.recommendation
            if (rec != null) item { RecCard(rec, s.recClothes, onDetail) }
            else if (!s.generating && !s.loading) item { EmptyRec(onAdd, s.count) }

            s.error?.let { item {
                Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(it, Modifier.padding(12.dp), color = OnErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }}

            item { Text("快捷入口", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashCard(Icons.Outlined.Checkroom, "衣柜", "${s.count}件", onWardrobe, Modifier.weight(1f))
                    DashCard(Icons.Outlined.AddCircleOutline, "录入", "添加", onAdd, Modifier.weight(1f))
                    DashCard(Icons.Outlined.History, "历史", "记录", onHistory, Modifier.weight(1f))
                }
            }

            if (s.count == 0 && !s.loading) item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.3f))) {
                    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = PrimaryContainer, modifier = Modifier.size(72.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Checkroom, "衣柜", Modifier.size(36.dp), tint = Primary) }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("开始构建云衣柜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("AI会分析每件衣物并智能推荐每日穿搭", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = OnSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onAdd, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Add, "添加", Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("添加第一件衣物")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherBanner(wx: WeatherInfo?, err: Boolean) {
    val c = if (wx != null) weatherColor(wx.weatherCode) else Primary
    Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(c.copy(alpha = 0.12f), c.copy(alpha = 0.03f))))) {
            Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = c.copy(alpha = 0.1f), modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(weatherIcon(wx?.weatherCode), "天气图标", Modifier.size(32.dp), tint = c)
                    }
                }
                Spacer(Modifier.width(16.dp))
                if (wx != null) {
                    Column(Modifier.weight(1f)) {
                        Text(wx.weatherDesc, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp)); Row { Chip("湿度${wx.humidity}%"); Spacer(Modifier.width(8.dp)); Chip("风力${wx.windSpeed.toInt()}km/h") }
                    }
                    Column {
                        Text("${wx.maxTemp.toInt()}°", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("${wx.minTemp.toInt()}°", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                } else if (err) {
                    Column(Modifier.weight(1f)) { Text("天气获取失败", style = MaterialTheme.typography.titleMedium); Text("请检查网络", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) }
                } else {
                    Column(Modifier.weight(1f)) { Text("加载中...", style = MaterialTheme.typography.titleMedium) }
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = c)
                }
            }
        }
    }
}

@Composable fun Chip(t: String) { Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) { Text(t, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant) } }

@Composable
fun RecCard(rec: RecommendationEntity, clothes: List<ClothingEntity>, onDetail: (Long) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(20.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryContainer.copy(alpha = 0.5f)) {
                Text("${rec.weatherDesc}  ·  ${rec.temperature}", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = OnPrimaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            Text(rec.reasoning, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, maxLines = 6, overflow = TextOverflow.Ellipsis)
            if (clothes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp)); Divider(color = OutlineVariant); Spacer(Modifier.height(12.dp))
                Text("推荐搭配", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(clothes) { c -> OutfitChip(c, onDetail) } }
            }
        }
    }
}

@Composable
fun OutfitChip(c: ClothingEntity, onClick: (Long) -> Unit) {
    Surface(Modifier.width(130.dp).clickable { onClick(c.id) }, shape = RoundedCornerShape(14.dp), color = SurfaceVariant.copy(alpha = 0.5f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Checkroom, c.name, Modifier.size(24.dp), tint = Primary) }
            Spacer(Modifier.height(8.dp)); Text(c.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text(c.category, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            if (c.isWashing) { Surface(color = WashRedBg, shape = RoundedCornerShape(4.dp)) { Text("清洗中", Modifier.padding(horizontal = 6.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = WashRed) } }
            if (c.consecutiveWearDays >= 2) { Text("连穿${c.consecutiveWearDays}天", style = MaterialTheme.typography.labelSmall, color = Tertiary) }
        }
    }
}

@Composable
fun EmptyRec(onAdd: () -> Unit, count: Int) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))) {
        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = PrimaryContainer.copy(alpha = 0.4f), modifier = Modifier.size(60.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Style, "推荐", Modifier.size(30.dp), tint = Primary.copy(alpha = 0.5f)) } }
            Spacer(Modifier.height(16.dp)); Text("暂无今日推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp)); Text(if (count == 0) "先添加衣物再获取AI推荐" else "点击 AI推荐 获取今日穿搭", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun DashCard(icon: ImageVector, title: String, sub: String, onClick: () -> Unit, mod: Modifier) {
    Card(mod.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.4f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, title, Modifier.size(28.dp), tint = Primary); Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium); Text(sub, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

fun weatherIcon(code: Int?): ImageVector = when (code) {
    in 0..1 -> Icons.Filled.WbSunny; 2 -> Icons.Filled.WbCloudy; 3 -> Icons.Filled.Cloud
    in 45..48 -> Icons.Filled.Cloud; in 51..57, in 61..67, in 80..82 -> Icons.Filled.Water
    in 71..77, in 85..86 -> Icons.Filled.AcUnit; in 95..99 -> Icons.Filled.Thunderstorm; else -> Icons.Filled.WbSunny
}

fun weatherColor(code: Int?): Color = when (code) {
    in 0..1 -> SunnyColor; 2, 3 -> CloudyColor; in 45..48 -> FogColor
    in 51..57, in 61..67, in 80..82 -> RainColor; in 71..77, in 85..86 -> SnowColor
    in 95..99 -> StormColor; else -> Primary
}
