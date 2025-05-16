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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamento_detalle)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nombre = intent.getStringExtra("nombre") ?: "No disponible"
        dosis = intent.getStringExtra("dosis") ?: "No disponible"
        fechaFin = intent.getLongExtra("fechaFin", 0L)
        frecuenciaHoras = intent.getIntExtra("frecuenciaHoras", 1)

        val fechaCreacion = intent.getLongExtra("fechaCreacion", 0L)


        findViewById<TextView>(R.id.tvNombreMedicamento).text = nombre
        findViewById<TextView>(R.id.tvDosis).text = "Dosis: $dosis"
        tvTiempoRestante = findViewById(R.id.tvTiempoRestante)
        tvProximaDosis = findViewById(R.id.tvProximaDosis)

        iniciarCuentaRegresiva()
        mostrarTiempoProximaDosis(fechaCreacion)

        val btnMarcarComoTomado = findViewById<Button>(R.id.btnMarcarComoTomado)
        btnMarcarComoTomado.setOnClickListener {
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
                        "${meses}m ${diasRestantes}d ${horas}h ${minutos}m ${segundos}s restantes"
                }

                override fun onFinish() {
                    tvTiempoRestante.text = "🛑 Tratamiento finalizado"
                }
            }.start()
        } else {
            tvTiempoRestante.text = "🛑 Tratamiento finalizado"
        }
    }

    private fun mostrarTiempoProximaDosis(fechaCreacion: Long) {
        val frecuenciaMillis = frecuenciaHoras * 60 * 60 * 1000L
        val ahora = System.currentTimeMillis()
        val tiempoTranscurrido = ahora - fechaCreacion
        val tiempoRestante = frecuenciaMillis - (tiempoTranscurrido % frecuenciaMillis)
        val proximaDosisEn = ahora + tiempoRestante

        countDownDosis?.cancel()
        countDownDosis = object : CountDownTimer(tiempoRestante, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val horas = millisUntilFinished / (1000 * 60 * 60)
                val minutos = (millisUntilFinished / (1000 * 60)) % 60
                val segundos = (millisUntilFinished / 1000) % 60

                tvProximaDosis.text = "Próxima dosis en: ${horas}h ${minutos}m ${segundos}s"
            }

            override fun onFinish() {
                tvProximaDosis.text = "💊 ¡Es hora de tu próxima dosis!"
            }
        }.start()

        // Mostrar hora exacta de la próxima dosis (opcional)
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaExacta = formato.format(Date(proximaDosisEn))
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

                tvProximaDosis.text = "Próxima dosis en: ${horas}h ${minutos}m ${segundos}s"
            }

            override fun onFinish() {
                tvProximaDosis.text = "💊 ¡Es hora de tu próxima dosis!"
            }
        }.start()

        registrarTomaEnHistorial()

        Toast.makeText(this, "✅ Dosis marcada como tomada", Toast.LENGTH_SHORT).show()
    }

    private fun registrarTomaEnHistorial() {
        val userId = auth.currentUser?.uid ?: return

        val historialData = hashMapOf(
            "nombre" to nombre,
            "dosis" to dosis,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("usuarios")
            .document(userId)
            .collection("historial_tomas")
            .add(historialData)
            .addOnSuccessListener {
                Log.d("Firestore", "✅ Historial registrado")
            }
            .addOnFailureListener {
                Log.e("Firestore", "❌ Error al registrar historial", it)
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
