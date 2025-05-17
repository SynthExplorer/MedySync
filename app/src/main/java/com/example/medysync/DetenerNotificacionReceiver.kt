package com.example.medysync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DetenerNotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val idUnico = intent.getStringExtra("idUnico") ?: return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            idUnico.hashCode(),
            Intent(context, NotificacionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d("DetenerNotificacion", "⛔ Notificación cancelada para ID: $idUnico")
    }
}
