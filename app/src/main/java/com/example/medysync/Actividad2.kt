package com.example.medysync

import android.os.Bundle
import android.widget.Toast
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

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

        // Configuración de los NumberPickers
        npHoras.minValue = 0
        npHoras.maxValue = 24
        npHoras.value = 1

        npDias.minValue = 0
        npDias.maxValue = 7
        npDias.value = 1

        npSemanas.minValue = 0
        npSemanas.maxValue = 4
        npSemanas.value = 1

        npMeses.minValue = 0
        npMeses.maxValue = 12
        npMeses.value = 1

        // Función para guardar medicamento
        btnGuardar.setOnClickListener {
            Log.d("Actividad2", "Botón Guardar presionado")

            val nombreMedicamento = etNombreMedicamento.text.toString().trim()
            val dosis = etDosis.text.toString().trim()
            val frecuencia = "${npHoras.value}h, ${npDias.value}d, ${npSemanas.value}s, ${npMeses.value}m"

            // Cálculo del total en milisegundos
            val totalMillis = (
                    npHoras.value * 60 * 60 * 1000L +
                            npDias.value * 24 * 60 * 60 * 1000L +
                            npSemanas.value * 7 * 24 * 60 * 60 * 1000L +
                            npMeses.value * 30 * 24 * 60 * 60 * 1000L // Aproximando 1 mes a 30 días
                    )

            // Fecha de finalización
            val fechaFin = System.currentTimeMillis() + totalMillis

            // Objeto medicamento para almacenar
            val medicamento = hashMapOf(
                "nombre" to nombreMedicamento,
                "dosis" to dosis,
                "frecuencia" to frecuencia,
                "fechaFin" to fechaFin
            )

            // Validación de campos
            if (nombreMedicamento.isNotEmpty() && dosis.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                // Verifica si el usuario está autenticado
                if (userId != null) {
                    db.collection("usuarios")
                        .document(userId)
                        .collection("medicamentos")
                        .add(medicamento)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "✅ Medicamento guardado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Limpiar campos después de guardar
                            etNombreMedicamento.text.clear()
                            etDosis.text.clear()
                            npHoras.value = 0
                            npDias.value = 0
                            npSemanas.value = 0
                            npMeses.value = 0
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIRESTORE", "Error al guardar", e)
                            Toast.makeText(
                                this,
                                "❌ Error al guardar: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "⚠️ Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
