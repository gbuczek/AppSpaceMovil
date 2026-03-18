package com.appspace.movil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pantalla de ajustes
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoCleanEnabled by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sección: General
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, paddingValues.calculateTopPadding())
            )
            
            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                description = "Recibir alertas de limpieza",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            
            SettingsSwitchItem(
                icon = Icons.Default.AutoAwesome,
                title = "Limpieza automática",
                description = "Limpiar caché automáticamente cada semana",
                checked = autoCleanEnabled,
                onCheckedChange = { autoCleanEnabled = it }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Sección: Apariencia
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Modo oscuro",
                description = "Usar tema oscuro",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Sección: Almacenamiento
            Text(
                text = "Almacenamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = "Limpiar caché",
                description = "Eliminar archivos temporales",
                onClick = { /* Acción de limpiar caché */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Eliminar duplicados",
                description = "Buscar y eliminar archivos repetidos",
                onClick = { /* Acción de eliminar duplicados */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Sección: Acerca de
            Text(
                text = "Acerca de",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Versión",
                description = "1.0.0",
                onClick = { }
            )
            
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Términos y privacidad",
                description = "Leer política de privacidad",
                onClick = { }
            )
        }
    }
}

/**
 * Item de ajuste con switch
 */
@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Item de ajuste clicable
 */
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ir",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
