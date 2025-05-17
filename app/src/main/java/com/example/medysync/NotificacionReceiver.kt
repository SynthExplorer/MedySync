package com.example.medysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: "Medicamento"
        val dosis = intent.getStringExtra("dosis") ?: ""

        val builder = NotificationCompat.Builder(context, "canal_meds")
            .setSmallIcon(R.drawable.ic_medication) // Asegúrate de tener este ícono
            .setContentTitle("Hora de tu medicamento")
            .setContentText("$nombre - Dosis: $dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build()) // Evita colisiones de ID
    }
}
