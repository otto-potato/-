package com.yunshangguizhou.app.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.ui.theme.*

data class CatFilter(val key: String?, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    onAdd: () -> Unit, onDetail: (Long) -> Unit, onBack: () -> Unit,
    vm: WardrobeViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()
    var delDialog by remember { mutableStateOf<ClothingEntity?>(null) }

    delDialog?.let { c ->
        AlertDialog(onDismissRequest = { delDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定删除「${c.name}」？此操作不可撤销。") },
            confirmButton = { TextButton(onClick = { vm.delete(c); delDialog = null }) { Text("删除", color = Error) } },
            dismissButton = { TextButton(onClick = { delDialog = null }) { Text("取消") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的衣柜", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { IconButton(onClick = onAdd) { Icon(Icons.Default.Add, "添加") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Default.Add, "添加") },
                text = { Text("添加衣物") }, shape = RoundedCornerShape(16.dp))
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (s.all.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = PrimaryContainer.copy(alpha = 0.4f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Checkroom, "衣柜", Modifier.size(40.dp), tint = Primary.copy(alpha = 0.5f)) }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("衣柜是空的", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("点击下方按钮添加衣物", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
        } else {
            Column(Modifier.padding(pad)) {
                // Search bar
                OutlinedTextField(s.searchQuery, { vm.setSearch(it) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索衣物...") }, leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                    trailingIcon = { if (s.searchQuery.isNotBlank()) IconButton(onClick = { vm.setSearch("") }) { Icon(Icons.Default.Close, "清除") } },
                    singleLine = true, shape = RoundedCornerShape(12.dp))

                // Category filter
                val cats = listOf(CatFilter(null, "全部")) + clothingCategories.map { CatFilter(it, it) }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cats.size) { idx ->
                        val cat = cats[idx]
                        FilterChip(selected = s.selectedCategory == cat.key, onClick = { vm.setCategory(cat.key) },
                            label = { Text(cat.label) }, shape = RoundedCornerShape(10.dp))
                    }
                }

                // Grid
                val list = s.filtered
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("没有匹配的衣物", color = OnSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(list, key = { it.id }) { c -> GridItem(c, { onDetail(c.id) }, { vm.toggleWash(c.id, !c.isWashing) }, { delDialog = c }) }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(c: ClothingEntity, onClick: () -> Unit, onWash: () -> Unit, onDel: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))) {
        Column {
            Box {
                Box(Modifier.fillMaxWidth().height(140.dp).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    if (c.imageUri != null) AsyncImage(c.imageUri, c.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Outlined.Image, c.name, Modifier.size(40.dp), tint = Primary.copy(alpha = 0.3f))
                }
                Column(Modifier.align(Alignment.TopEnd).padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (c.isWashing) { Surface(shape = RoundedCornerShape(6.dp), color = WashRedBg) { Text("清洗中", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = WashRed) } }
                    if (c.consecutiveWearDays >= 2) { Surface(shape = RoundedCornerShape(6.dp), color = InfoBlueBg) { Text("连穿${c.consecutiveWearDays}天", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = InfoBlue) } }
                }
                Box(Modifier.align(Alignment.TopStart)) {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.MoreVert, "操作", Modifier.size(18.dp)) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(if (c.isWashing) "标记已清洗" else "标记清洗中") }, onClick = { onWash(); menu = false }, leadingIcon = { Icon(Icons.Default.LocalLaundryService, null) })
                        DropdownMenuItem(text = { Text("删除", color = Error) }, onClick = { onDel(); menu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Error) })
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(c.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = PrimaryContainer.copy(alpha = 0.4f)) { Text(c.category, Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer) }
                    Surface(shape = RoundedCornerShape(4.dp), color = SecondaryContainer.copy(alpha = 0.4f)) { Text(c.season, Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer) }
                }
            }
        }
    }
}
