package com.example.medysync

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicamentoDetalleActivity : AppCompatActivity() {

    private lateinit var tvTiempoRestante: TextView
    private var fechaFin: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var nombre: String = ""
    private var dosis: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamento_detalle)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener los datos del Intent
        nombre = intent.getStringExtra("nombre") ?: "No disponible"
        dosis = intent.getStringExtra("dosis") ?: "No disponible"
        fechaFin = intent.getLongExtra("fechaFin", 0L)

        // Log para verificar que los datos llegaron correctamente
        Log.d("MedicamentoDetalleActivity", "Datos recibidos: nombre=$nombre, dosis=$dosis, fechaFin=$fechaFin")

        // Actualizar la UI con los datos
        findViewById<TextView>(R.id.tvNombreMedicamento).text = nombre
        findViewById<TextView>(R.id.tvDosis).text = "Dosis: $dosis"
        tvTiempoRestante = findViewById(R.id.tvTiempoRestante)

        actualizarTiempo()
        handler.post(tiempoRunnable)

        // Eliminar medicamento
        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            eliminarMedicamento()
        }
    }

    private fun eliminarMedicamento() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios")
                .document(userId)
                .collection("medicamentos")
                .whereEqualTo("nombre", nombre)
                .whereEqualTo("dosis", dosis)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("usuarios")
                            .document(userId)
                            .collection("medicamentos")
                            .document(document.id)
                            .delete()
                    }
                    Toast.makeText(this, "✅ Medicamento eliminado", Toast.LENGTH_SHORT).show()
                    finish() // Regresar a la lista
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private val tiempoRunnable = object : Runnable {
        override fun run() {
            actualizarTiempo()
            handler.postDelayed(this, 1000)
        }
    }

    private fun actualizarTiempo() {
        val tiempoRestante = fechaFin - System.currentTimeMillis()
        val segundos = (tiempoRestante / 1000) % 60
        val minutos = (tiempoRestante / (1000 * 60)) % 60
        val horas = (tiempoRestante / (1000 * 60 * 60)) % 24
        val dias = (tiempoRestante / (1000 * 60 * 60 * 24))
        val meses = dias / 30

        tvTiempoRestante.text = if (tiempoRestante > 0) {
            "${meses}m ${dias % 30}d ${horas}h ${minutos}m ${segundos}s restantes"
        } else {
            "🛑 Tratamiento finalizado"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tiempoRunnable)
    }
}
