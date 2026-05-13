package com.yunshangguizhou.app.ui.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
fun AddClothingScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddClothingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setImageUri(uri)
    }

    LaunchedEffect(state.saveSuccess) { if (state.saveSuccess) onSuccess() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加衣物", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (state.imageUri != null) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(state.imageUri).crossfade(true).build(),
                            contentDescription = "衣物照片",
                            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.setImageUri(null) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Surface(shape = CircleShape, color = ErrorContainer) {
                                Icon(Icons.Default.Close, null, Modifier.size(20.dp), tint = Error)
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)),
                        color = PrimaryContainer.copy(alpha = 0.3f),
                        onClick = { imagePicker.launch("image/*") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.CameraAlt, null, Modifier.size(36.dp), tint = Primary.copy(alpha = 0.6f))
                                Spacer(Modifier.height(8.dp))
                                Text("添加照片", style = MaterialTheme.typography.labelMedium, color = Primary.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Name
            OutlinedTextField(state.name, { viewModel.updateName(it) },
                label = { Text("衣物名称 *") }, placeholder = { Text("如：白色纯棉T恤") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))

            // Category
            SectionLabel("分类")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clothingCategories.size) { idx ->
                    val cat = clothingCategories[idx]
                    FilterChip(selected = state.category == cat, onClick = { viewModel.updateCategory(cat) },
                        label = { Text(cat) }, shape = RoundedCornerShape(8.dp))
                }
            }

            // Color
            SectionLabel("颜色")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorOptions.size) { idx ->
                    val c = colorOptions[idx]
                    FilterChip(selected = state.color == c, onClick = { viewModel.updateColor(c) },
                        label = { Text(c) }, shape = RoundedCornerShape(8.dp))
                }
            }

            // Material & Thickness
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SectionLabel("材质")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(materialOptions.size) { idx ->
                            val m = materialOptions[idx]
                            FilterChip(selected = state.material == m, onClick = { viewModel.updateMaterial(m) },
                                label = { Text(m) }, shape = RoundedCornerShape(8.dp))
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SectionLabel("厚薄")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        thicknessOptions.forEach { t ->
                            FilterChip(selected = state.thickness == t, onClick = { viewModel.updateThickness(t) },
                                label = { Text(t) }, shape = RoundedCornerShape(8.dp))
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    SectionLabel("季节")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        seasonOptions.forEach { s ->
                            FilterChip(selected = state.season == s, onClick = { viewModel.updateSeason(s) },
                                label = { Text(s) }, shape = RoundedCornerShape(8.dp))
                        }
                    }
                }
            }

            // Style
            SectionLabel("风格")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                styleOptions.forEach { s ->
                    FilterChip(selected = state.style == s, onClick = { viewModel.updateStyle(s) },
                        label = { Text(s) }, shape = RoundedCornerShape(8.dp))
                }
            }

            // Description
            OutlinedTextField(state.description, { viewModel.updateDescription(it) },
                label = { Text("描述（可选）") }, placeholder = { Text("AI会自动生成描述，也可自行填写") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp))

            // Error
            state.error?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(err, Modifier.padding(12.dp), color = OnErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Buttons
            Button(onClick = { viewModel.analyzeWithAi() }, Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isAnalyzing && state.name.isNotBlank(), shape = RoundedCornerShape(14.dp)) {
                if (state.isAnalyzing) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                Icon(Icons.Filled.AutoAwesome, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("AI 分析并保存", fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(onClick = { viewModel.saveManually() }, Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isSaving && state.name.isNotBlank(), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("手动保存")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp))
}

@Composable
fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
