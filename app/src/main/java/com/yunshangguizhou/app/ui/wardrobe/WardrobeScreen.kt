package com.yunshangguizhou.app.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: WardrobeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<ClothingEntity?>(null) }

    showDeleteDialog?.let { clothing ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${clothing.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClothing(clothing)
                    showDeleteDialog = null
                }) { Text("删除", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的衣柜", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = onNavigateToAdd) { Icon(Icons.Default.Add, "添加") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("添加衣物") },
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.allClothing.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = PrimaryContainer.copy(alpha = 0.4f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Checkroom, null, Modifier.size(40.dp), tint = Primary.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("衣柜是空的", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("点击下方按钮添加衣物", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
        } else {
            Column(Modifier.padding(padding)) {
                // Category filter
                CategoryFilterRow(uiState.selectedCategory) { viewModel.filterByCategory(it) }
                // Grid
                val filtered = uiState.filteredClothing
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { clothing ->
                        ClothingGridItem(
                            clothing,
                            onClick = { onNavigateToDetail(clothing.id) },
                            onWashToggle = { viewModel.markWashing(clothing.id, !clothing.isWashing) },
                            onDelete = { showDeleteDialog = clothing }
                        )
                    }
                }
            }
        }
    }
}

data class CatFilter(val key: String?, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(selected: String?, onSelect: (String?) -> Unit) {
    val cats = listOf(CatFilter(null, "全部")) + clothingCategories.map { CatFilter(it, it) }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cats.size) { index ->
            val cat = cats[index]
            FilterChip(
                selected = selected == cat.key,
                onClick = { onSelect(cat.key) },
                label = { Text(cat.label, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
fun ClothingGridItem(
    clothing: ClothingEntity,
    onClick: () -> Unit,
    onWashToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Box {
                // Image placeholder
                Box(
                    Modifier.fillMaxWidth().height(140.dp).background(PrimaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (clothing.imageUri != null) {
                        AsyncImage(clothing.imageUri, clothing.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Outlined.Image, null, Modifier.size(40.dp), tint = Primary.copy(alpha = 0.3f))
                    }
                }
                // Badges
                Column(Modifier.align(Alignment.TopEnd).padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (clothing.isWashing) {
                        Surface(shape = RoundedCornerShape(6.dp), color = WashRedBg) {
                            Text("清洗中", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = WashRed)
                        }
                    }
                    if (clothing.consecutiveWearDays >= 2) {
                        Surface(shape = RoundedCornerShape(6.dp), color = InfoBlueBg) {
                            Text("连穿${clothing.consecutiveWearDays}天", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = InfoBlue)
                        }
                    }
                }
                // Menu button
                Box(Modifier.align(Alignment.TopStart)) {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, null, Modifier.size(18.dp), tint = OnSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (clothing.isWashing) "标记已清洗" else "标记清洗中") },
                            onClick = { onWashToggle(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.LocalLaundryService, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = Error) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Error) }
                        )
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(clothing.name, style = MaterialTheme.typography.labelLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = PrimaryContainer.copy(alpha = 0.4f)) {
                        Text(clothing.category, Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer)
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = SecondaryContainer.copy(alpha = 0.4f)) {
                        Text(clothing.season, Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                    }
                }
            }
        }
    }
}
