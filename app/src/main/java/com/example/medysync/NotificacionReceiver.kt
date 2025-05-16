package com.example.medysync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: "Medicamento"
        val dosis = intent.getStringExtra("dosis") ?: ""

        val builder = NotificationCompat.Builder(context, "meds_channel")
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle("Recordatorio: $nombre")
            .setContentText("Es hora de tomar $nombre - Dosis: $dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(nombre.hashCode(), builder.build())
        }
    }
}
