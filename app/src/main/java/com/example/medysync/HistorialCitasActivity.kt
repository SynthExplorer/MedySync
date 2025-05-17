package com.example.medysync

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HistorialCitasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CitaAdapter
    private val listaCitas = mutableListOf<Cita>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_citas)

        recyclerView = findViewById(R.id.rvCitas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CitaAdapter(listaCitas)
        recyclerView.adapter = adapter

        cargarCitasDesdeFirestore()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()
    }

    private fun cargarCitasDesdeFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(userId).collection("citas")
            .get()
            .addOnSuccessListener { result ->
                listaCitas.clear()

                val ahora = LocalDate.now()
                val citasParaMostrar = mutableListOf<Cita>()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                for (document in result) {
                    val cita = document.toObject(Cita::class.java)

                    try {
                        
                        val fechaCita = LocalDate.parse(cita.fecha, formatter)

                        val diasPasados = ChronoUnit.DAYS.between(fechaCita, ahora)

                        if (diasPasados <= 2) {
                            citasParaMostrar.add(cita)
                        } else {
                            // Elimina la cita antigua
                            db.collection("usuarios").document(userId)
                                .collection("citas").document(cita.id)
                                .delete()
                        }
                    } catch (e: Exception) {
                        Log.e("FechaParse", "Error al parsear fecha: ${cita.fecha}", e)
                    }
                }


                listaCitas.addAll(citasParaMostrar.sortedByDescending { it.fecha })
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar citas", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al leer citas: ", e)
            }
    }
}

