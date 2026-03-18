package com.appspace.movil.model

/**
 * Acción sugerida por el agente IA
 */
data class AgentAction(
    val id: String = System.currentTimeMillis().toString(),
    val type: ActionType,
    val title: String,
    val description: String,
    val files: List<FileItem> = emptyList(),
    val spaceToFree: Long = 0,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val isApproved: Boolean = false,
    val isExecuted: Boolean = false,
    val recommendation: String = ""
) {
    val formattedSpaceToFree: String
        get() = FileItem.formatSize(spaceToFree)
}

/**
 * Tipos de acciones que puede realizar el agente
 */
enum class ActionType {
    DELETE_DUPLICATES,
    DELETE_CACHE,
    DELETE_OLD_DOWNLOADS,
    DELETE_LARGE_FILES,
    DELETE_UNUSED_APK,
    ORGANIZE_PHOTOS,
    ORGANIZE_DOCUMENTS,
    COMPRESS_FILES,
    MOVE_TO_CLOUD,
    CUSTOM
}

/**
 * Nivel de riesgo de la acción
 */
enum class RiskLevel {
    LOW,      // Seguro, recomendado
    MEDIUM,   // Requiere atención
    HIGH      // Requiere confirmación explícita
}
