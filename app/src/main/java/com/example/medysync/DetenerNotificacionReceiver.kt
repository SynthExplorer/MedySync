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
        val requestCode = intent.getIntExtra("pendingIntentRequestCode", 0)

        Log.d("DetenerNotificacion", "Cancelando alarma para medicamento id: $idUnico")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, NotificacionReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("DetenerNotificacion", "Alarma cancelada")
        }
    }
}
