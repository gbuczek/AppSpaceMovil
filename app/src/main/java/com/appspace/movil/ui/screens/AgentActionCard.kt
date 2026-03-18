package com.appspace.movil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appspace.movil.model.AgentAction
import com.appspace.movil.model.RiskLevel

/**
 * Tarjeta de acción del agente IA
 */
@Composable
fun AgentActionCard(
    action: AgentAction,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val riskColor = when (action.riskLevel) {
        RiskLevel.LOW -> MaterialTheme.colorScheme.secondary
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
    }
    
    val riskText = when (action.riskLevel) {
        RiskLevel.LOW -> "Seguro"
        RiskLevel.MEDIUM -> "Precaución"
        RiskLevel.HIGH -> "Alto riesgo"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getActionIcon(action.type),
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = riskColor.copy(alpha = 0.2f),
                                contentColor = riskColor
                            ) {
                                Text(
                                    text = riskText,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = action.formattedSpaceToFree,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                    )
                }
            }
            
            // Descripción
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Recomendación del agente
            if (action.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = action.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Contenido expandido
            if (isExpanded && action.files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Archivos (${action.files.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lista de archivos (mostrar solo los primeros 5)
                action.files.take(5).forEach { file ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (action.files.size > 5) {
                    Text(
                        text = "... y ${action.files.size - 5} más",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Botones de acción
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechazar")
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = riskColor
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aprobar")
                }
            }
        }
    }
}

/**
 * Obtiene el ícono para el tipo de acción
 */
@Composable
fun getActionIcon(actionType: com.appspace.movil.model.ActionType) =
    when (actionType) {
        com.appspace.movil.model.ActionType.DELETE_DUPLICATES -> Icons.Default.ContentCopy
        com.appspace.movil.model.ActionType.DELETE_CACHE -> Icons.Default.DeleteSweep
        com.appspace.movil.model.ActionType.DELETE_OLD_DOWNLOADS -> Icons.Default.Download
        com.appspace.movil.model.ActionType.DELETE_LARGE_FILES -> Icons.Default.AttachFile
        com.appspace.movil.model.ActionType.DELETE_UNUSED_APK -> Icons.Default.Android
        com.appspace.movil.model.ActionType.ORGANIZE_PHOTOS -> Icons.Default.PhotoLibrary
        com.appspace.movil.model.ActionType.ORGANIZE_DOCUMENTS -> Icons.Default.Folder
        com.appspace.movil.model.ActionType.COMPRESS_FILES -> Icons.Default.ZipBox
        com.appspace.movil.model.ActionType.MOVE_TO_CLOUD -> Icons.Default.CloudUpload
        com.appspace.movil.model.ActionType.CUSTOM -> Icons.Default.AutoAwesome
    }
