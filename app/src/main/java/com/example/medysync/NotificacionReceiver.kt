package com.example.medysync

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.medysync.R

class NotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: "Medicamento"
        val dosis = intent.getStringExtra("dosis") ?: ""
        val frecuenciaMillis = intent.getLongExtra("frecuenciaMillis", 0L)
        val medicamentoId = intent.getStringExtra("id") ?: ""

        // Construir notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication) // Asegúrate de tener este ícono
            .setContentTitle("Hora de tu medicamento")
            .setContentText("$nombre - Dosis: $dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())

        // Reprogramar próxima notificación si aún no ha pasado la fecha fin
        val fechaFin = intent.getLongExtra("fechaFin", 0L)
        if (System.currentTimeMillis() < fechaFin && frecuenciaMillis > 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextTrigger = System.currentTimeMillis() + frecuenciaMillis

            val intentNext = Intent(context, NotificacionReceiver::class.java).apply {
                putExtra("nombre", nombre)
                putExtra("dosis", dosis)
                putExtra("frecuenciaMillis", frecuenciaMillis)
                putExtra("id", medicamentoId)
                putExtra("fechaFin", fechaFin)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicamentoId.hashCode(),
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger,
                pendingIntent
            )
        }
    }
}
