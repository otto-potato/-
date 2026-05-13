package com.yunshangguizhou.app.ui.settings

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yunshangguizhou.app.data.remote.WeatherSource
import com.yunshangguizhou.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) { snackbarHostState.showSnackbar("设置已保存"); viewModel.clearSaveSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // === AI Config ===
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = InfoBlueBg) {
                            Icon(Icons.Filled.AutoAwesome, null, Modifier.padding(6.dp).size(20.dp), tint = InfoBlue)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("AI 模型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(state.aiApiUrl, { viewModel.updateAiUrl(it) },
                        label = { Text("API 地址") }, placeholder = { Text("https://api.deepseek.com") },
                        supportingText = { Text("支持 OpenAI/DeepSeek 兼容 API") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    var showKey by remember { mutableStateOf(false) }
                    OutlinedTextField(state.aiApiKey, { viewModel.updateAiKey(it) },
                        label = { Text("API 密钥") }, placeholder = { Text("sk-...") },
                        visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { showKey = !showKey }) { Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(state.aiModelName, { viewModel.updateModelName(it) },
                        label = { Text("模型名称") }, placeholder = { Text("deepseek-v4-flash") },
                        supportingText = { Text("推荐 deepseek-v4-flash 或 gpt-4o") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                }
            }

            // === Weather Config ===
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = WarningOrangeBg) {
                            Icon(Icons.Filled.WbSunny, null, Modifier.padding(6.dp).size(20.dp), tint = WarningOrange)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("天气数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.weatherSource == WeatherSource.OPEN_METEO,
                            onClick = { viewModel.updateWeatherSource(WeatherSource.OPEN_METEO) },
                            label = { Text("Open-Meteo") },
                            leadingIcon = { Icon(Icons.Default.Public, null, Modifier.size(16.dp)) })
                        FilterChip(selected = state.weatherSource == WeatherSource.QWEATHER,
                            onClick = { viewModel.updateWeatherSource(WeatherSource.QWEATHER) },
                            label = { Text("和风天气") },
                            leadingIcon = { Icon(Icons.Default.Cloud, null, Modifier.size(16.dp)) })
                    }
                    Spacer(Modifier.height(12.dp))
                    if (state.weatherSource == WeatherSource.QWEATHER) {
                        var showQKey by remember { mutableStateOf(false) }
                        OutlinedTextField(state.qweatherKey, { viewModel.updateQWeatherKey(it) },
                            label = { Text("和风天气 API 密钥") },
                            visualTransformation = if (showQKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { showQKey = !showQKey }) { Icon(if (showQKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(state.qweatherCitySearch, { viewModel.updateQWeatherCitySearch(it) },
                                label = { Text("搜索城市") }, placeholder = { Text("如：北京、上海") },
                                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { viewModel.searchCity() }, enabled = !state.qweatherSearching,
                                shape = RoundedCornerShape(12.dp)) {
                                if (state.qweatherSearching) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                else Text("搜索")
                            }
                        }
                        if (state.qweatherLocationId.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = SuccessGreenBg) {
                                Text("城市 ID: ${state.qweatherLocationId}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(state.weatherLatitude, { viewModel.updateWeatherLatitude(it) },
                                label = { Text("纬度") }, placeholder = { Text("39.9042") },
                                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(state.weatherLongitude, { viewModel.updateWeatherLongitude(it) },
                                label = { Text("经度") }, placeholder = { Text("116.4074") },
                                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }

            // === Auto recommend ===
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = PurpleAccent.copy(alpha = 0.1f)) {
                                Icon(Icons.Outlined.Schedule, null, Modifier.padding(6.dp).size(20.dp), tint = PurpleAccent)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("每日自动推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("每天凌晨3点生成穿搭", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Switch(checked = state.calendarEnabled, onCheckedChange = { viewModel.updateCalendarEnabled(it) })
                }
            }

            state.error?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(err, Modifier.padding(12.dp), color = OnErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { viewModel.saveSettings() }, Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isSaving, shape = RoundedCornerShape(14.dp)) {
                if (state.isSaving) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                Text("保存设置", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
