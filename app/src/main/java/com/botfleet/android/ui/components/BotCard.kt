package com.botfleet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botfleet.android.data.api.models.Bot
import com.botfleet.android.ui.theme.*

@Composable
fun BotCard(
    bot: Bot,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    isActionLoading: Boolean = false
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bot.name,
                    color = OnBackground,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )
                StatusBadge(status = bot.status)
            }
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bot.cpuPercent != null) {
                    Text("CPU ${String.format("%.1f", bot.cpuPercent)}%", color = OnSurfaceVariant, fontSize = 11.sp)
                }
                if (bot.memMb != null) {
                    Text("RAM ${String.format("%.0f", bot.memMb)}MB", color = OnSurfaceVariant, fontSize = 11.sp)
                }
                if (bot.uptime != null && bot.status == "running") {
                    Text(formatUptime(bot.uptime), color = Success, fontSize = 11.sp)
                }
                if (bot.cpuPercent == null && bot.memMb == null) {
                    Text(bot.entryFile, color = OnSurfaceVariant, fontSize = 11.sp)
                }
            }
        }

        if (!showDeleteConfirm) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                if (bot.status == "running") {
                    IconButton(
                        onClick = onRestart,
                        enabled = !isActionLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = OnSurfaceVariant)
                        } else {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Restart", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = onStop,
                        enabled = !isActionLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                } else {
                    IconButton(
                        onClick = onStart,
                        enabled = !isActionLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Success)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Success, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Details", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("Delete", fontSize = 12.sp)
                }
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        }
    }
}

fun formatUptime(seconds: Double): String {
    val total = seconds.toLong()
    return when {
        total < 60 -> "${total}s"
        total < 3600 -> "${total / 60}m ${total % 60}s"
        else -> "${total / 3600}h ${(total % 3600) / 60}m"
    }
}
