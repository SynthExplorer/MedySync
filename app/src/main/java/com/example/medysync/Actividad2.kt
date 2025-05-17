package com.example.medysync

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

const val CHANNEL_ID = "canal_medicamentos"

class Actividad2 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var frecuenciaHoras = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad2)

        db = FirebaseFirestore.getInstance()
        crearCanalNotificacion()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }


        val sliderFrecuencia = findViewById<Slider>(R.id.sliderFrecuencia)
        val tvFrecuencia = findViewById<TextView>(R.id.tvFrecuenciaSeleccionada)

        sliderFrecuencia.addOnChangeListener { _, value, _ ->
            frecuenciaHoras = value.toInt()
            tvFrecuencia.text = "Cada $frecuenciaHoras hora${if (frecuenciaHoras > 1) "s" else ""}"
        }

        val etNombre = findViewById<EditText>(R.id.etNombreMedicamento)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val npHoras = findViewById<NumberPicker>(R.id.npHoras)
        val npDias = findViewById<NumberPicker>(R.id.npDias)
        val npSemanas = findViewById<NumberPicker>(R.id.npSemanas)
        val npMeses = findViewById<NumberPicker>(R.id.npMeses)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        npHoras.minValue = 0; npHoras.maxValue = 23
        npDias.minValue = 0; npDias.maxValue = 30
        npSemanas.minValue = 0; npSemanas.maxValue = 4
        npMeses.minValue = 0; npMeses.maxValue = 12

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dosis = etDosis.text.toString().trim()

            if (nombre.isEmpty() || dosis.isEmpty()) {
                Toast.makeText(this, "❗Completa todos los campos ❗", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duracionMilis =
                npHoras.value * 60 * 60 * 1000L +
                        npDias.value * 24 * 60 * 60 * 1000L +
                        npSemanas.value * 7 * 24 * 60 * 60 * 1000L +
                        npMeses.value * 30L * 24 * 60 * 60 * 1000L

            if (duracionMilis <= 0) {
                Toast.makeText(this, "⚠️ Selecciona una duración válida ⚠️", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaFin = System.currentTimeMillis() + duracionMilis
            val idUnico = UUID.randomUUID().toString()
            val frecuenciaMillis = frecuenciaHoras * 60 * 60 * 1000L

            val medicamento = Medicamento(
                nombre = nombre,
                dosis = dosis,
                frecuencia = "Cada ${frecuenciaHoras}h",
                frecuenciaHoras = frecuenciaHoras,
                fechaFin = fechaFin,
                id = idUnico,
                fechaCreacion = System.currentTimeMillis()
            )

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                db.collection("usuarios")
                    .document(userId)
                    .collection("medicamentos")
                    .document(medicamento.id)
                    .set(medicamento)
                    .addOnSuccessListener {
                        programarNotificacion(medicamento, frecuenciaMillis)
                        Toast.makeText(this, "✅ Medicamento guardado", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, Actividad1::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearCanalNotificacion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun programarNotificacion(medicamento: Medicamento, frecuenciaMillis: Long) {
        val intent = Intent(this, NotificacionReceiver::class.java).apply {
            putExtra("nombre", medicamento.nombre)
            putExtra("dosis", medicamento.dosis)
            putExtra("frecuenciaMillis", frecuenciaMillis)
            putExtra("id", medicamento.id)
            putExtra("fechaFin", medicamento.fechaFin)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicamento.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = System.currentTimeMillis() + frecuenciaMillis

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)

        // Alarma para detener la notificación al llegar a la fechaFin
        val detenerIntent = Intent(this, DetenerNotificacionReceiver::class.java).apply {
            putExtra("idUnico", medicamento.id)
            putExtra("pendingIntentRequestCode", medicamento.id.hashCode())
        }

        val detenerPendingIntent = PendingIntent.getBroadcast(
            this,
            ("cancel_${medicamento.id}").hashCode(),
            detenerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            medicamento.fechaFin,
            detenerPendingIntent
        )

        Log.d("Actividad2", "Notificación programada para ${triggerAtMillis}, detener en ${medicamento.fechaFin}")
    }
}
