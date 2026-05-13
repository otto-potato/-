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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yunshangguizhou.app.YunShangGuiZhouApp
import com.yunshangguizhou.app.data.remote.WeatherSource
import com.yunshangguizhou.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsVM = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val app = LocalContext.current.applicationContext as YunShangGuiZhouApp
    LaunchedEffect(s.saved) { if (s.saved) { snack.showSnackbar("设置已保存"); vm.clearSaved() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        snackbarHost = { SnackbarHost(snack) }, containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = InfoBlueBg) { Icon(Icons.Filled.AutoAwesome, "AI", Modifier.padding(6.dp).size(20.dp), tint = InfoBlue) }
                        Spacer(Modifier.width(10.dp)); Text("AI 模型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(s.aiUrl, { vm.setAiUrl(it) }, label = { Text("API 地址") }, placeholder = { Text("https://api.deepseek.com") }, supportingText = { Text("支持 OpenAI/DeepSeek 兼容 API") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    var showK by remember { mutableStateOf(false) }
                    OutlinedTextField(s.aiKey, { vm.setAiKey(it) }, label = { Text("API 密钥") }, placeholder = { Text("sk-...") },
                        visualTransformation = if (showK) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { showK = !showK }) { Icon(if (showK) Icons.Default.VisibilityOff else Icons.Default.Visibility, "显示") } },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(s.aiModel, { vm.setAiModel(it) }, label = { Text("模型名称") }, placeholder = { Text("deepseek-v4-flash") }, supportingText = { Text("推荐 deepseek-v4-flash 或 gpt-4o") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                }
            }

            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = WarningOrangeBg) { Icon(Icons.Filled.WbSunny, "天气", Modifier.padding(6.dp).size(20.dp), tint = WarningOrange) }
                        Spacer(Modifier.width(10.dp)); Text("天气数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = s.wxSrc == WeatherSource.OPEN_METEO, onClick = { vm.setWxSrc(WeatherSource.OPEN_METEO) }, label = { Text("Open-Meteo") }, leadingIcon = { Icon(Icons.Default.Public, "免费", Modifier.size(16.dp)) })
                        FilterChip(selected = s.wxSrc == WeatherSource.QWEATHER, onClick = { vm.setWxSrc(WeatherSource.QWEATHER) }, label = { Text("和风天气") }, leadingIcon = { Icon(Icons.Default.Cloud, "和风", Modifier.size(16.dp)) })
                    }
                    Spacer(Modifier.height(12.dp))
                    if (s.wxSrc == WeatherSource.QWEATHER) {
                        var sk by remember { mutableStateOf(false) }
                        OutlinedTextField(s.qwKey, { vm.setQwKey(it) }, label = { Text("和风天气 API 密钥") },
                            visualTransformation = if (sk) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { sk = !sk }) { Icon(if (sk) Icons.Default.VisibilityOff else Icons.Default.Visibility, "显示") } },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(s.qwSearch, { vm.setQwSearch(it) }, label = { Text("搜索城市") }, placeholder = { Text("如：北京") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { vm.searchCity() }, enabled = !s.qwSearching, shape = RoundedCornerShape(12.dp)) {
                                if (s.qwSearching) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("搜索")
                            }
                        }
                        if (s.qwLocId.isNotBlank()) { Spacer(Modifier.height(4.dp)); Surface(shape = RoundedCornerShape(6.dp), color = SuccessGreenBg) { Text("城市 ID: ${s.qwLocId}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = SuccessGreen) } }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(s.lat, { vm.setLat(it) }, label = { Text("纬度") }, placeholder = { Text("39.9042") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(s.lon, { vm.setLon(it) }, label = { Text("经度") }, placeholder = { Text("116.4074") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = PurpleAccent.copy(alpha = 0.1f)) { Icon(Icons.Outlined.Schedule, "定时", Modifier.padding(6.dp).size(20.dp), tint = PurpleAccent) }
                            Spacer(Modifier.width(10.dp)); Text("每日自动推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("每天凌晨3点生成穿搭", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Switch(checked = s.calendar, onCheckedChange = { vm.setCal(it) })
                }
            }

            s.error?.let { Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(12.dp)) { Text(it, Modifier.padding(12.dp), color = OnErrorContainer, style = MaterialTheme.typography.bodySmall) } }

            Button(onClick = { vm.save(app) }, Modifier.fillMaxWidth().height(52.dp), enabled = !s.saving, shape = RoundedCornerShape(14.dp)) {
                if (s.saving) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                Text("保存设置", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
