package com.example.medysync

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class InformeActivity : AppCompatActivity() {

    private lateinit var tvInforme: TextView
    private lateinit var btnGenerarPdf: Button
    private lateinit var btnCompartirPdf: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var pdfFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informe)

        tvInforme = findViewById(R.id.tvInforme)
        btnGenerarPdf = findViewById(R.id.btnGenerarPdf)
        btnCompartirPdf = findViewById(R.id.btnCompartirPdf)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        generarInformeCompleto()

        btnGenerarPdf.setOnClickListener {
            guardarInformeComoPdf()
        }

        btnCompartirPdf.setOnClickListener {
            if (::pdfFile.isInitialized && pdfFile.exists()) {
                compartirPdf(pdfFile)
            } else {
                Toast.makeText(this, "Primero genera y guarda el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generarInformeCompleto() {
        tvInforme.text = "Generando informe..."
        val userId = auth.currentUser?.uid ?: return

        val builder = StringBuilder()

        db.collection("usuarios").document(userId).collection("historial_tomas")
            .get()
            .addOnSuccessListener { docsMedicamentos ->
                builder.append("Historial de Medicamentos:\n\n")
                for (doc in docsMedicamentos) {
                    val nombre = doc.getString("nombre") ?: "N/A"
                    val dosis = doc.getString("dosis") ?: "N/A"
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()
                    val fechaStr = if (timestamp != null) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp)
                    } else "N/A"
                    builder.append("- $nombre, Dosis: $dosis, Fecha: $fechaStr\n")
                }

                db.collection("usuarios").document(userId).collection("historial_citas")
                    .get()
                    .addOnSuccessListener { docsCitas ->
                        builder.append("\nHistorial de Citas Médicas:\n\n")
                        for (doc in docsCitas) {
                            val titulo = doc.getString("titulo") ?: "N/A"
                            val descripcion = doc.getString("descripcion") ?: "N/A"
                            val fecha = doc.getString("fecha") ?: "N/A"
                            val hora = doc.getString("hora") ?: "N/A"
                            builder.append("- $titulo, $descripcion, Fecha: $fecha, Hora: $hora\n")
                        }

                        tvInforme.text = builder.toString()
                    }
                    .addOnFailureListener {
                        tvInforme.text = "Error al obtener historial de citas: ${it.message}"
                    }
            }
            .addOnFailureListener {
                tvInforme.text = "Error al obtener historial de medicamentos: ${it.message}"
            }
    }

    private fun guardarInformeComoPdf() {
        try {
            val texto = tvInforme.text.toString()
            if (texto.isEmpty()) {
                Toast.makeText(this, "No hay contenido para generar el PDF", Toast.LENGTH_SHORT).show()
                return
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size approx
            val page = pdfDocument.startPage(pageInfo)

            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f

            val lines = texto.split("\n")
            var y = 40f

            for (line in lines) {
                canvas.drawText(line, 10f, y, paint)
                y += paint.descent() - paint.ascent() + 5f
            }

            pdfDocument.finishPage(page)

            val pdfPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (pdfPath == null) {
                Toast.makeText(this, "No se puede acceder a documentos externos", Toast.LENGTH_SHORT).show()
                return
            }
            pdfFile = File(pdfPath, "InformeMedySync.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            Toast.makeText(this, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compartirPdf(pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(this, "com.example.medysync.provider", pdfFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Compartir PDF usando"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
