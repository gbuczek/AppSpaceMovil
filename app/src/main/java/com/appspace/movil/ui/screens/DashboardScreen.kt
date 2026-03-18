package com.appspace.movil.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appspace.movil.ui.viewmodel.CleanViewModel
import com.appspace.movil.ui.viewmodel.UiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

/**
 * Pantalla principal de Dashboard
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    viewModel: CleanViewModel = viewModel(),
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAgent: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()
    val files by viewModel.files.collectAsState()
    val actions by viewModel.agentActions.collectAsState()
    val reports by viewModel.reports.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    // Solicitar permisos
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    LaunchedEffect(Unit) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.scanFiles()
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AppSpace Movil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Ajustes */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de almacenamiento
            if (storageStats != null) {
                item {
                    StorageCard(storageStats = storageStats!!)
                }
            }
            
            // Botón de escanear
            item {
                PrimaryActionButton(
                    text = if (uiState.isScanning) "Escaneando..." else "Escanear ahora",
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    onClick = { viewModel.scanFiles() },
                    isLoading = uiState.isScanning
                )
            }
            
            // Acciones rápidas
            if (actions.isNotEmpty()) {
                item {
                    Text(
                        text = "Recomendaciones del Agente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(actions.take(3)) { action ->
                    AgentActionCard(
                        action = action,
                        onApprove = { viewModel.executeAction(action) },
                        onReject = { /* Ignorar acción */ }
                    )
                }
            }
            
            // Categorías
            item {
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                CategoryGrid(onNavigateToCategory = onNavigateToCategory)
            }
            
            // Informes recientes
            if (reports.isNotEmpty()) {
                item {
                    Text(
                        text = "Último informe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    val lastReport = reports.first()
                    ReportCard(
                        spaceFreed = lastReport.formattedSpaceFreed,
                        filesDeleted = lastReport.filesDeleted,
                        date = lastReport.formattedDate,
                        summary = lastReport.summary
                    )
                }
            }
            
            // Botón para limpiar todo
            if (actions.isNotEmpty()) {
                item {
                    PrimaryActionButton(
                        text = "Limpiar todo",
                        icon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                        onClick = {
                            actions.forEach { action ->
                                viewModel.executeAction(action)
                            }
                        },
                        isLoading = uiState.isCleaning
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Grid de categorías
 */
@Composable
fun CategoryGrid(
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryItem("Fotos", Icons.Default.Photo, "photos_color"),
        CategoryItem("Videos", Icons.Default.VideoLibrary, "videos_color"),
        CategoryItem("Documentos", Icons.Default.Description, "documents_color"),
        CategoryItem("Descargas", Icons.Default.Download, "downloads_color"),
        CategoryItem("Caché", Icons.Default.DeleteSweep, "cache_color"),
        CategoryItem("Duplicados", Icons.Default.ContentCopy, "accent")
    )
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                categoryName = category.name,
                icon = { Icon(category.icon, contentDescription = null) },
                size = "0 B", // Se calculará dinámicamente
                fileCount = 0,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onNavigateToCategory(category.name) }
            )
        }
    }
}

data class CategoryItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val colorKey: String
)
