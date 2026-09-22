package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerenciador de Notificações do Bl4ck System.
 * Garante que cada notificação seja única por evento e por pedido, eliminando loops
 * de repetição e disparos duplicados.
 */
class AppNotificationHelper private constructor(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    // Cache de deduplicação para prevenir loops e múltiplos disparos da mesma notificação
    private val sentEventCache = ConcurrentHashMap<String, Long>()
    private val DEBOUNCE_WINDOW_MS = 6000L

    init {
        createNotificationChannels()
    }

    private fun isDuplicateEvent(eventKey: String): Boolean {
        val now = System.currentTimeMillis()
        val lastSent = sentEventCache[eventKey] ?: 0L
        if (now - lastSent < DEBOUNCE_WINDOW_MS) {
            return true
        }
        sentEventCache[eventKey] = now
        // Limpa chaves antigas periodicamente para evitar crescimento de memória
        if (sentEventCache.size > 100) {
            val iterator = sentEventCache.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > 60_000L) {
                    iterator.remove()
                }
            }
        }
        return false
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações operacionais de transferências"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun canPostNotification(): Boolean {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificacoesGerais.value) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    private fun getOrderIdHash(displayId: String): Int {
        return Math.abs(displayId.hashCode()) % 100000
    }

    fun notificarReceberPedido(displayId: String, megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarReceberPedido.value || !canPostNotification()) return

        val dedupeKey = "PEDIDO_${displayId}_${megas}_$numero"
        if (isDuplicateEvent(dedupeKey)) return

        val notifId = NOTIFICATION_BASE_PEDIDO + getOrderIdHash(displayId)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Novo Pedido Recebido ($displayId)")
            .setContentText("Transferência de $megas MB para $numero agendada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarIniciarTransferencia(displayId: String, megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarIniciarTransferencia.value || !canPostNotification()) return

        val dedupeKey = "INICIO_${displayId}"
        if (isDuplicateEvent(dedupeKey)) return

        val notifId = NOTIFICATION_BASE_INICIO + getOrderIdHash(displayId)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Iniciando Transferência ($displayId)")
            .setContentText("Enviando $megas MB para $numero via *162#...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarFinalizarTransferencia(displayId: String, megas: String, numero: String, resposta: String, status: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarFinalizarTransferencia.value || !canPostNotification()) return

        val dedupeKey = "FIM_${displayId}_$status"
        if (isDuplicateEvent(dedupeKey)) return

        val notifId = NOTIFICATION_BASE_FIM + getOrderIdHash(displayId)

        val titulo = when (status) {
            "CONCLUIDO" -> "Transferência Concluída ($displayId)"
            "ANALISE" -> "Requer Análise ($displayId)"
            else -> "Falha na Transferência ($displayId)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (status == "CONCLUIDO") android.R.drawable.stat_sys_upload_done else android.R.drawable.stat_notify_error)
            .setContentTitle(titulo)
            .setContentText("$megas MB para $numero: $resposta")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$megas MB para $numero\nResposta da operadora: $resposta"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarErro(titulo: String, mensagem: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarErro.value || !canPostNotification()) return

        val dedupeKey = "ERRO_${titulo}_$mensagem"
        if (isDuplicateEvent(dedupeKey)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID_ERRO, builder.build())
        } catch (_: SecurityException) {
        }
    }

    companion object {
        private const val CHANNEL_ID = "bl4ck_transfers_channel"
        private const val CHANNEL_NAME = "Transferências e Alertas"

        private const val NOTIFICATION_BASE_PEDIDO = 10000
        private const val NOTIFICATION_BASE_INICIO = 20000
        private const val NOTIFICATION_BASE_FIM = 30000
        private const val NOTIFICATION_ID_ERRO = 99999

        @Volatile
        private var instance: AppNotificationHelper? = null

        fun getInstance(context: Context): AppNotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: AppNotificationHelper(context).also { instance = it }
            }
        }
    }
}
