package com.example.medysync

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Button
import java.io.File
import java.io.FileOutputStream


class InformeActivity : AppCompatActivity() {

    private lateinit var tvInforme: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informe)

        tvInforme = findViewById(R.id.tvInforme)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        generarInformeCompleto()
        val btnGenerarPdf = findViewById<Button>(R.id.btnGenerarPdf)
        btnGenerarPdf.setOnClickListener {
            generarPdf(tvInforme.text.toString())
        }

    }

    private fun generarInformeCompleto() {
        val userId = auth.currentUser?.uid ?: return

        val historialMedicamentos = mutableListOf<String>()
        val historialCitas = mutableListOf<String>()

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("usuarios")
            .document(userId)
            .collection("historial_tomas")
            .get()
            .addOnSuccessListener { tomas ->
                for (document in tomas) {
                    val nombre = document.getString("nombre") ?: "Desconocido"
                    val dosis = document.getString("dosis") ?: "-"
                    val timestamp = document.get("timestamp")
                    val fecha = when (timestamp) {
                        is Number -> formatoFecha.format(Date(timestamp.toLong()))
                        is com.google.firebase.Timestamp -> formatoFecha.format(timestamp.toDate())
                        else -> "Fecha desconocida"
                    }
                    historialMedicamentos.add("$nombre ($dosis)\nTomado: $fecha\n")
                }

                db.collection("usuarios")
                    .document(userId)
                    .collection("historial_citas")
                    .get()
                    .addOnSuccessListener { citas ->
                        for (document in citas) {
                            val titulo = document.getString("titulo") ?: "Sin título"
                            val descripcion = document.getString("descripcion") ?: "-"
                            val fecha = document.getString("fecha") ?: "Desconocida"
                            val hora = document.getString("hora") ?: "Desconocida"
                            val frecuencia = document.getString("frecuencia") ?: "No definida"
                            val timestamp = document.get("timestamp")
                            val fechaCreacion = when (timestamp) {
                                is Number -> formatoFecha.format(Date(timestamp.toLong()))
                                is com.google.firebase.Timestamp -> formatoFecha.format(timestamp.toDate())
                                else -> "Desconocida"
                            }
                            historialCitas.add("$titulo\nDescripción: $descripcion\nFecha: $fecha $hora\nFrecuencia: $frecuencia\nCreado: $fechaCreacion\n")
                        }

                        val informe = StringBuilder()
                        informe.append("INFORME DE USO\n\n")

                        if (historialMedicamentos.isEmpty()) {
                            informe.append("No hay historial de tomas.\n\n")
                        } else {
                            informe.append("🔹 HISTORIAL DE MEDICAMENTOS:\n")
                            historialMedicamentos.sorted().forEach {
                                informe.append("$it\n")
                            }
                        }

                        if (historialCitas.isEmpty()) {
                            informe.append("No hay historial de citas.\n")
                        } else {
                            informe.append("🔹 HISTORIAL DE CITAS MÉDICAS:\n")
                            historialCitas.sorted().forEach {
                                informe.append("$it\n")
                            }
                        }

                        tvInforme.text = informe.toString()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Error al obtener historial de citas", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al obtener historial de medicamentos", Toast.LENGTH_SHORT).show()
            }
    }
    private fun generarPdf(texto: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val x = 40
        var y = 50
        val lineHeight = 20
        val maxWidth = pageInfo.pageWidth - 2 * x

        val lines = texto.split("\n")
        for (line in lines) {
            var currentLine = line
            while (paint.measureText(currentLine) > maxWidth) {
                val cutIndex = paint.breakText(currentLine, true, maxWidth.toFloat(), null)
                canvas.drawText(currentLine.substring(0, cutIndex), x.toFloat(), y.toFloat(), paint)
                y += lineHeight
                currentLine = currentLine.substring(cutIndex)
            }
            canvas.drawText(currentLine, x.toFloat(), y.toFloat(), paint)
            y += lineHeight
        }

        pdfDocument.finishPage(page)

        val dir = File(getExternalFilesDir(null), "Informes")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "Informe_MedySync.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "✅ PDF guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

}
