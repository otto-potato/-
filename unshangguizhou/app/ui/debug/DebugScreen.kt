package com.yunshangguizhou.app.ui.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object DebugLog {
    val entries = mutableStateListOf<LogEntry>()
    fun log(tag: String, msg: String) { entries.add(0, LogEntry(System.currentTimeMillis(), tag, msg)) }
    fun clear() { entries.clear() }
}

data class LogEntry(val time: Long, val tag: String, val msg: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val clip = LocalClipboardManager.current
    var selected by remember { mutableStateOf<LogEntry?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("调试日志") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { DebugLog.clear() }) { Icon(Icons.Default.Delete, "清空") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        selected?.let { e ->
            AlertDialog(onDismissRequest = { selected = null },
                title = { Text(e.tag) },
                text = { Text(e.msg, style = MaterialTheme.typography.bodySmall) },
                confirmButton = { TextButton(onClick = { clip.setText(AnnotatedString(e.msg)); selected = null }) { Text("复制") } },
                dismissButton = { TextButton(onClick = { selected = null }) { Text("关闭") } })
        }
        if (DebugLog.entries.isEmpty()) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("暂无日志") }
        else LazyColumn(modifier = Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(DebugLog.entries.toList()) { e ->
                Card(Modifier.fillMaxWidth().clickable { selected = e }, shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = when {
                        e.msg.contains("HTTP 4") || e.msg.contains("HTTP 5") -> MaterialTheme.colorScheme.errorContainer
                        e.msg.contains("FAIL") || e.msg.contains("ERROR") -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    })
                ) {
                    Row(Modifier.padding(10.dp)) {
                        Text(e.tag, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(60.dp))
                        Text(e.msg, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    }
                }
            }
        }
    }
}
