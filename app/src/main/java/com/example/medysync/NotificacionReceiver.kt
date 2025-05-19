package com.example.medysync

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.medysync.R
import java.util.Date

class NotificacionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val nombre = intent.getStringExtra("nombre") ?: return
        val dosis = intent.getStringExtra("dosis") ?: return
        val frecuenciaMillis = intent.getLongExtra("frecuenciaMillis", -1L)
        val id = intent.getStringExtra("id") ?: return
        val fechaFin = intent.getLongExtra("fechaFin", 0L)


        Log.d("NotificacionReceiver", "Notificación recibida para: $nombre, ID: $id")

        val now = System.currentTimeMillis()

        mostrarNotificacion(context, nombre, dosis, id.hashCode())

        if (now < fechaFin && frecuenciaMillis > 0) {
            val siguienteTrigger = now + frecuenciaMillis

            if (siguienteTrigger < fechaFin) {
                val nuevoIntent = Intent(context, NotificacionReceiver::class.java).apply {
                    putExtra("nombre", nombre)
                    putExtra("dosis", dosis)
                    putExtra("frecuenciaMillis", frecuenciaMillis)
                    putExtra("id", id)
                    putExtra("fechaFin", fechaFin)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    id.hashCode(),
                    nuevoIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, siguienteTrigger, pendingIntent)
                            Log.d("NotificacionReceiver", "Próxima alarma programada para: ${Date(siguienteTrigger)}")
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, siguienteTrigger, pendingIntent)
                            Log.d("NotificacionReceiver", "Próxima alarma aproximada para: ${Date(siguienteTrigger)}")
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, siguienteTrigger, pendingIntent)
                        Log.d("NotificacionReceiver", "Próxima alarma programada para: ${Date(siguienteTrigger)}")
                    }
                } catch (e: Exception) {
                    Log.e("NotificacionReceiver", "Error al programar alarma: ${e.message}")
                    // Fallback a set normal
                    alarmManager.set(AlarmManager.RTC_WAKEUP, siguienteTrigger, pendingIntent)
                }
            } else {
                Log.d("NotificacionReceiver", "No se programa más notificaciones porque se alcanzará la fecha fin")
            }
        } else {
            Log.d("NotificacionReceiver", "Tratamiento finalizado, no se programan más notificaciones")
        }
    }

    private fun mostrarNotificacion(context: Context, nombre: String, dosis: String, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un canal de notificación para API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Recordatorios de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recordatorios de medicamentos"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app cuando se hace clic en la notificación
        val intent = Intent(context, Actividad1::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción para marcar como tomado
        val tomarIntent = Intent(context, TomarMedicamentoReceiver::class.java).apply {
            putExtra("id", notificationId.toString())
            putExtra("nombre", nombre)
            putExtra("dosis", dosis)
        }

        val tomarPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000, // ID diferente para no sobreescribir otros PendingIntents
            tomarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación con estilo y acciones
        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este icono en drawable
            .setContentTitle("💊 Hora de tu medicamento")
            .setContentText("$nombre - $dosis")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Es hora de tomar tu medicación:\n✅ $nombre\n💊 Dosis: $dosis"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "Tomar ahora", tomarPendingIntent)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            .build()

        // Mostrar la notificación
        try {
            notificationManager.notify(notificationId, notification)
            Log.d("NotificacionReceiver", "Notificación mostrada con ID: $notificationId")
        } catch (e: Exception) {
            Log.e("NotificacionReceiver", "Error al mostrar notificación: ${e.message}")
        }
    }
}

