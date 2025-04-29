package com.example.medysync

import android.os.Bundle
import android.widget.Toast
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.android.identity.util.UUID

class Actividad2 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad2)

        db = FirebaseFirestore.getInstance()

        val etNombreMedicamento = findViewById<EditText>(R.id.etNombreMedicamento)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val npHoras = findViewById<NumberPicker>(R.id.npHoras)
        val npDias = findViewById<NumberPicker>(R.id.npDias)
        val npSemanas = findViewById<NumberPicker>(R.id.npSemanas)
        val npMeses = findViewById<NumberPicker>(R.id.npMeses)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // Configuración de NumberPickers
        npHoras.minValue = 0
        npHoras.maxValue = 24
        npHoras.value = 1

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

            val horas = npHoras.value
            val dias = npDias.value
            val semanas = npSemanas.value
            val meses = npMeses.value

            val duracionEnMilis =
                (horas * 60 * 60 * 1000L) +
                        (dias * 24 * 60 * 60 * 1000L) +
                        (semanas * 7 * 24 * 60 * 60 * 1000L) +
                        (meses * 30L * 24 * 60 * 60 * 1000L)

            if (duracionEnMilis <= 0) {
                Toast.makeText(this, "⚠️ Selecciona una duración válida ⚠️", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaFin = System.currentTimeMillis() + duracionEnMilis
            val idUnico = UUID.randomUUID().toString()
            val frecuencia = "" //Pendiente

            val medicamento = Medicamento(
                nombre = nombre,
                dosis = dosis,
                frecuencia = frecuencia,
                fechaFin = fechaFin,
                id = idUnico
            )

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                db.collection("usuarios")
                    .document(userId)
                    .collection("medicamentos")
                    .document(medicamento.id)
                    .set(medicamento)
                    .addOnSuccessListener {
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
}
