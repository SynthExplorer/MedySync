package com.example.medysync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*



class HistorialTomasActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val historialList = mutableListOf<HistorialToma>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_tomas)

        rvHistorial = findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(this)

        adapter = HistorialAdapter(historialList)
        rvHistorial.adapter = adapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarHistorial()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()
    }


    private fun cargarHistorial() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("historial_tomas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                historialList.clear()
                for (doc in documents) {
                    val nombre = doc.getString("nombre") ?: "Desconocido"
                    val dosis = doc.getString("dosis") ?: "N/A"
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()

                    historialList.add(HistorialToma(nombre, dosis, timestamp))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }
}
