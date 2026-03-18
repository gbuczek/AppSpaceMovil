package com.appspace.movil.model

/**
 * Mensaje para el chat con el agente IA
 */
data class ChatMessage(
    val id: Long = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actions: List<AgentAction> = emptyList(),
    val messageType: MessageType = MessageType.TEXT
)

/**
 * Tipos de mensaje
 */
enum class MessageType {
    TEXT,
    REPORT,
    ACTIONS,
    CONFIRMATION
}
