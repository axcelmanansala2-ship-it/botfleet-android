package com.botfleet.android.ui.screens.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.botfleet.android.ui.screens.login.outlinedFieldColors
import com.botfleet.android.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit,
    onUploaded: () -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var botName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onUploaded()
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "bot_file"
            viewModel.setSelectedFile(fileName)
            val tmpFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(it)?.use { input ->
                tmpFile.outputStream().use { out -> input.copyTo(out) }
            }
            selectedFile = tmpFile
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Upload Bot", fontWeight = FontWeight.SemiBold, color = OnBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = botName,
                onValueChange = { botName = it },
                label = { Text("Bot Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .border(
                        width = 1.5.dp,
                        color = if (selectedFile != null) Primary else Border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { filePicker.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (selectedFile != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (selectedFile != null) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    if (selectedFile != null) {
                        Text(uiState.selectedFileName ?: "", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Tap to change", color = OnSurfaceVariant, fontSize = 11.sp)
                    } else {
                        Text("Tap to select .py or .zip", color = OnSurfaceVariant, fontSize = 13.sp)
                        Text("Python bot file or zip archive", color = OnSurfaceVariant.copy(0.6f), fontSize = 11.sp)
                    }
                }
            }

            uiState.error?.let {
                Text(it, color = Error, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    selectedFile?.let { viewModel.upload(it, botName) }
                        ?: run { viewModel.upload(File(""), botName) }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading && selectedFile != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = OnPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Bot", fontWeight = FontWeight.SemiBold)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Supported formats", color = OnBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("• .py — single Python bot file", color = OnSurfaceVariant, fontSize = 12.sp)
                    Text("• .zip — bot project folder (auto-detects entry file)", color = OnSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Dependencies from requirements.txt are installed automatically.", color = OnSurfaceVariant, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
