package com.example.medysync

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DetenerNotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val idUnico = intent.getStringExtra("idUnico") ?: return
        val requestCode = intent.getIntExtra("pendingIntentRequestCode", 0)

        Log.d("DetenerNotificacion", "Cancelando alarma para medicamento id: $idUnico, requestCode: $requestCode")

        // Reconstruir el Intent con los mismos extras que el original
        val notificacionIntent = Intent(context, NotificacionReceiver::class.java).apply {
            putExtra("id", idUnico)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Intentar cancelar con PendingIntent.FLAG_NO_CREATE para verificar si existe
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notificacionIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("DetenerNotificacion", "Alarma cancelada exitosamente")
            } else {
                // Intentar con un nuevo PendingIntent igual al que se usó para programar
                val newPendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    notificacionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(newPendingIntent)
                newPendingIntent.cancel()
                Log.d("DetenerNotificacion", "Alarma cancelada con nuevo PendingIntent")
            }

            // Cancelar también la notificación por si está visible
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(requestCode)

        } catch (e: Exception) {
            Log.e("DetenerNotificacion", "Error al cancelar alarma: ${e.message}")
        }
    }
}