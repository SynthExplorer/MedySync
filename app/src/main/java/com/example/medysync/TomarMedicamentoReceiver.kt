package com.example.medysync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TomarMedicamentoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val nombre = intent.getStringExtra("nombre") ?: return
        val dosis = intent.getStringExtra("dosis") ?: return

        Log.d("TomarMedicamento", "Toma registrada: $nombre - $dosis")

        // Cancelar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id.hashCode())

        // Registrar en Firestore que se tomó el medicamento
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Actualizar la última toma
            db.collection("usuarios")
                .document(userId)
                .collection("medicamentos")
                .document(id)
                .update("ultimaToma", System.currentTimeMillis())
                .addOnSuccessListener {
                    Log.d("TomarMedicamento", "Última toma actualizada en Firestore")
                }
                .addOnFailureListener {
                    Log.e("TomarMedicamento", "Error al actualizar última toma: ${it.message}")
                }

            // Agregar al historial
            val nuevaToma = hashMapOf(
                "medicamentoId" to id,
                "nombre" to nombre,
                "dosis" to dosis,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("usuarios")
                .document(userId)
                .collection("historial_tomas")
                .add(nuevaToma)
                .addOnSuccessListener {
                    Log.d("TomarMedicamento", "Toma agregada al historial")
                    Toast.makeText(context, "✅ Medicamento registrado como tomado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("TomarMedicamento", "Error al agregar al historial: ${it.message}")
                }
        }
    }
}