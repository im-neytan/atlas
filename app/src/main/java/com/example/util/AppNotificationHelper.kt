package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

class AppNotificationHelper private constructor(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações operacionais do Bl4ck System"
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

    fun notificarReceberPedido(displayId: String, megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarReceberPedido.value || !canPostNotification()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Novo Pedido Recebido ($displayId)")
            .setContentText("Transferência de $megas MB para $numero agendada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID_PEDIDO, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarIniciarTransferencia(displayId: String, megas: String, numero: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarIniciarTransferencia.value || !canPostNotification()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Iniciando Transferência ($displayId)")
            .setContentText("Enviando $megas MB para $numero via *162#...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID_INICIO, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarFinalizarTransferencia(displayId: String, megas: String, numero: String, resposta: String, status: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarFinalizarTransferencia.value || !canPostNotification()) return

        val titulo = when (status) {
            "CONCLUIDO" -> "✅ Transferência Concluída ($displayId)"
            "ANALISE" -> "⚠️ Requer Análise ($displayId)"
            else -> "❌ Falha na Transferência ($displayId)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (status == "CONCLUIDO") android.R.drawable.stat_sys_upload_done else android.R.drawable.stat_notify_error)
            .setContentTitle(titulo)
            .setContentText("$megas MB para $numero: $resposta")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$megas MB para $numero\nResposta: $resposta"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID_FIM, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun notificarErro(titulo: String, mensagem: String) {
        val settings = AppSettingsManager.getInstance(context)
        if (!settings.notificarErro.value || !canPostNotification()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID_ERRO, builder.build())
        } catch (_: SecurityException) {
        }
    }

    companion object {
        private const val CHANNEL_ID = "bl4ck_transfers_channel"
        private const val CHANNEL_NAME = "Transferências e Alertas"

        private const val NOTIFICATION_ID_PEDIDO = 1001
        private const val NOTIFICATION_ID_INICIO = 1002
        private const val NOTIFICATION_ID_FIM = 1003
        private const val NOTIFICATION_ID_ERRO = 1004

        @Volatile
        private var instance: AppNotificationHelper? = null

        fun getInstance(context: Context): AppNotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: AppNotificationHelper(context).also { instance = it }
            }
        }
    }
}
