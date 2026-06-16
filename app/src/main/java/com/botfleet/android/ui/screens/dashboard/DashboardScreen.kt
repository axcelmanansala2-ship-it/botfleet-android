package com.botfleet.android.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.botfleet.android.ui.components.BotCard
import com.botfleet.android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToBotDetail: (String) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val filteredBots = if (search.isBlank()) uiState.bots
    else uiState.bots.filter { it.name.contains(search, ignoreCase = true) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        viewModel.logout()
                        onLoggedOut()
                    }
                }) { Text("Sign Out", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text("BotFleet", fontWeight = FontWeight.Bold, color = Primary, fontSize = 20.sp)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUpload,
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload Bot")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total", uiState.stats?.totalBots, Icons.Default.SmartToy, PrimaryContainer, Primary, Modifier.weight(1f))
                    StatCard("Running", uiState.stats?.runningBots, Icons.Default.PlayArrow, SuccessContainer, Success, Modifier.weight(1f))
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Stopped", uiState.stats?.stoppedBots, Icons.Default.Stop, SurfaceVariant, OnSurfaceVariant, Modifier.weight(1f))
                    StatCard("Crashed", uiState.stats?.crashedBots, Icons.Default.Warning, ErrorContainer, Error, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search bots...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border,
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            cursorColor = Primary,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Bots",
                        color = OnBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.bots.isNotEmpty()) {
                        Text(
                            text = if (search.isBlank()) "${uiState.bots.size}" else "${filteredBots.size}/${uiState.bots.size}",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                    )
                }
            } else if (filteredBots.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SmartToy, null, tint = OnSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(
                            text = if (search.isBlank()) "No bots yet" else "No bots match \"$search\"",
                            color = OnBackground,
                            fontWeight = FontWeight.Medium
                        )
                        if (search.isBlank()) {
                            Button(
                                onClick = onNavigateToUpload,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Upload Bot")
                            }
                        } else {
                            TextButton(onClick = { search = "" }) { Text("Clear search") }
                        }
                    }
                }
            } else {
                items(filteredBots, key = { it.id }) { bot ->
                    BotCard(
                        bot = bot,
                        onStart = { viewModel.startBot(bot.id) },
                        onStop = { viewModel.stopBot(bot.id) },
                        onRestart = { viewModel.restartBot(bot.id) },
                        onDelete = { viewModel.deleteBot(bot.id) },
                        onClick = { onNavigateToBotDetail(bot.id) },
                        isActionLoading = uiState.actionLoadingBotId == bot.id
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: Int?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, color = OnSurfaceVariant, fontSize = 11.sp)
            Text(
                text = value?.toString() ?: "-",
                color = OnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
