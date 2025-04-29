package com.example.medysync

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Actividad1 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicamentoAdapter
    private val listaMedicamentos = mutableListOf<Medicamento>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad1)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FabFragment())
            .commit()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewMedicamentos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicamentoAdapter(listaMedicamentos)
        recyclerView.adapter = adapter

        obtenerMedicamentosDelUsuario()
    }

    override fun onResume() {
        super.onResume()
        obtenerMedicamentosDelUsuario()
    }

    private fun obtenerMedicamentosDelUsuario() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("usuarios")
                .document(userId)
                .collection("medicamentos")
                .get()
                .addOnSuccessListener { result ->
                    listaMedicamentos.clear()
                    for (document in result) {
                        val medicamento = document.toObject(Medicamento::class.java)
                        listaMedicamentos.add(medicamento)
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "✅ Medicamentos obtenidos correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "❌ Error al obtener los medicamentos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
