package com.botfleet.android.ui.screens.botdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.botfleet.android.ui.components.StatusBadge
import com.botfleet.android.ui.components.formatUptime
import com.botfleet.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotDetailScreen(
    botId: String,
    onNavigateBack: () -> Unit,
    viewModel: BotDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logListState = rememberLazyListState()

    LaunchedEffect(botId) { viewModel.init(botId) }
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            logListState.animateScrollToItem(uiState.logs.size - 1)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.bot?.name ?: "Bot Detail",
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                            fontSize = 17.sp
                        )
                        uiState.bot?.let {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(it.status)
                                if (uiState.wsConnected) {
                                    Text("● live", color = Success, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        val bot = uiState.bot
        if (bot == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Bot not found", color = OnSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bot.status == "running") {
                        Button(
                            onClick = { viewModel.restartBot() },
                            enabled = !uiState.isActionLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isActionLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = OnBackground)
                            else Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restart", color = OnBackground, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { viewModel.stopBot() },
                            enabled = !uiState.isActionLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop", color = OnBackground, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startBot() },
                            enabled = !uiState.isActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isActionLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = OnPrimary)
                            else Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Start", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Info", fontWeight = FontWeight.SemiBold, color = OnBackground, fontSize = 13.sp)
                    InfoRow("Entry File", bot.entryFile)
                    if (bot.uptime != null && bot.status == "running") {
                        InfoRow("Uptime", formatUptime(bot.uptime))
                    }
                    if (bot.cpuPercent != null) {
                        InfoRow("CPU", "${String.format("%.1f", bot.cpuPercent)}%")
                    }
                    if (bot.memMb != null) {
                        InfoRow("Memory", "${String.format("%.0f", bot.memMb)} MB")
                    }
                    InfoRow("Crashes", bot.crashCount.toString())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-restart", color = OnSurfaceVariant, fontSize = 13.sp)
                        Switch(
                            checked = bot.autoRestart,
                            onCheckedChange = { viewModel.updateAutoRestart(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = Primary)
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Logs", fontWeight = FontWeight.SemiBold, color = OnBackground, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!uiState.wsConnected) {
                                Text("● offline", color = OnSurfaceVariant, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            }
                            IconButton(
                                onClick = { viewModel.clearLogs() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color(0xFF0D0D14))
                            .padding(8.dp)
                    ) {
                        if (uiState.logs.isEmpty()) {
                            Text(
                                "No logs yet...",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(state = logListState) {
                                items(uiState.logs) { line ->
                                    Text(
                                        text = line,
                                        color = when {
                                            line.contains("ERROR", ignoreCase = true) || line.contains("error", ignoreCase = true) -> Error
                                            line.contains("WARNING", ignoreCase = true) || line.contains("WARN", ignoreCase = true) -> Warning
                                            else -> Color(0xFF9CA3AF)
                                        },
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (bot.installedPackages.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Installed Packages", fontWeight = FontWeight.SemiBold, color = OnBackground, fontSize = 13.sp)
                        bot.installedPackages.forEach { pkg ->
                            Text("• $pkg", color = OnSurfaceVariant, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Text(value, color = OnBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}