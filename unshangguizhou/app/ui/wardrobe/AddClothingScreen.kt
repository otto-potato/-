package com.yunshangguizhou.app.ui.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yunshangguizhou.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothingScreen(onBack: () -> Unit, onSuccess: () -> Unit, vm: AddClothingViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) vm.setImage(it) }

    LaunchedEffect(s.success) { if (s.success) { onSuccess(); vm.reset() } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("添加衣物", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Photo
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (s.imageUri != null) {
                    Box {
                        AsyncImage(ImageRequest.Builder(ctx).data(s.imageUri).crossfade(true).build(),
                            "衣物照片", Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { vm.setImage(null) }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Surface(shape = RoundedCornerShape(50), color = ErrorContainer) { Icon(Icons.Default.Close, "移除", Modifier.size(20.dp), tint = Error) }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)),
                        onClick = { picker.launch("image/*") },
                        color = PrimaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.CameraAlt, "拍照", Modifier.size(36.dp), tint = Primary.copy(alpha = 0.6f))
                                Spacer(Modifier.height(8.dp)); Text("添加照片", style = MaterialTheme.typography.labelMedium, color = Primary.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Name
            OutlinedTextField(s.name, { vm.setName(it) }, label = { Text("衣物名称 *") }, placeholder = { Text("如：白色纯棉T恤") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))

            // Category
            Text("分类", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(clothingCategories.size) { i -> val c = clothingCategories[i]; FilterChip(selected = s.category == c, onClick = { vm.setCategory(c) }, label = { Text(c) }, shape = RoundedCornerShape(8.dp)) } }

            Text("颜色", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(colorOptions.size) { i -> val c = colorOptions[i]; FilterChip(selected = s.color == c, onClick = { vm.setColor(c) }, label = { Text(c) }, shape = RoundedCornerShape(8.dp)) } }

            Text("材质", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(materialOptions.size) { i -> val m = materialOptions[i]; FilterChip(selected = s.material == m, onClick = { vm.setMaterial(m) }, label = { Text(m) }, shape = RoundedCornerShape(8.dp)) } }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("厚薄", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { thicknessOptions.forEach { t -> FilterChip(selected = s.thickness == t, onClick = { vm.setThickness(t) }, label = { Text(t) }, shape = RoundedCornerShape(8.dp)) } }
                }
                Column(Modifier.weight(1f)) {
                    Text("季节", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { seasonOptions.forEach { se -> FilterChip(selected = s.season == se, onClick = { vm.setSeason(se) }, label = { Text(se) }, shape = RoundedCornerShape(8.dp)) } }
                }
            }

            Text("风格", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { styleOptions.forEach { st -> FilterChip(selected = s.style == st, onClick = { vm.setStyle(st) }, label = { Text(st) }, shape = RoundedCornerShape(8.dp)) } }

            OutlinedTextField(s.description, { vm.setDesc(it) }, label = { Text("描述（可选）") },
                placeholder = { Text("AI会自动生成，也可自行填写") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp))

            s.error?.let {
                val isWarn = s.aiSuccess == false || it.contains("AI分析失败")
                Card(colors = CardDefaults.cardColors(containerColor = if (isWarn) WarningOrangeBg else ErrorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(it, Modifier.padding(12.dp), color = if (isWarn) WarningOrange else OnErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { vm.analyzeWithAi() }, Modifier.fillMaxWidth().height(50.dp),
                enabled = !s.loading && s.name.isNotBlank(), shape = RoundedCornerShape(14.dp)) {
                if (s.loading) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                Icon(Icons.Filled.AutoAwesome, "AI分析", Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("AI 分析并保存", fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(onClick = { vm.saveManually() }, Modifier.fillMaxWidth().height(50.dp),
                enabled = !s.loading && s.name.isNotBlank(), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Save, "保存", Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("手动保存")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
