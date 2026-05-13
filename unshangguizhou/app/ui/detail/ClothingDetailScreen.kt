package com.yunshangguizhou.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yunshangguizhou.app.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingDetailScreen(clothingId: Long, onBack: () -> Unit, onDeleted: () -> Unit, vm: ClothingDetailViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    var delDlg by remember { mutableStateOf(false) }
    LaunchedEffect(clothingId) { vm.load(clothingId) }

    if (delDlg) AlertDialog(onDismissRequest = { delDlg = false }, title = { Text("确认删除") }, text = { Text("确定删除这件衣物？") },
        confirmButton = { TextButton(onClick = { vm.delete(); delDlg = false; onDeleted() }) { Text("删除", color = Error) } },
        dismissButton = { TextButton(onClick = { delDlg = false }) { Text("取消") } })

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("衣物详情", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    if (!s.editMode) IconButton(onClick = { vm.startEdit() }) { Icon(Icons.Default.Edit, "编辑") }
                    IconButton(onClick = { delDlg = true }) { Icon(Icons.Default.Delete, "删除", tint = Error) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (s.loading) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else s.clothing?.let { c ->
            Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Image
                if (c.imageUri != null) {
                    AsyncImage(model = c.imageUri, contentDescription = c.name,
                        modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop)
                }

                if (s.editMode) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(s.editName, { vm.setEditName(it) }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editCategory, { vm.setEditCat(it) }, label = { Text("分类") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editColor, { vm.setEditColor(it) }, label = { Text("颜色") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editMaterial, { vm.setEditMat(it) }, label = { Text("材质") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editSeason, { vm.setEditSeason(it) }, label = { Text("季节") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editStyle, { vm.setEditStyle(it) }, label = { Text("风格") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editThickness, { vm.setEditThick(it) }, label = { Text("厚薄") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(s.editDesc, { vm.setEditDesc(it) }, label = { Text("描述") }, minLines = 2, shape = RoundedCornerShape(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { vm.cancelEdit() }, Modifier.weight(1f)) { Text("取消") }
                                Button(onClick = { vm.saveEdit() }, Modifier.weight(1f), enabled = !s.saving) {
                                    if (s.saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Text("保存")
                                }
                            }
                        }
                    }
                } else {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(24.dp)) {
                            Text(c.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ChipTag(c.category, PrimaryContainer, OnPrimaryContainer)
                                ChipTag(c.season, SecondaryContainer, OnSecondaryContainer)
                                ChipTag(c.style, TertiaryContainer, OnTertiaryContainer)
                                ChipTag(c.thickness, WarningOrangeBg, WarningOrange)
                            }
                        }
                    }

                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("属性", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            PropRow("颜色", c.color); PropRow("材质", c.material); PropRow("季节", c.season)
                        }
                    }

                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, "AI", Modifier.size(18.dp), tint = PurpleAccent); Spacer(Modifier.width(6.dp))
                                Text("AI 描述", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(c.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                    }

                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("穿着状态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("连续穿着"); Text("${c.consecutiveWearDays} 天", fontWeight = FontWeight.SemiBold, color = if (c.consecutiveWearDays >= 3) Error else Primary)
                            }
                            if (c.lastWornDate != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("上次穿着")
                                    Text(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(c.lastWornDate)), color = OnSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { vm.toggleWash(!c.isWashing) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = if (c.isWashing) ButtonDefaults.buttonColors(containerColor = WashRed) else ButtonDefaults.buttonColors()) {
                                Icon(if (c.isWashing) Icons.Default.Check else Icons.Default.LocalLaundryService, "清洗"); Spacer(Modifier.width(8.dp))
                                Text(if (c.isWashing) "标记为已清洗" else "标记为清洗中")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun ChipTag(t: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) { Surface(shape = RoundedCornerShape(6.dp), color = bg.copy(alpha = 0.5f)) { Text(t, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = fg) } }
@Composable fun PropRow(l: String, v: String) { Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), Arrangement.SpaceBetween) { Text(l, color = OnSurfaceVariant); Text(v, fontWeight = FontWeight.Medium) } }
