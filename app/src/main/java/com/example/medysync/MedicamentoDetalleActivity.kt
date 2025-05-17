package com.example.medysync

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicamentoDetalleActivity : AppCompatActivity() {

    private lateinit var tvTiempoRestante: TextView
    private lateinit var tvProximaDosis: TextView
    private var fechaFin: Long = 0L
    private var countDownTimer: CountDownTimer? = null
    private var countDownDosis: CountDownTimer? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var nombre: String = ""
    private var dosis: String = ""
    private var frecuenciaHoras: Int = 1

    private var ultimaToma: Long? = null  // Nuevo campo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamento_detalle)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nombre = intent.getStringExtra("nombre") ?: "No disponible"
        dosis = intent.getStringExtra("dosis") ?: "No disponible"
        fechaFin = intent.getLongExtra("fechaFin", 0L)
        frecuenciaHoras = intent.getIntExtra("frecuenciaHoras", 1)

        tvTiempoRestante = findViewById(R.id.tvTiempoRestante)
        tvProximaDosis = findViewById(R.id.tvProximaDosis)

        findViewById<TextView>(R.id.tvNombreMedicamento).text = nombre
        findViewById<TextView>(R.id.tvDosis).text = dosis

        iniciarCuentaRegresiva()
        obtenerUltimaTomaYMostrarProximaDosis()

        findViewById<Button>(R.id.btnMarcarComoTomado).setOnClickListener {
            reiniciarContadorDosisDesdeAhora()
        }

        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            eliminarMedicamento()
        }
    }

    private fun iniciarCuentaRegresiva() {
        val tiempoRestante = fechaFin - System.currentTimeMillis()
        if (tiempoRestante > 0) {
            countDownTimer = object : CountDownTimer(tiempoRestante, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val segundos = (millisUntilFinished / 1000) % 60
                    val minutos = (millisUntilFinished / (1000 * 60)) % 60
                    val horas = (millisUntilFinished / (1000 * 60 * 60)) % 24
                    val dias = (millisUntilFinished / (1000 * 60 * 60 * 24))
                    val meses = dias / 30
                    val diasRestantes = dias % 30

                    tvTiempoRestante.text =
                        "${meses}m ${diasRestantes}d ${horas}h ${minutos}m ${segundos}s "
                }

                override fun onFinish() {
                    tvTiempoRestante.text = "🛑 Tratamiento finalizado"
                }
            }.start()
        } else {
            tvTiempoRestante.text = "🛑 Tratamiento finalizado"
        }
    }

    private fun obtenerUltimaTomaYMostrarProximaDosis() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("medicamentos")
            .whereEqualTo("nombre", nombre)
            .whereEqualTo("dosis", dosis)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    ultimaToma = document.getLong("ultimaToma") ?: document.getLong("fechaCreacion") ?: System.currentTimeMillis()
                    mostrarTiempoProximaDosis(ultimaToma!!)
                } else {
                    // Si no hay documento, usar fecha actual como última toma
                    ultimaToma = System.currentTimeMillis()
                    mostrarTiempoProximaDosis(ultimaToma!!)
                }
            }
            .addOnFailureListener {
                // En caso de error, usar fecha actual
                ultimaToma = System.currentTimeMillis()
                mostrarTiempoProximaDosis(ultimaToma!!)
            }
    }

    private fun mostrarTiempoProximaDosis(ultimaToma: Long) {
        val frecuenciaMillis = frecuenciaHoras * 60 * 60 * 1000L
        val ahora = System.currentTimeMillis()
        val tiempoTranscurrido = ahora - ultimaToma
        val tiempoRestante = frecuenciaMillis - (tiempoTranscurrido % frecuenciaMillis)

        countDownDosis?.cancel()
        countDownDosis = object : CountDownTimer(tiempoRestante, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val horas = millisUntilFinished / (1000 * 60 * 60)
                val minutos = (millisUntilFinished / (1000 * 60)) % 60
                val segundos = (millisUntilFinished / 1000) % 60

                tvProximaDosis.text = "${horas}h ${minutos}m ${segundos}s"
            }

            override fun onFinish() {
                tvProximaDosis.text = "💊 ¡Es hora de tu próxima dosis!"
            }
        }.start()

        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaExacta = formato.format(Date(ahora + tiempoRestante))
        Toast.makeText(this, "🕐 Próxima dosis a las $horaExacta", Toast.LENGTH_SHORT).show()
    }

    private fun reiniciarContadorDosisDesdeAhora() {
        countDownDosis?.cancel()

        val frecuenciaMillis = frecuenciaHoras * 60 * 60 * 1000L

        countDownDosis = object : CountDownTimer(frecuenciaMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val horas = millisUntilFinished / (1000 * 60 * 60)
                val minutos = (millisUntilFinished / (1000 * 60)) % 60
                val segundos = (millisUntilFinished / 1000) % 60

                tvProximaDosis.text = "${horas}h ${minutos}m ${segundos}s"
            }

            override fun onFinish() {
                tvProximaDosis.text = "💊 ¡Es hora de tu próxima dosis!"
            }
        }.start()

        val ahora = System.currentTimeMillis()
        actualizarUltimaTomaEnFirestore(ahora)
        Toast.makeText(this, "✅ Dosis marcada como tomada", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarUltimaTomaEnFirestore(timestamp: Long) {
        val userId = auth.currentUser?.uid ?: return

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
                        .update("ultimaToma", timestamp)
                }
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
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownDosis?.cancel()
    }
}
