package com.example.medysync

import androidx.appcompat.app.AppCompatActivity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CrearCitaActivity : AppCompatActivity() {

    private lateinit var etTitulo: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etFecha: TextInputEditText
    private lateinit var etHora: TextInputEditText
    private lateinit var etFrecuencia: AutoCompleteTextView
    private lateinit var btnGuardar: MaterialButton

    private var calendar = Calendar.getInstance()
    private lateinit var db: FirebaseFirestore

    private val opcionesFrecuencia = arrayOf("Una vez", "Diaria", "Semanal")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cita)

        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etFrecuencia = findViewById(R.id.etFrecuencia)
        btnGuardar = findViewById(R.id.btnGuardar)

        db = FirebaseFirestore.getInstance()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesFrecuencia)
        etFrecuencia.setAdapter(adapter)


        etFrecuencia.setOnClickListener {
            etFrecuencia.showDropDown()
        }

        etFecha.setOnClickListener { seleccionarFecha() }
        etFecha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                seleccionarFecha()
            }
        }

        etHora.setOnClickListener { seleccionarHora() }
        etHora.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                seleccionarHora()
            }
        }

        btnGuardar.setOnClickListener { guardarCita() }
    }

    private fun seleccionarFecha() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etFecha.setText(formato.format(calendar.time))
        }, year, month, day).show()
    }

    private fun seleccionarHora() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, m)
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            etHora.setText(formato.format(calendar.time))
        }, hour, minute, true).show()
    }

    private fun guardarCita() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val hora = etHora.text.toString().trim()
        val frecuencia = etFrecuencia.text.toString().trim()

        if (titulo.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() || hora.isEmpty() || frecuencia.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val citaId = db.collection("usuarios").document(userId).collection("citas").document().id
        val cita = Cita(citaId, titulo, descripcion, fecha, hora, frecuencia)

        db.collection("usuarios")
            .document(userId)
            .collection("citas")
            .document(citaId)
            .set(cita)
            .addOnSuccessListener {
                programarNotificacion(cita)
                guardarEnHistorialCitas(cita)
                Toast.makeText(this, "Cita guardada y notificación programada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar cita", Toast.LENGTH_SHORT).show()
            }
    }
    private fun programarNotificacion(cita: Cita) {
        val intent = Intent(this, CitaNotificacionReceiver::class.java).apply {
            putExtra("titulo", cita.titulo)
            putExtra("descripcion", cita.descripcion)
            putExtra("fecha", cita.fecha)
            putExtra("hora", cita.hora)
        }

        val requestCode = cita.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent) // Cancelar notificación anterior si existe

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHora: Date = try {
            sdf.parse("${cita.fecha} ${cita.hora}") ?: throw IllegalArgumentException()
        } catch (e: Exception) {
            Toast.makeText(this, "Fecha u hora inválida", Toast.LENGTH_SHORT).show()
            return
        }

        when (cita.frecuencia) {
            "Una vez" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaHora.time, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, fechaHora.time, pendingIntent)
                }
            }
            "Diaria" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    fechaHora.time,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            "Semanal" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    fechaHora.time,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
        }
    }
    private fun guardarEnHistorialCitas(cita: Cita) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val historialId = db.collection("usuarios").document(userId)
                .collection("historial_citas").document().id

            val historial = hashMapOf(
                "id" to historialId,
                "titulo" to cita.titulo,
                "descripcion" to cita.descripcion,
                "fecha" to cita.fecha,
                "hora" to cita.hora,
                "frecuencia" to cita.frecuencia,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("usuarios")
                .document(userId)
                .collection("historial_citas")
                .document(historialId)
                .set(historial)
                .addOnSuccessListener {
                    Log.d("HistorialCita", "Historial guardado con éxito")
                }
                .addOnFailureListener {
                    Log.e("HistorialCita", "Error al guardar historial: ${it.message}")
                }
        }
    }


}

