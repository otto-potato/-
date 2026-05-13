package com.yunshangguizhou.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yunshangguizhou.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingDetailScreen(
    clothingId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ClothingDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(clothingId) { viewModel.loadClothing(clothingId) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这件衣物吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteClothing(); showDeleteDialog = false; onDeleted() }) {
                    Text("删除", color = Error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("衣物详情", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "删除", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val clothing = state.clothing
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (clothing == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("衣物不存在", color = OnSurfaceVariant)
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(24.dp)) {
                        Text(clothing.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TagChip(clothing.category, PrimaryContainer, OnPrimaryContainer)
                            TagChip(clothing.season, SecondaryContainer, OnSecondaryContainer)
                            TagChip(clothing.style, TertiaryContainer, OnTertiaryContainer)
                            TagChip(clothing.thickness, WarningOrangeBg, WarningOrange)
                        }
                    }
                }

                // Properties
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("属性", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        PropRow("颜色", clothing.color)
                        PropRow("材质", clothing.material)
                        PropRow("厚薄", clothing.thickness)
                        PropRow("季节", clothing.season)
                        PropRow("风格", clothing.style)
                    }
                }

                // AI Description
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, null, Modifier.size(18.dp), tint = PurpleAccent)
                            Spacer(Modifier.width(6.dp))
                            Text("AI 描述", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(clothing.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }

                // Status
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("穿着状态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("连续穿着")
                            Text("${clothing.consecutiveWearDays} 天", fontWeight = FontWeight.SemiBold,
                                color = if (clothing.consecutiveWearDays >= 3) Error else Primary)
                        }
                        if (clothing.lastWornDate != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("上次穿着")
                                Text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(clothing.lastWornDate)),
                                    color = OnSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.markWashing(!clothing.isWashing) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (clothing.isWashing) ButtonDefaults.buttonColors(containerColor = WashRed)
                            else ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(if (clothing.isWashing) Icons.Default.Check else Icons.Default.LocalLaundryService, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (clothing.isWashing) "标记为已清洗" else "标记为清洗中")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = bg.copy(alpha = 0.5f)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
fun PropRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
