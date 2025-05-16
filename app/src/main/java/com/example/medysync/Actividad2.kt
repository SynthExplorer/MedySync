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


class Actividad2 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var frecuenciaHoras = 1 // valor inicial del slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad2)

        db = FirebaseFirestore.getInstance()
        crearCanalNotificacion()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val sliderFrecuencia = findViewById<Slider>(R.id.sliderFrecuencia)
        val tvFrecuencia = findViewById<TextView>(R.id.tvFrecuenciaSeleccionada)

        // 🟢 Escuchar cambios en el slider
        sliderFrecuencia.addOnChangeListener { _, value, _ ->
            frecuenciaHoras = value.toInt()
            tvFrecuencia.text = "Cada $frecuenciaHoras hora${if (frecuenciaHoras > 1) "s" else ""}"
        }

        val etNombreMedicamento = findViewById<EditText>(R.id.etNombreMedicamento)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val npHoras = findViewById<NumberPicker>(R.id.npHoras)
        val npDias = findViewById<NumberPicker>(R.id.npDias)
        val npSemanas = findViewById<NumberPicker>(R.id.npSemanas)
        val npMeses = findViewById<NumberPicker>(R.id.npMeses)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // Configuración NumberPickers
        npHoras.minValue = 0
        npHoras.maxValue = 23

        npDias.minValue = 0
        npDias.maxValue = 30

        npSemanas.minValue = 0
        npSemanas.maxValue = 4

        npMeses.minValue = 0
        npMeses.maxValue = 12

        btnGuardar.setOnClickListener {
            val nombre = etNombreMedicamento.text.toString().trim()
            val dosis = etDosis.text.toString().trim()

            if (nombre.isEmpty() || dosis.isEmpty()) {
                Toast.makeText(this, "❗Completa todos los campos ❗", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dias = npDias.value
            val semanas = npSemanas.value
            val meses = npMeses.value

            val duracionEnMilis =
                (dias * 24 * 60 * 60 * 1000L) +
                        (semanas * 7 * 24 * 60 * 60 * 1000L) +
                        (meses * 30L * 24 * 60 * 60 * 1000L)

            if (duracionEnMilis <= 0) {
                Toast.makeText(this, "⚠️ Selecciona una duración válida ⚠️", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaFin = System.currentTimeMillis() + duracionEnMilis
            val idUnico = UUID.randomUUID().toString()
            val frecuenciaTexto = "Cada ${frecuenciaHoras}h"
            val frecuenciaMillis = frecuenciaHoras * 60 * 60 * 1000L

            val medicamento = Medicamento(
                nombre = nombre,
                dosis = dosis,
                frecuencia = frecuenciaTexto,
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
            val canal = NotificationChannel(
                "canal_meds",
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recordar medicamentos"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun programarNotificacion(medicamento: Medicamento, frecuenciaMillis: Long) {
        val intent = Intent(this, NotificacionReceiver::class.java).apply {
            putExtra("nombre", medicamento.nombre)
            putExtra("dosis", medicamento.dosis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicamento.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = System.currentTimeMillis() + frecuenciaMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            frecuenciaMillis,
            pendingIntent
        )
    }
}
